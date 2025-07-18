package com.sk89q.worldedit.forge.compat;

/** Rotation handling for doors using default meta rules. */
public class DoorRotation implements RotationBase {
    @Override
    public int rotate(int meta, int steps) {
        return RotationUtils.rotateMeta(RotationType.DOOR, steps, meta);
    }
}
