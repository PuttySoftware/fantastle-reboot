/*  Fantastle: A Maze-Solving Game
Copyright (C) 2008-2010 Eric Ahnell

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Any questions should be directed to the author via email at: fantastle@worldwizard.net
 */
package com.puttysoftware.fantastlereboot.obsolete.maze1.generic;

import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.Messager;
import com.puttysoftware.fantastlereboot.utilities.ArrowTypeConstants;
import com.puttysoftware.fantastlereboot.utilities.TypeConstants;

public abstract class GenericBow extends GenericUsableObject {
    // Constructors
    protected GenericBow(final int uses) {
        super(uses);
    }

    @Override
    public abstract String getName();

    @Override
    public void useAction(final MazeObject mo, final int x, final int y,
            final int z, final int w) {
        FantastleReboot.getBagOStuff().getGameManager()
                .setArrowType(ArrowTypeConstants.ARROW_TYPE_ICE);
        FantastleReboot.getBagOStuff().getGameManager().keepNextMessage();
        Messager.showMessage(this.getName() + " activated.");
    }

    @Override
    public void useHelper(final int x, final int y, final int z, final int w) {
        this.useAction(null, x, y, z, w);
    }

    @Override
    public byte getGroupID() {
        return (byte) 0;
    }

    @Override
    public int getCustomProperty(final int propID) {
        return MazeObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
        // Do nothing
    }

    @Override
    protected void setTypes() {
        this.type.set(TypeConstants.TYPE_BOW);
        this.type.set(TypeConstants.TYPE_USABLE);
        this.type.set(TypeConstants.TYPE_INVENTORYABLE);
        this.type.set(TypeConstants.TYPE_CONTAINABLE);
    }
}