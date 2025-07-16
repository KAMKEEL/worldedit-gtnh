package com.sk89q.worldedit.forge.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.transform.BlockTransformHook;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;

/**
 * Fallback rotation handler for modded stairs, pillars, doors and trap doors.
 */
public class ModRotationBlockTransformHook implements BlockTransformHook {

    private enum Type {
        STAIRS,
        PILLAR,
        DOOR,
        TRAP_DOOR
    }

    private final Map<Integer, Type> cache = new HashMap<>();

    private Type lookup(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        Block mcBlock = Block.getBlockById(id);
        Type result = null;
        if (mcBlock != null) {
            Object identifier = Block.blockRegistry.getNameForObject(mcBlock);
            String name = identifier == null ? null : identifier.toString();
            if (name != null && !name.startsWith("minecraft:")) {
                if (mcBlock instanceof BlockStairs) {
                    result = Type.STAIRS;
                } else if (mcBlock instanceof BlockRotatedPillar) {
                    result = Type.PILLAR;
                } else if (mcBlock instanceof BlockDoor) {
                    result = Type.DOOR;
                } else if (mcBlock instanceof BlockTrapDoor) {
                    result = Type.TRAP_DOOR;
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
        int ticks = Math.round((float) (-rot.getY() / 90));
        if (ticks == 0) {
            return block;
        }
        int data = block.getData();
        int steps = Math.abs(ticks) % 4;
        for (int i = 0; i < steps; i++) {
            switch (type) {
                case STAIRS:
                    data = ticks > 0 ? rotateStairs90(data) : rotateStairs90Reverse(data);
                    break;
                case PILLAR:
                    data = rotatePillar90(data); // same both directions
                    break;
                case DOOR:
                    data = ticks > 0 ? rotateDoor90(data) : rotateDoor90Reverse(data);
                    break;
                case TRAP_DOOR:
                    data = ticks > 0 ? rotateTrapdoor90(data) : rotateTrapdoor90Reverse(data);
                    break;
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
        boolean bigMeta = data >= 8;
        int meta = bigMeta ? data - 8 : data;
        int result;
        switch (meta) {
            case 0:
                result = 2;
                break;
            case 1:
                result = 3;
                break;
            case 2:
                result = 1;
                break;
            case 3:
                result = 0;
                break;
            case 4:
                result = 6;
                break;
            case 5:
                result = 7;
                break;
            case 6:
                result = 5;
                break;
            case 7:
                result = 4;
                break;
            default:
                result = meta;
        }
        return bigMeta ? result + 8 : result;
    }

    private int rotateStairs90Reverse(int data) {
        boolean bigMeta = data >= 8;
        int meta = bigMeta ? data - 8 : data;
        int result;
        switch (meta) {
            case 2:
                result = 0;
                break;
            case 3:
                result = 1;
                break;
            case 1:
                result = 2;
                break;
            case 0:
                result = 3;
                break;
            case 6:
                result = 4;
                break;
            case 7:
                result = 5;
                break;
            case 5:
                result = 6;
                break;
            case 4:
                result = 7;
                break;
            default:
                result = meta;
        }
        return bigMeta ? result + 8 : result;
    }

    private int rotateDoor90(int data) {
        int extra = data & ~0x3;
        int without = data & 0x3;
        switch (without) {
            case 0:
                return 1 | extra;
            case 1:
                return 2 | extra;
            case 2:
                return 3 | extra;
            case 3:
                return 0 | extra;
            default:
                return data;
        }
    }

    private int rotateDoor90Reverse(int data) {
        int extra = data & ~0x3;
        int without = data & 0x3;
        switch (without) {
            case 1:
                return 0 | extra;
            case 2:
                return 1 | extra;
            case 3:
                return 2 | extra;
            case 0:
                return 3 | extra;
            default:
                return data;
        }
    }

    private int rotateTrapdoor90(int data) {
        int without = data & ~0x3;
        int orientation = data & 0x3;
        switch (orientation) {
            case 0:
                return 3 | without;
            case 1:
                return 2 | without;
            case 2:
                return 0 | without;
            case 3:
                return 1 | without;
            default:
                return data;
        }
    }

    private int rotateTrapdoor90Reverse(int data) {
        int without = data & ~0x3;
        int orientation = data & 0x3;
        switch (orientation) {
            case 3:
                return 0 | without;
            case 2:
                return 1 | without;
            case 0:
                return 2 | without;
            case 1:
                return 3 | without;
            default:
                return data;
        }
    }
}
