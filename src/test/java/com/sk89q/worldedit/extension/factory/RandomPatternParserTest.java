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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the weighted list tokenizing in {@link RandomPatternParser}.
 */
public class RandomPatternParserTest {

    private static void assertSplit(String input, String... expected) {
        List<String> actual = RandomPatternParser.splitTopLevel(input);
        assertEquals("split of '" + input + "'", Arrays.asList(expected), actual);
    }

    @Test
    public void splitsPlainLists() {
        assertSplit("stone", "stone");
        assertSplit("stone,dirt", "stone", "dirt");
        assertSplit("70%stone,30%dirt", "70%stone", "30%dirt");
    }

    @Test
    public void keepsBracketedCommasIntact() {
        assertSplit("#id[a,b],c", "#id[a,b]", "c");
        assertSplit("70%#id[70%a,30%b],30%stone", "70%#id[70%a,30%b]", "30%stone");
        assertSplit("#data[#id[a,b],c]", "#data[#id[a,b],c]");
    }

    @Test
    public void handlesEdgeShapes() {
        assertSplit("", "");
        assertSplit("a,", "a", "");
        assertSplit(",a", "", "a");
        // Unbalanced closing brackets do not underflow the depth counter
        assertSplit("a],b", "a]", "b");
    }

    @Test
    public void weightPrefixDetection() {
        assertTrue(RandomPatternParser.hasWeightPrefix("70%stone"));
        assertTrue(RandomPatternParser.hasWeightPrefix("0.5%stone"));
        assertTrue(RandomPatternParser.hasWeightPrefix("70%#id[stone]"));
        assertFalse(RandomPatternParser.hasWeightPrefix("stone"));
        assertFalse(RandomPatternParser.hasWeightPrefix("#id[70%stone]"));
        assertFalse(RandomPatternParser.hasWeightPrefix("%stone"));
    }

}
