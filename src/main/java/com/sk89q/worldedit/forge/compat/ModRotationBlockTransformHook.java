package com.sk89q.worldedit.forge.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import com.sk89q.worldedit.forge.compat.RotationMappings;
import com.sk89q.worldedit.forge.compat.RotationMapping;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.transform.BlockTransformHook;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;

/**
 * Fallback rotation handler for modded stairs, pillars, doors and trap doors.
 */
public class ModRotationBlockTransformHook implements BlockTransformHook {

    private final Map<Integer, RotationMapping> cache = new HashMap<>();

    private RotationMapping lookup(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        Block mcBlock = Block.getBlockById(id);
        RotationMapping result = null;
        if (mcBlock != null) {
            Object identifier = Block.blockRegistry.getNameForObject(mcBlock);
            String name = identifier == null ? null : identifier.toString();
            if (name != null) {
                result = RotationMappings.getInstance().get(name);
            }
        }
        cache.put(id, result);
        return result;
    }

    @Override
    public BaseBlock transformBlock(BaseBlock block, Transform transform) {
        if (!(transform instanceof AffineTransform affine)) {
            return block;
        }
        RotationMapping mapping = lookup(block.getId());
        if (mapping == null) {
            return block;
        }
        int data = block.getData();
        if (mapping.getMetas() != null && !mapping.getMetas().isEmpty()) {
            data = rotateTransform(data, mapping.getMetas(), affine);
        }
        block.setData(data);
        return block;
    }

    private static final Map<String, Vector> DIRS = new HashMap<>();
    static {
        DIRS.put("north", new Vector(0, 0, -1));
        DIRS.put("south", new Vector(0, 0, 1));
        DIRS.put("east", new Vector(1, 0, 0));
        DIRS.put("west", new Vector(-1, 0, 0));
        DIRS.put("x", new Vector(1, 0, 0));
        DIRS.put("y", new Vector(0, 1, 0));
        DIRS.put("z", new Vector(0, 0, 1));
    }

    private int rotateTransform(int data, Map<String, Integer> map, AffineTransform transform) {
        String key = null;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() == data) {
                key = e.getKey();
                break;
            }
        }
        if (key == null) {
            return data;
        }
        String suffix = "";
        String base = key;
        if (key.endsWith("_top")) {
            suffix = "_top";
            base = key.substring(0, key.length() - 4);
        } else if (key.endsWith("_bottom")) {
            suffix = "_bottom";
            base = key.substring(0, key.length() - 7);
        }
        Vector vec = DIRS.get(base.toLowerCase());
        if (vec == null) {
            return data;
        }
        Vector out = transform.apply(vec).subtract(transform.apply(Vector.ZERO)).normalize();
        String best = null;
        double bestDot = -2;
        for (String cand : map.keySet()) {
            String c = cand;
            if (c.endsWith("_top")) c = c.substring(0, c.length() - 4);
            else if (c.endsWith("_bottom")) c = c.substring(0, c.length() - 7);
            Vector dirVec = DIRS.get(c.toLowerCase());
            if (dirVec == null) continue;
            double dot = dirVec.normalize().dot(out);
            if (dot > bestDot) {
                bestDot = dot;
                best = c;
            }
        }
        if (best == null) {
            return data;
        }
        Integer newMeta = map.get(best + suffix);
        return newMeta != null ? newMeta : data;
    }
}
