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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.session.request.Request;

/**
 * A pattern that changes only the block ID, keeping the data value that is
 * already at the position. Used through the {@code #id[pattern]} syntax.
 *
 * <p>
 * For example {@code //replace spruce_stairs #id[134]} converts every
 * stair while preserving its orientation.
 * </p>
 *
 * <p>
 * The block already at the position is read from the given extent, or,
 * when none was supplied at parse time, from the {@link Request}'s current
 * {@link com.sk89q.worldedit.EditSession}. Because the new ID comes from the
 * pattern, any NBT data supplied by the pattern is kept while the previous
 * block's NBT data (which belongs to the old ID) is discarded.
 * </p>
 */
public class IdPattern extends AbstractPattern {

    @Nullable
    private final Extent extent;
    private final Pattern idSource;

    /**
     * Create a new instance resolving the current block through the
     * request's edit session.
     *
     * @param idSource the pattern supplying the new block ID
     */
    public IdPattern(Pattern idSource) {
        this(null, idSource);
    }

    /**
     * Create a new instance.
     *
     * @param extent   the extent to read existing blocks from, or null to use
     *                 the request's edit session
     * @param idSource the pattern supplying the new block ID
     */
    public IdPattern(@Nullable Extent extent, Pattern idSource) {
        checkNotNull(idSource);
        this.extent = extent;
        this.idSource = idSource;
    }

    private Extent getExtent() {
        if (extent != null) {
            return extent;
        }
        Extent requestExtent = Request.request()
            .getEditSession();
        if (requestExtent == null) {
            throw new IllegalStateException("#id pattern has no extent to read existing blocks from");
        }
        return requestExtent;
    }

    @Override
    public BaseBlock apply(Vector position) {
        BaseBlock existing = getExtent().getBlock(position);
        BaseBlock next = idSource.apply(position);
        return new BaseBlock(next.getId(), existing.getData(), next.getNbtData());
    }

}
