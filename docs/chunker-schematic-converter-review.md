# Chunker SchematicConverter AddBlocks2 behavior review

This note reviews the `SchematicConverter` from the ChunkPort branch `implement-schematic-conversion-system-te5vi7` and how it handles the NotEnoughIDs `AddBlocks2` extension in classic WorldEdit schematics.

## Reading schematics

* Classic schematics combine `Blocks` (low 8 bits) with optional `AddBlocks` (bits 8–11) and `AddBlocks2` (bits 12–15) nibble arrays. The converter reconstructs the runtime block ID by nibble-unpacking both arrays when present, so any upper bits saved in `AddBlocks2` are honored while loading.

```java
byte[] blocks = root.getByteArray("Blocks");
byte[] addBlocks = root.contains("AddBlocks") ? root.getByteArray("AddBlocks") : null;
byte[] addBlocks2 = root.contains("AddBlocks2") ? root.getByteArray("AddBlocks2") : null;
...
int id = blocks[index] & 0xFF;
if (addBlocks != null && (index >> 1) < addBlocks.length) {
    int add = (index & 1) == 0 ? addBlocks[index >> 1] & 0x0F : (addBlocks[index >> 1] & 0xF0) >> 4;
    id |= add << 8;
}
if (addBlocks2 != null && (index >> 1) < addBlocks2.length) {
    int add = (index & 1) == 0 ? addBlocks2[index >> 1] & 0x0F : (addBlocks2[index >> 1] & 0xF0) >> 4;
    id |= add << 12;
}
```

## Writing schematics

* The converter always writes classic schematics and packs IDs >255 into `AddBlocks`.
* IDs above 4095 are only preserved if the caller passes `allowNeids = true`, which allocates an `AddBlocks2` nibble array and stores the upper 4 bits (bits 12–15). With `allowNeids = false`, those high bits are dropped and IDs are truncated to 12 bits.
* The nibble packing mirrors the read logic (half-byte per block, `(volume >> 1) + 1` length), so data that was originally saved with `AddBlocks2` will round-trip correctly when NEIDs is allowed.

```java
int id = schematic.getBlockIds()[i];
blocks[i] = (byte) (id & 0xFF);
if (id > 255) {
    if (addBlocks == null) addBlocks = new byte[(volume >> 1) + 1];
    addBlocks[i >> 1] = (byte) (((i & 1) == 0) ? addBlocks[i >> 1] & 0xF0 | (id >> 8) & 0xF
            : addBlocks[i >> 1] & 0xF | ((id >> 8) & 0xF) << 4);
}
if (allowNeids && id > 4095) {
    if (addBlocks2 == null) addBlocks2 = new byte[(volume >> 1) + 1];
    addBlocks2[i >> 1] = (byte) (((i & 1) == 0) ? addBlocks2[i >> 1] & 0xF0 | (id >> 12) & 0xF
            : addBlocks2[i >> 1] & 0xF | ((id >> 12) & 0xF) << 4);
}
```

## Takeaways

* `AddBlocks2` is functionally supported: it is read, and it is written when `allowNeids` is set and an ID exceeds 4095.
* Conversions that omit the `allowNeids` flag will silently lose the upper nibble of IDs >4095, matching vanilla WorldEdit’s 12-bit ceiling.
* The nibble packing for `AddBlocks2` matches the reading logic and the classic WorldEdit convention, so round-tripping high IDs is consistent as long as NEIDs output is enabled.
