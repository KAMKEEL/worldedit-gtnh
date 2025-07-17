package com.sk89q.worldedit.forge.compat;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import com.google.gson.Gson;
import com.sk89q.worldedit.util.gson.GsonUtil;
import org.junit.Test;

/** Tests for {@link ModRotationConfig}. */
public class ModRotationConfigTest {

    @Test
    public void testGenerateDefaults() throws Exception {
        File dir = Files.createTempDirectory("modrot").toFile();
        System.out.println("DIR=" + dir.getAbsolutePath());
        ModRotationConfig.init(dir);
        ModRotationConfig cfg = ModRotationConfig.getInstance();
        assertNotNull(cfg);
        File f = new File(dir, "mod-rotations.json");
        assertTrue("config file missing", f.exists());

        Gson gson = GsonUtil.createBuilder().create();
        try (java.io.FileReader r = new java.io.FileReader(f)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(r, Map.class);
            if (map != null) {
                for (String key : map.keySet()) {
                    assertFalse("vanilla entry present", key.startsWith("minecraft:"));
                }
            }
        }
    }
}
