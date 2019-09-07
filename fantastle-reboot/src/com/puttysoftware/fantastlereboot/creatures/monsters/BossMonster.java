/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.fantastlereboot.creatures.monsters;

import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.PreferencesManager;
import com.puttysoftware.fantastlereboot.ai.map.MapAIRoutinePicker;
import com.puttysoftware.fantastlereboot.ai.window.AbstractWindowAIRoutine;
import com.puttysoftware.fantastlereboot.ai.window.VeryHardWindowAIRoutine;
import com.puttysoftware.fantastlereboot.creatures.AbstractCreature;
import com.puttysoftware.fantastlereboot.creatures.faiths.Faith;
import com.puttysoftware.fantastlereboot.creatures.faiths.FaithManager;
import com.puttysoftware.fantastlereboot.creatures.party.PartyManager;
import com.puttysoftware.fantastlereboot.loaders.older.BossImageManager;
import com.puttysoftware.fantastlereboot.ttspells.SpellBook;
import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.randomrange.RandomRange;

public class BossMonster extends AbstractCreature {
    // Fields
    private static final int MINIMUM_STAT_VALUE_VERY_EASY = 100;
    private static final int MINIMUM_STAT_VALUE_EASY = 200;
    private static final int MINIMUM_STAT_VALUE_NORMAL = 400;
    private static final int MINIMUM_STAT_VALUE_HARD = 600;
    private static final int MINIMUM_STAT_VALUE_VERY_HARD = 900;
    private static final int STAT_MULT_VERY_EASY = 3;
    private static final int STAT_MULT_EASY = 4;
    private static final int STAT_MULT_NORMAL = 5;
    private static final int STAT_MULT_HARD = 8;
    private static final int STAT_MULT_VERY_HARD = 12;

    // Constructors
    BossMonster() {
        super(true, 1);
        this.setWindowAI(BossMonster.getInitialWindowAI());
        this.setMapAI(MapAIRoutinePicker.getNextRoutine());
        final SpellBook spells = new SystemMonsterSpellBook();
        spells.learnAllSpells();
        this.setSpellBook(spells);
        this.loadCreature();
    }

    // Methods
    @Override
    public String getFightingWhatString() {
        return "You're fighting The Boss";
    }

    @Override
    public String getName() {
        return "The Boss";
    }

    @Override
    public Faith getFaith() {
        return FaithManager.getFaith(0);
    }

    @Override
    public boolean checkLevelUp() {
        return false;
    }

    @Override
    protected void levelUpHook() {
        // Do nothing
    }

    @Override
    protected BufferedImageIcon getInitialImage() {
        return BossImageManager.getBossImage();
    }

    @Override
    public int getSpeed() {
        final int difficulty = FantastleReboot.getBagOStuff().getPrefsManager().getGameDifficulty();
        final int base = this.getBaseSpeed();
        FantastleReboot.getBagOStuff().getPrefsManager();
        if (difficulty == PreferencesManager.DIFFICULTY_VERY_EASY) {
            return (int) (base * SPEED_ADJUST_SLOWEST);
        } else {
            FantastleReboot.getBagOStuff().getPrefsManager();
            if (difficulty == PreferencesManager.DIFFICULTY_EASY) {
                return (int) (base * SPEED_ADJUST_SLOW);
            } else {
                FantastleReboot.getBagOStuff().getPrefsManager();
                if (difficulty == PreferencesManager.DIFFICULTY_NORMAL) {
                    return (int) (base * SPEED_ADJUST_NORMAL);
                } else {
                    FantastleReboot.getBagOStuff().getPrefsManager();
                    if (difficulty == PreferencesManager.DIFFICULTY_HARD) {
                        return (int) (base * SPEED_ADJUST_FAST);
                    } else {
                        FantastleReboot.getBagOStuff().getPrefsManager();
                        if (difficulty == PreferencesManager.DIFFICULTY_VERY_HARD) {
                            return (int) (base * SPEED_ADJUST_FASTEST);
                        } else {
                            return (int) (base * SPEED_ADJUST_NORMAL);
                        }
                    }
                }
            }
        }
    }

