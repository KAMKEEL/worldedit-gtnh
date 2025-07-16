package com.sk89q.worldedit.forge.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.ResourceLocation;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.transform.BlockTransformHook;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;

/**
 * Fallback rotation handler for modded stairs and pillar blocks.
 */
public class ModRotationBlockTransformHook implements BlockTransformHook {

    private enum Type {
        STAIRS,
        PILLAR
    }

    private final Map<Integer, Type> cache = new HashMap<>();

    private Type lookup(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        Block mcBlock = Block.getBlockById(id);
        Type result = null;
        if (mcBlock != null) {
            ResourceLocation name = (ResourceLocation) Block.blockRegistry.getNameForObject(mcBlock);
            if (name != null && !"minecraft".equals(name.getResourceDomain())) {
                if (mcBlock instanceof BlockStairs) {
                    result = Type.STAIRS;
                } else if (mcBlock instanceof BlockRotatedPillar) {
                    result = Type.PILLAR;
                }
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
        Type type = lookup(block.getId());
        if (type == null) {
            return block;
        }
        Vector rot = affine.getRotations();
        int ticks = Math.round((float) (rot.getY() / 90));
        if (ticks == 0) {
            return block;
        }
        int data = block.getData();
        int steps = Math.abs(ticks) % 4;
        for (int i = 0; i < steps; i++) {
            if (type == Type.STAIRS) {
                data = ticks > 0 ? rotateStairs90(data) : rotateStairs90Reverse(data);
            } else {
                data = rotatePillar90(data); // same both directions
            }
        }
        block.setData(data);
        return block;
    }

    private int rotatePillar90(int data) {
        if (data >= 4 && data <= 11) {
            return data ^ 0xC;
        }
        return data;
    }

    private int rotateStairs90(int data) {
        switch (data) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 1;
            case 3:
                return 0;
            case 4:
                return 6;
            case 5:
                return 7;
            case 6:
                return 5;
            case 7:
                return 4;
            default:
                return data;
        }
    }

    private int rotateStairs90Reverse(int data) {
        switch (data) {
            case 2:
                return 0;
            case 3:
                return 1;
            case 1:
                return 2;
            case 0:
                return 3;
            case 6:
                return 4;
            case 7:
                return 5;
            case 5:
                return 6;
            case 4:
                return 7;
            default:
                return data;
        }
    }
}
