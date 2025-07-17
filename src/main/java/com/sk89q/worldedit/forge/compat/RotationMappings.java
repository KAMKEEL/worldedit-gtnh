package com.sk89q.worldedit.forge.compat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.gson.GsonUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockButton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Loads and stores rotation mappings for modded blocks.
 */
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
            Map<String, RotationMapping> loaded = loadFile(file, type);
            if (loaded != null) {
                mappings.putAll(loaded);
            }
        }
        if (mappings.isEmpty()) {
            generateDefaults();
        }
        saveAll();
    }

    private Map<String, RotationMapping> loadFile(File file, RotationType type) {
        if (!file.exists()) {
            return null;
        }
        Type mapType = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();
        try (FileReader r = new FileReader(file)) {
            com.google.gson.stream.JsonReader jr = new com.google.gson.stream.JsonReader(r);
            jr.setLenient(true);
            Map<String, Map<String, Integer>> raw = gson.fromJson(jr, mapType);
            if (raw == null) return null;
            Map<String, RotationMapping> result = new HashMap<>();
            for (Map.Entry<String, Map<String, Integer>> e : raw.entrySet()) {
                RotationMapping rm = new RotationMapping(type);
                rm.setMetas(e.getValue());
                result.put(e.getKey(), rm);
            }
            return result;
        } catch (IOException ignore) {
            return null;
        }
    }

    private void saveAll() {
        Map<RotationType, Map<String, Map<String, Integer>>> byType = new EnumMap<>(RotationType.class);
        for (RotationType t : RotationType.values()) {
            byType.put(t, new HashMap<>());
        }
        for (Map.Entry<String, RotationMapping> e : mappings.entrySet()) {
            byType.get(e.getValue().getType()).put(e.getKey(), e.getValue().getMetas());
        }
        for (RotationType t : RotationType.values()) {
            File file = new File(dir, t.name().toLowerCase() + ".json");
            try (FileWriter w = new FileWriter(file)) {
                w.write("// Rotation mappings for " + t.name().toLowerCase() + "\n");
                if (t == RotationType.OTHER) {
                    w.write("// Add block id to map custom meta directions\n");
                }
                gson.toJson(byType.get(t), w);
            } catch (IOException ignore) {
            }
        }
    }

    private void generateDefaults() {
        Iterator<?> it = Block.blockRegistry.iterator();
        while (it.hasNext()) {
            Block block = (Block) it.next();
            Object identifier = Block.blockRegistry.getNameForObject(block);
            String name = identifier == null ? null : identifier.toString();
            if (name == null || name.startsWith("minecraft:")) continue;
            RotationType type = null;
            if (block instanceof BlockStairs) {
                type = RotationType.STAIRS;
            } else if (block instanceof BlockRotatedPillar) {
                type = RotationType.PILLAR;
            } else if (block instanceof BlockDoor) {
                type = RotationType.DOOR;
            } else if (block instanceof BlockTrapDoor) {
                type = RotationType.TRAP_DOOR;
            } else if (block instanceof BlockFenceGate) {
                type = RotationType.FENCE_GATE;
            } else if (block instanceof BlockButton) {
                type = RotationType.OTHER;
            }
            if (type != null) {
                RotationMapping mapping = new RotationMapping(type);
                Map<String,Integer> metas;
                if (block instanceof BlockButton) {
                    metas = RotationUtils.defaultButtonMap();
                } else {
                    metas = RotationUtils.defaultMetaMap(type);
                }
                mapping.setMetas(metas);
                mappings.put(name, mapping);
            }
        }
    }
}
