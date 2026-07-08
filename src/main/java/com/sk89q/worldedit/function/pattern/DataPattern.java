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
 * A pattern that changes only the data value, keeping the block ID that is
 * already at the position. Used through the {@code #data[pattern]} syntax.
 *
 * <p>
 * The block already at the position is read from the given extent, or,
 * when none was supplied at parse time, from the {@link Request}'s current
 * {@link com.sk89q.worldedit.EditSession}. Because the ID does not change,
 * the previous block's NBT data (chest contents, sign text, ...) is kept.
 * </p>
 */
public class DataPattern extends AbstractPattern {

    @Nullable
    private final Extent extent;
    private final Pattern dataSource;

    /**
     * Create a new instance resolving the current block through the
     * request's edit session.
     *
     * @param dataSource the pattern supplying the new data value
     */
    public DataPattern(Pattern dataSource) {
        this(null, dataSource);
    }

    /**
     * Create a new instance.
     *
     * @param extent     the extent to read existing blocks from, or null to use
     *                   the request's edit session
     * @param dataSource the pattern supplying the new data value
     */
    public DataPattern(@Nullable Extent extent, Pattern dataSource) {
        checkNotNull(dataSource);
        this.extent = extent;
        this.dataSource = dataSource;
    }

    private Extent getExtent() {
        if (extent != null) {
            return extent;
        }
        Extent requestExtent = Request.request()
            .getEditSession();
        if (requestExtent == null) {
            throw new IllegalStateException("#data pattern has no extent to read existing blocks from");
        }
        return requestExtent;
    }

    @Override
    public BaseBlock apply(Vector position) {
        BaseBlock existing = getExtent().getBlock(position);
        BaseBlock next = dataSource.apply(position);
        return new BaseBlock(existing.getId(), next.getData(), existing.getNbtData());
    }

}