    // Helper Methods
    @Override
    public void loadCreature() {
        final int newLevel = PartyManager.getParty().getTowerLevel() + 6;
        this.setLevel(newLevel);
        this.setVitality(this.getInitialVitality());
        this.setCurrentHP(this.getMaximumHP());
        this.setIntelligence(this.getInitialIntelligence());
        this.setCurrentMP(this.getMaximumMP());
        this.setStrength(this.getInitialStrength());
        this.setBlock(this.getInitialBlock());
        this.setAgility(this.getInitialAgility());
        this.setLuck(this.getInitialLuck());
        this.setGold(0);
        this.setExperience(0);
        this.setAttacksPerRound(1);
        this.setSpellsPerRound(1);
        this.image = this.getInitialImage();
    }

    private int getInitialStrength() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private int getInitialBlock() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private int getInitialAgility() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private int getInitialVitality() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private int getInitialIntelligence() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private int getInitialLuck() {
        final int min = getMinimumStatForDifficulty();
        final RandomRange r = new RandomRange(min, Math.max(this.getLevel()
                * getStatMultiplierForDifficulty(), min));
        return r.generate();
    }

    private static AbstractWindowAIRoutine getInitialWindowAI() {
        return new VeryHardWindowAIRoutine();
    }

    private static int getStatMultiplierForDifficulty() {
        final int difficulty = FantastleReboot.getBagOStuff().getPrefsManager().getGameDifficulty();
        FantastleReboot.getBagOStuff().getPrefsManager();
        if (difficulty == PreferencesManager.DIFFICULTY_VERY_EASY) {
            return STAT_MULT_VERY_EASY;
        } else {
            FantastleReboot.getBagOStuff().getPrefsManager();
            if (difficulty == PreferencesManager.DIFFICULTY_EASY) {
                return STAT_MULT_EASY;
            } else {
                FantastleReboot.getBagOStuff().getPrefsManager();
                if (difficulty == PreferencesManager.DIFFICULTY_NORMAL) {
                    return STAT_MULT_NORMAL;
                } else {
                    FantastleReboot.getBagOStuff().getPrefsManager();
                    if (difficulty == PreferencesManager.DIFFICULTY_HARD) {
                        return STAT_MULT_HARD;
                    } else {
                        FantastleReboot.getBagOStuff().getPrefsManager();
                        if (difficulty == PreferencesManager.DIFFICULTY_VERY_HARD) {
                            return STAT_MULT_VERY_HARD;
                        } else {
                            return STAT_MULT_NORMAL;
                        }
                    }
                }
            }
        }
    }

    private static int getMinimumStatForDifficulty() {
        final int difficulty = FantastleReboot.getBagOStuff().getPrefsManager().getGameDifficulty();
        FantastleReboot.getBagOStuff().getPrefsManager();
        if (difficulty == PreferencesManager.DIFFICULTY_VERY_EASY) {
            return MINIMUM_STAT_VALUE_VERY_EASY;
        } else {
            FantastleReboot.getBagOStuff().getPrefsManager();
            if (difficulty == PreferencesManager.DIFFICULTY_EASY) {
                return MINIMUM_STAT_VALUE_EASY;
            } else {
                FantastleReboot.getBagOStuff().getPrefsManager();
                if (difficulty == PreferencesManager.DIFFICULTY_NORMAL) {
                    return MINIMUM_STAT_VALUE_NORMAL;
                } else {
                    FantastleReboot.getBagOStuff().getPrefsManager();
                    if (difficulty == PreferencesManager.DIFFICULTY_HARD) {
                        return MINIMUM_STAT_VALUE_HARD;
                    } else {
                        FantastleReboot.getBagOStuff().getPrefsManager();
                        if (difficulty == PreferencesManager.DIFFICULTY_VERY_HARD) {
                            return MINIMUM_STAT_VALUE_VERY_HARD;
                        } else {
                            return MINIMUM_STAT_VALUE_NORMAL;
                        }
                    }
                }
            }
        }
    }
}
