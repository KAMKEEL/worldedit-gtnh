package com.sk89q.worldedit.forge.compat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/** Utility methods for rotation mappings. */
public final class RotationUtils {
    private RotationUtils() {}

    public static int rotatePillar90(int data) {
        if (data >= 4 && data <= 11) {
            return data ^ 0xC;
        }
        return data;
    }

    public static int rotateStairs90(int data) {
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

    public static int rotateStairs90Reverse(int data) {
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

    public static int rotateDoor90(int data) {
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

    public static int rotateDoor90Reverse(int data) {
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

    public static int rotateTrapdoor90(int data) {
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

    public static int rotateTrapdoor90Reverse(int data) {
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

    public static int rotateFenceGate90(int data) {
        int extra = data & ~0x3;
        int orientation = data & 0x3;
        orientation = (orientation + 1) & 3;
        return orientation | extra;
    }

    public static int rotateFenceGate90Reverse(int data) {
        int extra = data & ~0x3;
        int orientation = data & 0x3;
        orientation = (orientation + 3) & 3;
        return orientation | extra;
    }

    public static Map<String,Integer> defaultMetaMap(RotationType type) {
        Map<String,Integer> map = new LinkedHashMap<>();
        switch (type) {
            case STAIRS:
                fillDirectional(map, 0, RotationUtils::rotateStairs90);
                fillDirectional(map, 4, RotationUtils::rotateStairs90);
                break;
            case DOOR:
                fillDirectional(map, 0, RotationUtils::rotateDoor90);
                break;
            case TRAP_DOOR:
                fillDirectional(map, 0, RotationUtils::rotateTrapdoor90);
                break;
            case FENCE_GATE:
                fillDirectional(map, 0, RotationUtils::rotateFenceGate90);
                break;
            case PILLAR:
                map.put("y", 0);
                map.put("x", 4);
                map.put("z", 8);
                break;
            default:
                break;
        }
        return map;
    }

    private static void fillDirectional(Map<String,Integer> map, int start, IntUnaryOperator rot) {
        String[] dirs = {"north","east","south","west"};
        int meta = start;
        for (String dir : dirs) {
            map.put(dir, meta);
            meta = rot.applyAsInt(meta);
        }
    }
}
