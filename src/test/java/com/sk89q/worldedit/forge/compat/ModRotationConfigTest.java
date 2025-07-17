package com.sk89q.worldedit.forge.compat;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

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
        assertTrue(f.exists());
    }
}
