package com.sk89q.worldedit.forge.compat;

import com.sk89q.worldedit.math.transform.AffineTransform;

/** Rotation mapping for pillar blocks. */
public class PillarRotation implements RotationBase {
    private int x;
    private int y;
    private int z;

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    @Override
    public int rotate(int meta, int steps) {
        if ((steps % 2) != 0) {
            if (meta == x) return z;
            if (meta == z) return x;
        }
        return meta;
    }

    @Override
    public int transform(int meta, AffineTransform transform) {
        // rotation/flip around Y axis only affects x/z axes
        return rotate(meta, Math.round((float)(-transform.getRotations().getY()/90)));
    }
}
