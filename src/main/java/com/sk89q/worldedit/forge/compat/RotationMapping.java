package com.sk89q.worldedit.forge.compat;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping information for rotating a block.
 */
public class RotationMapping {

    private RotationType type = RotationType.OTHER;
    private Map<String, Integer> metas = new HashMap<>();

    public RotationMapping() {
    }

    public RotationMapping(RotationType type) {
        this.type = type;
    }

    public RotationType getType() {
        return type;
    }

    public void setType(RotationType type) {
        this.type = type;
    }

    public Map<String, Integer> getMetas() {
        return metas;
    }

    public void setMetas(Map<String, Integer> metas) {
        this.metas = metas;
    }
}
