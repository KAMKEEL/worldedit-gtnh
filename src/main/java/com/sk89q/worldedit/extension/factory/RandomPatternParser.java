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

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.internal.registry.InputParser;

/**
 * Parses comma separated weighted pattern lists such as
 * {@code 70%stone,25%andesite,5%mossy_cobblestone}. Every entry may itself be
 * any pattern, so nested forms like {@code 70%#id[134],30%stone} work, and
 * commas inside brackets do not split the list.
 */
class RandomPatternParser extends InputParser<Pattern> {

    RandomPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        List<String> tokens = splitTopLevel(input);

        // Single entries without a weight belong to the other parsers
        if (tokens.size() == 1 && !hasWeightPrefix(tokens.get(0))) {
            return null;
        }

        RandomPattern randomPattern = new RandomPattern();

        for (String token : tokens) {
            if (token.isEmpty()) {
                throw new InputParseException("Empty entry in the pattern list '" + input + "'");
            }

            double chance = 1;
            String part = token;

            if (hasWeightPrefix(token)) {
                int percent = token.indexOf('%');
                if (percent == token.length() - 1) {
                    throw new InputParseException("Missing the pattern after the % symbol for '" + input + "'");
                }
                chance = Double.parseDouble(token.substring(0, percent));
                part = token.substring(percent + 1);
            }

            randomPattern.add(
                worldEdit.getPatternFactory()
                    .parseFromInput(part, context),
                chance);
        }

        return randomPattern;
    }

    /**
     * Whether the token starts with a numeric weight prefix such as {@code 70%}.
     */
    static boolean hasWeightPrefix(String token) {
        return token.matches("[0-9]+(\\.[0-9]*)?%.*");
    }

    /**
     * Split on commas that are not inside square brackets, so bracketed inner
     * patterns like {@code #id[a,b]} stay intact.
     */
    static List<String> splitTopLevel(String input) {
        List<String> parts = new ArrayList<String>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth = Math.max(0, depth - 1);
            } else if (c == ',' && depth == 0) {
                parts.add(input.substring(start, i));
                start = i + 1;
            }
        }

        parts.add(input.substring(start));
        return parts;
    }

}
