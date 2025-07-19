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

import com.sk89q.worldedit.forge.compat.RotationUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Loads and stores rotation mappings for modded blocks.
 */
public class ModRotationConfig {

    private static ModRotationConfig instance;

    private final File file;
    private final Gson gson = GsonUtil.createBuilder().setPrettyPrinting().create();
    private Map<String, RotationMapping> mappings = new HashMap<>();

    private ModRotationConfig(File file) {
        this.file = file;
        load();
    }

    public static void init(File dir) {
        instance = new ModRotationConfig(new File(dir, "mod-rotations.json"));
    }

    public static ModRotationConfig getInstance() {
        return instance;
    }

    public RotationMapping get(String name) {
        return mappings.get(name);
    }

    private void load() {
        if (!file.exists()) {
            generateDefaults();
            save();
            return;
        }
        Type type = new TypeToken<Map<String, RotationMapping>>(){}.getType();
        try (FileReader reader = new FileReader(file)) {
            Map<String, RotationMapping> raw = gson.fromJson(reader, type);
            if (raw != null) {
                mappings.putAll(raw);
            }
        } catch (IOException ignored) {
        }
    }

    private void save() {
        Map<String, RotationMapping> raw = new HashMap<>(mappings);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(raw, writer);
        } catch (IOException ignored) {
        }
    }

    private void generateDefaults() {
        Iterator<?> it = Block.blockRegistry.iterator();
        while (it.hasNext()) {
            Block block = (Block) it.next();
            Object identifier = Block.blockRegistry.getNameForObject(block);
            String name = identifier == null ? null : identifier.toString();
            if (name == null) continue;
            RotationMapping mapping = null;
            if (block instanceof BlockStairs) {
                mapping = new RotationMapping(RotationType.STAIRS,
                        RotationUtils.defaultStairs());
            } else if (block instanceof BlockRotatedPillar) {
                mapping = new RotationMapping(RotationType.PILLAR,
                        RotationUtils.defaultPillar());
            } else if (block instanceof BlockDoor) {
                mapping = new RotationMapping(RotationType.DOOR,
                        new DoorRotation());
            } else if (block instanceof BlockTrapDoor) {
                mapping = new RotationMapping(RotationType.TRAP_DOOR,
                        new TrapdoorRotation());
            } else if (block instanceof BlockFenceGate) {
                mapping = new RotationMapping(RotationType.FENCE_GATE,
                        RotationUtils.defaultFour(false));
            }
            if (mapping != null) {
                mappings.put(name, mapping);
            }
        }
    }
}
