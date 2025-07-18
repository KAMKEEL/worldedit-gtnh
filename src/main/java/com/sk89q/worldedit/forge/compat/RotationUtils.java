package com.sk89q.worldedit.forge.compat;

import java.util.function.IntUnaryOperator;

/** Utility methods for rotation mappings. */
public final class RotationUtils {
    private RotationUtils() {}

    public static int rotatePillar90(int data) {
        int axis = data & 0xC;
        if (axis == 0x4) { // x -> z
            return (data & ~0xC) | 0x8;
        } else if (axis == 0x8) { // z -> x
            return (data & ~0xC) | 0x4;
        } else {
            return data;
        }
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
        if ((data & 0x8) != 0) {
            return data; // top half stores no orientation
        }
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
        if ((data & 0x8) != 0) {
            return data;
        }
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

    public static StairRotation defaultStairs() {
        StairRotation sr = new StairRotation();
        sr.setBottom(fillDirectional(3, RotationUtils::rotateStairs90));
        sr.setTop(fillDirectional(7, RotationUtils::rotateStairs90));
        return sr;
    }

    public static PillarRotation defaultPillar() {
        PillarRotation pr = new PillarRotation();
        pr.setY(0);
        pr.setX(4);
        pr.setZ(8);
        return pr;
    }

    public static FourRotation defaultFour(boolean button) {
        int start = button ? 4 : 2; // north facing for fence gates
        IntUnaryOperator rot = button ? RotationUtils::rotateButton90 : RotationUtils::rotateFenceGate90;
        FourRotation fr = new FourRotation();
        fr.setMetas(fillDirectional(start, rot));
        return fr;
    }

    private static int[] fillDirectional(int start, IntUnaryOperator rot) {
        int[] arr = new int[4];
        int meta = start;
        for (int i=0;i<4;i++) {
            arr[i] = meta;
            meta = rot.applyAsInt(meta);
        }
        return arr;
    }

    public static int rotateButton90(int data) {
        int dir = data & 7;
        int pressed = data & 8;
        int out;
        switch (dir) {
            case 1: out = 3; break; // east -> south
            case 3: out = 2; break; // south -> west
            case 2: out = 4; break; // west -> north
            case 4: out = 1; break; // north -> east
            default: out = dir; break;
        }
        return out | pressed;
    }

    public static int rotateButton90Reverse(int data) {
        int dir = data & 7;
        int pressed = data & 8;
        int out;
        switch (dir) {
            case 3: out = 1; break; // south -> east
            case 2: out = 3; break; // west -> south
            case 4: out = 2; break; // north -> west
            case 1: out = 4; break; // east -> north
            default: out = dir; break;
        }
        return out | pressed;
    }
}
