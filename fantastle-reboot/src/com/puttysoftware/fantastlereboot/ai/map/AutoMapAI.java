/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.fantastlereboot.ai.map;

import java.awt.Point;

import com.puttysoftware.fantastlereboot.ai.AIContext;
import com.puttysoftware.fantastlereboot.ai.AIRoutine;
import com.puttysoftware.fantastlereboot.creatures.Creature;

public class AutoMapAI extends AbstractMapAIRoutine {
    // Constructor
    public AutoMapAI() {
        super();
    }

    @Override
    public int getNextAction(final Creature c, final AIContext ac) {
        final Point there = ac.isEnemyNearby();
        if (there != null) {
            // Something hostile is nearby, so attack it
            this.moveX = there.x;
            this.moveY = there.y;
            return AIRoutine.ACTION_MOVE;
        } else {
            return AbstractMapAIRoutine.ACTION_END_TURN;
        }
    }
}
