package com.sk89q.worldedit.forge.compat;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import com.google.gson.Gson;
import com.sk89q.worldedit.util.gson.GsonUtil;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.forge.compat.RotationMappings;
import com.sk89q.worldedit.forge.compat.RotationUtils;
import com.sk89q.worldedit.forge.compat.RotationType;
import org.junit.Test;

/** Tests for {@link RotationMappings}. */
public class RotationMappingsTest {

    @Test
    public void testGenerateDefaults() throws Exception {
        File dir = Files.createTempDirectory("modrot").toFile();
        RotationMappings.init(dir);
        RotationMappings cfg = RotationMappings.getInstance();
        assertNotNull(cfg);
        File f = new File(dir, "mappings/stairs.json");
        assertTrue("config file missing", f.exists());

        Gson gson = GsonUtil.createBuilder().create();
        try (java.io.FileReader r = new java.io.FileReader(f)) {
            com.google.gson.stream.JsonReader jr = new com.google.gson.stream.JsonReader(r);
            jr.setLenient(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(jr, Map.class);
            if (map != null) {
                for (String key : map.keySet()) {
                    assertFalse("vanilla entry present", key.startsWith("minecraft:"));
                }
            }
        }
        // ensure comment exists
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String first = br.readLine();
            assertTrue(first.startsWith("//"));
        }
    }

    @Test
    public void testRotateDoorOpenMeta() {
        int meta = 4; // north open bottom
        AffineTransform rot = new AffineTransform();
        rot = rot.rotateY(-Math.PI / 2);
        int result = RotationUtils.rotateMeta(RotationType.DOOR, 1, meta);
        assertEquals("door open meta rotated", 5, result);
    }
}
