package com.sk89q.worldedit.forge.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;

import com.sk89q.worldedit.forge.compat.RotationType;
import com.sk89q.worldedit.forge.compat.ModRotationConfig;
import com.sk89q.worldedit.forge.compat.RotationMapping;
import com.sk89q.worldedit.forge.compat.RotationUtils;

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
                result = ModRotationConfig.getInstance().get(name);
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
        Vector rot = affine.getRotations();
        int ticks = Math.round((float) (-rot.getY() / 90));
        if (ticks == 0) {
            return block;
        }
        int data = block.getData();
        int steps = Math.abs(ticks) % 4;
        for (int i = 0; i < steps; i++) {
            if (mapping.getMetas() != null && !mapping.getMetas().isEmpty()) {
                data = rotateCustom(data, mapping.getMetas(), ticks > 0);
                continue;
            }
            switch (mapping.getType()) {
                case STAIRS:
                    data = ticks > 0 ? RotationUtils.rotateStairs90(data) : RotationUtils.rotateStairs90Reverse(data);
                    break;
                case PILLAR:
                    data = RotationUtils.rotatePillar90(data); // same both directions
                    break;
                case DOOR:
                    data = ticks > 0 ? RotationUtils.rotateDoor90(data) : RotationUtils.rotateDoor90Reverse(data);
                    break;
                case TRAP_DOOR:
                    data = ticks > 0 ? RotationUtils.rotateTrapdoor90(data) : RotationUtils.rotateTrapdoor90Reverse(data);
                    break;
                case FENCE_GATE:
                    data = ticks > 0 ? RotationUtils.rotateFenceGate90(data) : RotationUtils.rotateFenceGate90Reverse(data);
                    break;
                case OTHER:
                    // no meta mapping provided
                    break;
            }
        }
        block.setData(data);
        return block;
    }


    private int rotateCustom(int data, Map<String, Integer> map, boolean clockwise) {
        if (map == null || map.isEmpty()) {
            return data;
        }
        int extra = data & ~0x3;
        int orientation = data & 0x3;
        String dir = null;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() == orientation) {
                dir = e.getKey().toLowerCase();
                break;
            }
        }
        if (dir == null) {
            return data;
        }
        String[] order = {"north", "east", "south", "west"};
        int idx = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(dir)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            return data;
        }
        int newIdx = clockwise ? (idx + 1) & 3 : (idx + 3) & 3;
        Integer newMeta = map.get(order[newIdx]);
        if (newMeta == null) {
            return data;
        }
        return extra | newMeta;
    }
}
