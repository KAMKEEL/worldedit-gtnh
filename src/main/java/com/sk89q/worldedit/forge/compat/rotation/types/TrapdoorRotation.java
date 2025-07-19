package com.sk89q.worldedit.forge.compat.rotation.types;

import com.sk89q.worldedit.forge.compat.rotation.RotationUtils;
import com.sk89q.worldedit.math.transform.AffineTransform;

/**
 * Rotation handling for trapdoors supporting open/top states.
 */
public class TrapdoorRotation implements RotationBase {

    private int[] bottomClosed = { 0, 1, 2, 3 };
    private int[] bottomOpen = { 4, 5, 6, 7 };
    private int[] topClosed = { 8, 9, 10, 11 };
    private int[] topOpen = { 12, 13, 14, 15 };
    private int mask = 0xF;

    public int[] getBottomClosed() {
        return bottomClosed;
    }

    public void setBottomClosed(int[] m) {
        bottomClosed = m;
        computeMask();
    }

    public int[] getBottomOpen() {
        return bottomOpen;
    }

    public void setBottomOpen(int[] m) {
        bottomOpen = m;
        computeMask();
    }

    public int[] getTopClosed() {
        return topClosed;
    }

    public void setTopClosed(int[] m) {
        topClosed = m;
        computeMask();
    }

    public int[] getTopOpen() {
        return topOpen;
    }

    public void setTopOpen(int[] m) {
        topOpen = m;
        computeMask();
    }

    private void computeMask() {
        mask = 0;
        for (int v : bottomClosed) mask |= v;
        for (int v : bottomOpen) mask |= v;
        for (int v : topClosed) mask |= v;
        for (int v : topOpen) mask |= v;
    }

    @Override
    public int rotate(int meta, int steps) {
        int extras = meta & ~mask;
        int data = meta & mask;
        int s = Math.abs(steps) % 4;
        for (int i = 0; i < s; i++) {
            data = steps > 0 ? RotationUtils.rotateTrapdoor90(data) : RotationUtils.rotateTrapdoor90Reverse(data);
        }
        return data | extras;
    }

    @Override
    public int transform(int meta, AffineTransform transform) {
        return rotate(
            meta,
            Math.round(
                (float) (-transform.getRotations()
                    .getY() / 90)));
    }
}
