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
package com.puttysoftware.fantastlereboot.obsolete.maze1.objects;

import com.puttysoftware.fantastlereboot.BagOStuff;
import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.PreferencesManager;
import com.puttysoftware.fantastlereboot.game.ObjectInventory;
import com.puttysoftware.fantastlereboot.obsolete.maze1.Maze;
import com.puttysoftware.fantastlereboot.obsolete.maze1.generic.GenericMovableObject;
import com.puttysoftware.fantastlereboot.obsolete.maze1.generic.MazeObject;

public class PushableBlockOnce extends GenericMovableObject {
    // Constructors
    public PushableBlockOnce() {
        super(true, false);
    }

    @Override
    public String getName() {
        return "Pushable Block Once";
    }

    @Override
    public String getPluralName() {
        return "Pushable Blocks Once";
    }

    @Override
    public void pushAction(final ObjectInventory inv, final MazeObject mo,
            final int x, final int y, final int pushX, final int pushY) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        app.getGameManager().updatePushedPosition(x, y, pushX, pushY, this);
        FantastleReboot.getBagOStuff().getPrefsManager();
        if (app.getPrefsManager()
                .getSoundEnabled(PreferencesManager.SOUNDS_GAME)) {
            MazeObject.playPushSuccessSound();
        }
        app.getGameManager().morphOther(new Wall(), pushX, pushY,
                Maze.LAYER_OBJECT);
    }

    @Override
    public byte getObjectID() {
        return (byte) 4;
    }

    @Override
    public String getDescription() {
        return "Pushable Blocks Once can only be pushed once, before turning into a wall.";
    }
}