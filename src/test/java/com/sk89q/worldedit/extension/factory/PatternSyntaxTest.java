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

package com.sk89q.worldedit.extension.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;

/**
 * End to end tests for the {@code ^pattern}, {@code *} and {@code id:*}
 * syntax through the real pattern and block factories.
 */
public class PatternSyntaxTest {

    private static final Vector POS = new Vector(1, 1, 1);

    private WorldEdit worldEdit;
    private BlockArrayClipboard extent;
    private ParserContext context;
    private int previousMaxId;
    private int previousMaxData;

    @Before
    public void setUp() {
        // Simulate the NotEnoughIDs cap raise applied by ForgeWorldEdit
        previousMaxId = BaseBlock.MAX_ID;
        previousMaxData = BaseBlock.MAX_DATA;
        BaseBlock.MAX_ID = Short.MAX_VALUE;
        BaseBlock.MAX_DATA = (1 << 16) - 1;
        worldEdit = WorldEdit.getInstance();
        extent = new BlockArrayClipboard(new CuboidRegion(new Vector(0, 0, 0), new Vector(2, 2, 2)));

        World world = mock(World.class);
        when(world.isValidBlockType(anyInt())).thenReturn(true);

        Actor actor = mock(Actor.class);
        when(actor.hasPermission(anyString())).thenReturn(true);

        context = new ParserContext();
        context.setWorld(world);
        context.setExtent(extent);
        context.setActor(actor);
    }

    @After
    public void tearDown() {
        BaseBlock.MAX_ID = previousMaxId;
        BaseBlock.MAX_DATA = previousMaxData;
    }

    private ParserContext maskContext() {
        ParserContext maskContext = new ParserContext(context);
        maskContext.setRestricted(false);
        maskContext.setPreferringWildcard(true);
        return maskContext;
    }

    @Test
    public void caretKeepsExistingData() throws Exception {
        extent.setBlock(POS, new BaseBlock(53, 3));

        Pattern pattern = worldEdit.getPatternFactory()
            .parseFromInput("^164", context);
        BaseBlock result = pattern.apply(POS);

        assertEquals(164, result.getId());
        assertEquals("data preserved by ^", 3, result.getData());
    }

    @Test
    public void caretAcceptsBracketedForm() throws Exception {
        extent.setBlock(POS, new BaseBlock(53, 2));

        Pattern pattern = worldEdit.getPatternFactory()
            .parseFromInput("^[164]", context);
        BaseBlock result = pattern.apply(POS);

        assertEquals(164, result.getId());
        assertEquals(2, result.getData());
    }

    @Test
    public void starKeepsExistingBlock() throws Exception {
        extent.setBlock(POS, new BaseBlock(53, 3));

        assertEquals(
            53,
            worldEdit.getPatternFactory()
                .parseFromInput("*", context)
                .apply(POS)
                .getId());
        assertEquals(
            3,
            worldEdit.getPatternFactory()
                .parseFromInput("#existing", context)
                .apply(POS)
                .getData());
    }

    @Test
    public void weightedListsAcceptCaretAndStar() throws Exception {
        Pattern pattern = worldEdit.getPatternFactory()
            .parseFromInput("50%^164,50%*", context);
        assertTrue("weighted list parses to a random pattern", pattern instanceof RandomPattern);
    }

    @Test
    public void wildcardDataParsesWhereBlocksAreMatched() throws Exception {
        BaseBlock block = worldEdit.getBlockFactory()
            .parseFromInput("53:*", maskContext());

        assertEquals(53, block.getId());
        assertEquals("explicit * data is the fuzzy wildcard", -1, block.getData());
    }

    @Test(expected = InputParseException.class)
    public void wildcardDataRejectedInPatternPosition() throws Exception {
        worldEdit.getBlockFactory()
            .parseFromInput("53:*", context);
    }

    @Test
    public void extendedDataValuesAccepted() throws Exception {
        // NotEnoughIDs AddData metas go past 15, the old parser rejected them
        BaseBlock block = worldEdit.getBlockFactory()
            .parseFromInput("100:300", maskContext());

        assertEquals(100, block.getId());
        assertEquals(300, block.getData());
    }

}
