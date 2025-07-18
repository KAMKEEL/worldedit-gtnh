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
import com.sk89q.worldedit.forge.compat.StairRotation;
import com.sk89q.worldedit.forge.compat.PillarRotation;
import com.sk89q.worldedit.forge.compat.FourRotation;
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

        // add a dummy custom mapping and verify type key
        RotationMapping custom = new RotationMapping(RotationType.OTHER, RotationUtils.defaultFour(true));
        cfg.put("dummy:button", custom);
        cfg.save();
        File other = new File(dir, "mappings/other.json");
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(other))) {
            String comment = br.readLine();
            assertTrue(comment.startsWith("//"));
            com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(br).getAsJsonObject();
            assertTrue(obj.getAsJsonObject("dummy:button").has("type"));
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

    // door and trapdoor defaults covered by rotateMeta tests

    @Test
    public void testStairsBigMetaRotation() {
        int meta = 10; // meta >= 8
        int result = RotationUtils.rotateMeta(RotationType.STAIRS, 1, meta);
        assertEquals("big meta stairs rotated", 9, result);
    }

    @Test
    public void testStairsLowMetaRotation() {
        int meta = 3; // north bottom
        int result = RotationUtils.rotateMeta(RotationType.STAIRS, 1, meta);
        assertEquals("low meta stairs rotated", 0, result);
    }

    @Test
    public void testStairsDefaultArrays() {
        StairRotation sr = RotationUtils.defaultStairs();
        assertEquals(3, sr.getBottom()[0]);
        assertEquals(7, sr.getTop()[0]);
    }

    @Test
    public void testStairsRotateAll() {
        StairRotation sr = RotationUtils.defaultStairs();
        for (int meta = 0; meta < 16; meta++) {
            int expected = RotationUtils.rotateStairs90(meta);
            assertEquals("meta " + meta, expected, sr.rotate(meta, 1));
        }
    }

    @Test
    public void testPillarRotation() {
        int meta = 4; // x axis
        int result = RotationUtils.rotateMeta(RotationType.PILLAR, 1, meta);
        assertEquals("pillar axis swapped", 8, result);
        assertEquals("vertical pillar unchanged", 0, RotationUtils.rotateMeta(RotationType.PILLAR, 1, 0));
    }


    @Test
    public void testPillarDefault() {
        PillarRotation pr = RotationUtils.defaultPillar();
        assertEquals(0, pr.getY());
        assertEquals(4, pr.getX());
        assertEquals(8, pr.getZ());
    }

    @Test
    public void testButtonDefaults() {
        FourRotation fr = RotationUtils.defaultFour(true);
        assertEquals(4, fr.getMetas()[0]);
    }

    @Test
    public void testButtonRotation() throws Exception {
        int out = RotationUtils.rotateButton90(4);
        assertEquals(1, out);
    }
    @Test
    public void testStairsBigMetaReverseRotation() {
        int meta = 12;
        int result = RotationUtils.rotateMeta(RotationType.STAIRS, -1, meta);
        assertEquals("big meta stairs reverse", 15, result);
    }

    @Test
    public void testStairsBigMetaDoubleRotation() {
        int meta = 8;
        int result = RotationUtils.rotateMeta(RotationType.STAIRS, 2, meta);
        assertEquals("big meta stairs 180", 9, result);
    }

    @Test
    public void testFenceGateDefault() {
        FourRotation fr = RotationUtils.defaultFour(false);
        assertEquals(0, fr.getMetas()[0]);
        assertEquals(1, fr.getMetas()[1]);
    }

    @Test
    public void testFenceGateRotation() {
        int meta = 0;
        int result = RotationUtils.rotateMeta(RotationType.FENCE_GATE, 3, meta);
        assertEquals("gate rotated thrice", 3, result);
    }

    @Test
    public void testTrapdoorDoubleRotation() {
        int meta = 1;
        int result = RotationUtils.rotateMeta(RotationType.TRAP_DOOR, 2, meta);
        assertEquals("trapdoor 180", 0, result);
    }

    @Test
    public void testDoorNegativeRotation() {
        int meta = 1;
        int result = RotationUtils.rotateMeta(RotationType.DOOR, -1, meta);
        assertEquals("door reverse", 0, result);
    }
}
