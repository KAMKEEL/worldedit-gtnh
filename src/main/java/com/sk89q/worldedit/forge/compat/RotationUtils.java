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

    /** Rotate the given metadata according to the rotation type and transform. */
    public static int rotateMeta(RotationType type, int ticks, int data) {
        if (ticks == 0) {
            return data;
        }
        int steps = Math.abs(ticks) % 4;
        for (int i = 0; i < steps; i++) {
            switch (type) {
                case STAIRS:
                    data = ticks > 0 ? rotateStairs90(data) : rotateStairs90Reverse(data);
                    break;
                case PILLAR:
                    data = rotatePillar90(data);
                    break;
                case DOOR:
                    data = ticks > 0 ? rotateDoor90(data) : rotateDoor90Reverse(data);
                    break;
                case TRAP_DOOR:
                    data = ticks > 0 ? rotateTrapdoor90(data) : rotateTrapdoor90Reverse(data);
                    break;
                case FENCE_GATE:
                    data = ticks > 0 ? rotateFenceGate90(data) : rotateFenceGate90Reverse(data);
                    break;
                default:
                    break;
            }
        }
        return data;
    }

    public static Map<String,Integer> defaultMetaMap(RotationType type) {
        Map<String,Integer> map = new LinkedHashMap<>();
        switch (type) {
            case STAIRS:
                fillDirectional(map, 0, RotationUtils::rotateStairs90, "_bottom");
                fillDirectional(map, 4, RotationUtils::rotateStairs90, "_top");
                break;
            case DOOR:
                fillDoor(map);
                break;
            case TRAP_DOOR:
                fillTrapDoor(map);
                break;
            case FENCE_GATE:
                fillDirectional(map, 0, RotationUtils::rotateFenceGate90, "");
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

    private static void fillDirectional(Map<String,Integer> map, int start, IntUnaryOperator rot, String suffix) {
        String[] dirs = {"north","east","south","west"};
        int meta = start;
        for (String dir : dirs) {
            map.put(dir + suffix, meta);
            meta = rot.applyAsInt(meta);
        }
    }

    private static void fillDoor(Map<String, Integer> map) {
        int meta = 0;
        String[] dirs = {"north","east","south","west"};
        for (String dir : dirs) {
            map.put(dir + "_bottom_closed", meta);
            map.put(dir + "_bottom_open", meta | 4);
            map.put(dir + "_top_closed", meta | 8);
            map.put(dir + "_top_open", meta | 12);
            meta = rotateDoor90(meta);
        }
    }

    private static void fillTrapDoor(Map<String, Integer> map) {
        int meta = 0;
        String[] dirs = {"north","east","south","west"};
        for (String dir : dirs) {
            map.put(dir + "_bottom_closed", meta);
            map.put(dir + "_bottom_open", meta | 4);
            map.put(dir + "_top_closed", meta | 8);
            map.put(dir + "_top_open", meta | 12);
            meta = rotateTrapdoor90(meta);
        }
    }
}
