/*  TallerTower: An RPG
Copyright (C) 2008-2010 Eric Ahnell

Any questions should be directed to the author via email at: TallerTower@worldwizard.net
 */
package com.puttysoftware.fantastlereboot.obsolete.maze2.objects;

import com.puttysoftware.fantastlereboot.assets.GameSound;
import com.puttysoftware.fantastlereboot.effects.EffectConstants;
import com.puttysoftware.fantastlereboot.loaders.SoundLoader;
import com.puttysoftware.fantastlereboot.obsolete.TallerTower;
import com.puttysoftware.fantastlereboot.obsolete.loaders2.ObjectImageConstants;
import com.puttysoftware.fantastlereboot.obsolete.maze2.abc.AbstractTrap;

public class ConfusionTrap extends AbstractTrap {
    // Constructors
    public ConfusionTrap() {
        super(ObjectImageConstants.OBJECT_IMAGE_CONFUSION_TRAP);
    }

    @Override
    public String getName() {
        return "Confusion Trap";
    }

    @Override
    public String getPluralName() {
        return "Confusion Traps";
    }

    @Override
    public void postMoveAction(final boolean ie, final int dirX, final int dirY) {
        TallerTower.getApplication().showMessage("You are confused!");
        TallerTower.getApplication().getGameManager()
                .activateEffect(EffectConstants.EFFECT_CONFUSED);
        SoundLoader.playSound(GameSound.CONFUSED);
    }

    @Override
    public String getDescription() {
        return "Confusion Traps randomly alter your controls for 6 steps when stepped on.";
    }
}