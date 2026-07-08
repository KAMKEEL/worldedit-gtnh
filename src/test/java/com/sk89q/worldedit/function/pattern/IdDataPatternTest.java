/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.request.Request;

/**
 * Tests for {@link IdPattern} and {@link DataPattern}.
 */
public class IdDataPatternTest {

    private static final Vector POS = new Vector(1, 1, 1);

    private BlockArrayClipboard extent;

    @Before
    public void setUp() {
        extent = new BlockArrayClipboard(new CuboidRegion(new Vector(0, 0, 0), new Vector(2, 2, 2)));
        Request.reset();
    }

    private static CompoundTag tag(String marker) {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("marker", new StringTag(marker));
        return new CompoundTag(values);
    }

    @Test
    public void idPatternKeepsExistingData() throws Exception {
        // Oak stairs facing north (53:3) re-skinned to dark oak stairs (164)
        extent.setBlock(POS, new BaseBlock(53, 3));

        IdPattern pattern = new IdPattern(extent, new BlockPattern(new BaseBlock(164, 0)));
        BaseBlock result = pattern.apply(POS);

        assertEquals("new ID from the pattern", 164, result.getId());
        assertEquals("data preserved from the world", 3, result.getData());
    }

    @Test
    public void idPatternWorksAboveVanillaIdRange() throws Exception {
        // NotEnoughIDs mod stairs keep orientation too
        extent.setBlock(POS, new BaseBlock(134, 6));

        IdPattern pattern = new IdPattern(extent, new BlockPattern(new BaseBlock(16494, 0)));
        BaseBlock result = pattern.apply(POS);

        assertEquals(16494, result.getId());
        assertEquals(6, result.getData());
    }

    @Test
    public void idPatternDropsOldNbtAndCarriesPatternNbt() throws Exception {
        // The old block's NBT belongs to the old ID and must not survive,
        // NBT supplied by the pattern itself is kept
        extent.setBlock(POS, new BaseBlock(54, 2, tag("old-chest")));

        IdPattern plain = new IdPattern(extent, new BlockPattern(new BaseBlock(146, 0)));
        assertNull(
            "old NBT dropped on ID change",
            plain.apply(POS)
                .getNbtData());

        IdPattern withNbt = new IdPattern(extent, new BlockPattern(new BaseBlock(146, 0, tag("new-chest"))));
        BaseBlock result = withNbt.apply(POS);
        assertNotNull(result.getNbtData());
        assertEquals(
            "new-chest",
            ((StringTag) result.getNbtData()
                .getValue()
                .get("marker")).getValue());
    }

    @Test
    public void dataPatternKeepsExistingId() throws Exception {
        extent.setBlock(POS, new BaseBlock(53, 3));

        DataPattern pattern = new DataPattern(extent, new BlockPattern(new BaseBlock(1, 7)));
        BaseBlock result = pattern.apply(POS);

        assertEquals("ID preserved from the world", 53, result.getId());
        assertEquals("new data from the pattern", 7, result.getData());
    }

    @Test
    public void dataPatternPreservesNbt() throws Exception {
        // Changing only the data value of a chest must keep its contents.
        // (The KAWE reference implementation drops NBT here, that is a bug
        // we deliberately do not inherit.)
        extent.setBlock(POS, new BaseBlock(54, 2, tag("chest-contents")));

        DataPattern pattern = new DataPattern(extent, new BlockPattern(new BaseBlock(1, 5)));
        BaseBlock result = pattern.apply(POS);

        assertEquals(54, result.getId());
        assertEquals(5, result.getData());
        assertNotNull("NBT preserved when only data changes", result.getNbtData());
        assertEquals(
            "chest-contents",
            ((StringTag) result.getNbtData()
                .getValue()
                .get("marker")).getValue());
    }

    @Test(expected = IllegalStateException.class)
    public void idPatternWithoutExtentOrRequestThrows() {
        new IdPattern(new BlockPattern(new BaseBlock(1, 0))).apply(POS);
    }

    @Test(expected = IllegalStateException.class)
    public void dataPatternWithoutExtentOrRequestThrows() {
        new DataPattern(new BlockPattern(new BaseBlock(1, 0))).apply(POS);
    }

}
