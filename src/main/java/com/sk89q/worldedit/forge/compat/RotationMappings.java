package com.sk89q.worldedit.forge.compat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.util.gson.GsonUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockButton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Manages loading and saving rotation mappings. */
public class RotationMappings {
    private static RotationMappings instance;

    private final File dir;
    private final Gson gson = GsonUtil.createBuilder().setPrettyPrinting().create();
    private final Map<String, RotationMapping> mappings = new HashMap<>();

    private RotationMappings(File dir) {
        this.dir = dir;
        loadAll();
    }

    public static void init(File workingDir) {
        File mapDir = new File(workingDir, "mappings");
        if (!mapDir.exists()) {
            mapDir.mkdirs();
        }
        instance = new RotationMappings(mapDir);
    }

    public static RotationMappings getInstance() {
        return instance;
    }

    public RotationMapping get(String name) {
        return mappings.get(name);
    }

    private void loadAll() {
        for (RotationType type : RotationType.values()) {
            File file = new File(dir, type.name().toLowerCase() + ".json");
            loadFile(file, type);
        }
        if (mappings.isEmpty()) {
            generateDefaults();
            saveAll();
        }
    }

    private void loadFile(File file, RotationType type) {
        if (!file.exists()) return;
        try (FileReader r = new FileReader(file)) {
            JsonObject root = gson.fromJson(r, JsonObject.class);
            if (root == null) return;
            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                JsonObject obj = e.getValue().getAsJsonObject();
                RotationMapping rm = parseMapping(type, obj);
                if (rm != null) {
                    mappings.put(e.getKey(), rm);
                }
            }
        } catch (IOException ignore) {}
    }

    private RotationMapping parseMapping(RotationType type, JsonObject obj) {
        switch (type) {
            case STAIRS:
                int[] top = arr(obj.getAsJsonArray("top"));
                int[] bottom = arr(obj.getAsJsonArray("bottom"));
                return new RotationMapping(type, new StairRotation(){
                    { setTop(top); setBottom(bottom); }
                });
            case PILLAR:
                PillarRotation p = new PillarRotation();
                p.setX(obj.get("x").getAsInt());
                p.setY(obj.get("y").getAsInt());
                p.setZ(obj.get("z").getAsInt());
                return new RotationMapping(type, p);
            case FENCE_GATE:
            case OTHER:
                int[] m = arr(obj.getAsJsonArray("metas"));
                return new RotationMapping(type, new FourRotation(){ { setMetas(m); } });
            case DOOR:
            case TRAP_DOOR:
            default:
                return null; // not implemented
        }
    }

    private int[] arr(JsonArray a) {
        int[] out = new int[a.size()];
        for (int i=0;i<a.size();i++) out[i] = a.get(i).getAsInt();
        return out;
    }

    private JsonArray toArray(int[] a) {
        JsonArray j = new JsonArray();
        for (int v : a) j.add(new com.google.gson.JsonPrimitive(v));
        return j;
    }

    private void saveAll() {
        Map<RotationType, JsonObject> byType = new HashMap<>();
        for (RotationType t : RotationType.values()) {
            byType.put(t, new JsonObject());
        }
        for (Map.Entry<String, RotationMapping> e : mappings.entrySet()) {
            JsonObject obj = new JsonObject();
            RotationMapping rm = e.getValue();
            RotationBase base = rm.getBase();
            if (base instanceof StairRotation s) {
                obj.add("top", toArray(s.getTop()));
                obj.add("bottom", toArray(s.getBottom()));
            } else if (base instanceof PillarRotation p) {
                obj.addProperty("x", p.getX());
                obj.addProperty("y", p.getY());
                obj.addProperty("z", p.getZ());
            } else if (base instanceof FourRotation f) {
                obj.add("metas", toArray(f.getMetas()));
            }
            byType.get(rm.getType()).add(e.getKey(), obj);
        }
        for (RotationType t : RotationType.values()) {
            File file = new File(dir, t.name().toLowerCase() + ".json");
            try (FileWriter w = new FileWriter(file)) {
                w.write("// Rotation mappings for " + t.name().toLowerCase() + "\n");
                gson.toJson(byType.get(t), w);
            } catch (IOException ignore) {}
        }
    }

    private void generateDefaults() {
        Iterator<?> it = Block.blockRegistry.iterator();
        while (it.hasNext()) {
            Block block = (Block) it.next();
            Object identifier = Block.blockRegistry.getNameForObject(block);
            String name = identifier == null ? null : identifier.toString();
            if (name == null || name.startsWith("minecraft:")) continue;
            RotationMapping mapping = null;
            if (block instanceof BlockStairs) {
                StairRotation sr = RotationUtils.defaultStairs();
                mapping = new RotationMapping(RotationType.STAIRS, sr);
            } else if (block instanceof BlockRotatedPillar) {
                PillarRotation pr = RotationUtils.defaultPillar();
                mapping = new RotationMapping(RotationType.PILLAR, pr);
            } else if (block instanceof BlockFenceGate || block instanceof BlockButton) {
                FourRotation fr = RotationUtils.defaultFour(block instanceof BlockButton);
                mapping = new RotationMapping(block instanceof BlockFenceGate ? RotationType.FENCE_GATE : RotationType.OTHER, fr);
            }
            if (mapping != null) {
                mappings.put(name, mapping);
            }
        }
    }
}
