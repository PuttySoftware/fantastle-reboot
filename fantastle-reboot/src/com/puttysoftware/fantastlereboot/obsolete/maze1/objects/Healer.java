package com.puttysoftware.fantastlereboot.obsolete.maze1.objects;

import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.obsolete.maze1.generic.GenericDungeonObject;

public class Healer extends GenericDungeonObject {
    // Constructors
    public Healer() {
        super(false);
    }

    @Override
    public void postMoveActionHook() {
        FantastleReboot.getBagOStuff().getHealer().showShop();
    }

    @Override
    public String getName() {
        return "Healer";
    }

    @Override
    public String getPluralName() {
        return "Healers";
    }

    @Override
    public String getDescription() {
        return "Healers restore health, for a fee.";
    }

    @Override
    public byte getObjectID() {
        return (byte) 3;
    }
}
