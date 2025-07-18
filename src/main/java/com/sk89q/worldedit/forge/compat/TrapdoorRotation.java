package com.sk89q.worldedit.forge.compat;

/** Rotation handling for trapdoors using default meta rules. */
public class TrapdoorRotation implements RotationBase {
    @Override
    public int rotate(int meta, int steps) {
        return RotationUtils.rotateMeta(RotationType.TRAP_DOOR, steps, meta);
    }
}
