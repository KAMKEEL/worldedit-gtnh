# Schematic AddBlocks2 notes

`AddBlocks2` is only written when a block in the clipboard has an ID above 4095.
If a mod remaps its runtime IDs below that ceiling (e.g., showing `15122` in the
UI but exposing `<=4095` to WorldEdit), the `AddBlocks2` array stays empty
because `SchematicWriter` never sees a value over 4095 when iterating the
clipboard blocks. The NEID transformer raises `BaseBlock.MAX_ID`, but WorldEdit
still depends on the runtime IDs it receives from the world when deciding
whether to populate `AddBlocks2`.
