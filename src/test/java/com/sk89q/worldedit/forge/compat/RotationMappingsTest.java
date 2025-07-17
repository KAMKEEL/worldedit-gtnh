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

    @Test
    public void testRotateDoorTopHalf() {
        int meta = 8; // north top closed
        int result = RotationUtils.rotateMeta(RotationType.DOOR, 1, meta);
        assertEquals("door top half unchanged", 8, result);
    }

    @Test
    public void testDoorDefaultMap() {
        Map<String,Integer> map = RotationUtils.defaultMetaMap(RotationType.DOOR);
        assertEquals("door map entries", 12, map.size());
        assertEquals(Integer.valueOf(0), map.get("north_bottom_closed"));
        assertEquals(Integer.valueOf(4), map.get("north_bottom_open"));
        assertEquals(Integer.valueOf(8), map.get("top_left"));
        assertEquals(Integer.valueOf(9), map.get("top_right"));
    }

    @Test
    public void testTrapdoorDefaultMap() {
        Map<String,Integer> map = RotationUtils.defaultMetaMap(RotationType.TRAP_DOOR);
        assertEquals("trapdoor map entries", 16, map.size());
        assertEquals(Integer.valueOf(0), map.get("north_bottom_closed"));
        assertEquals(Integer.valueOf(4), map.get("north_bottom_open"));
        assertEquals(Integer.valueOf(8), map.get("north_top_closed"));
        assertEquals(Integer.valueOf(12), map.get("north_top_open"));
    }

    @Test
    public void testStairsBigMetaRotation() {
        int meta = 10; // meta >= 8
        int result = RotationUtils.rotateMeta(RotationType.STAIRS, 1, meta);
        assertEquals("big meta stairs rotated", 9, result);
    }

    @Test
    public void testPillarRotation() {
        int meta = 4; // x axis
        int result = RotationUtils.rotateMeta(RotationType.PILLAR, 1, meta);
        assertEquals("pillar axis swapped", 8, result);
        assertEquals("vertical pillar unchanged", 0, RotationUtils.rotateMeta(RotationType.PILLAR, 1, 0));
    }
}
