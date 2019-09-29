/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.fantastlereboot.game;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.puttysoftware.fantastlereboot.assets.GameEffectImage;
import com.puttysoftware.fantastlereboot.creatures.party.Party;
import com.puttysoftware.fantastlereboot.creatures.party.PartyManager;
import com.puttysoftware.fantastlereboot.creatures.party.PartyMember;
import com.puttysoftware.fantastlereboot.loaders.EffectImageLoader;
import com.puttysoftware.images.BufferedImageIcon;

class StatGUI {
    // Fields
    private Container statsPane;
    private JLabel hpLabel;
    private JLabel mpLabel;
    private JLabel goldLabel;
    private JLabel attackLabel;
    private JLabel defenseLabel;
    private JLabel xpLabel;
    private JLabel levelLabel;

    // Constructors
    StatGUI() {
        this.setUpGUI();
    }

    // Methods
    Container getStatsPane() {
        return this.statsPane;
    }

    void updateStats() {
        final Party party = PartyManager.getParty();
        if (party != null) {
            final PartyMember pc = party.getLeader();
            if (pc != null) {
                this.hpLabel.setText(pc.getHPString());
                this.mpLabel.setText(pc.getMPString());
                this.goldLabel.setText(Integer.toString(pc.getGold()));
                this.attackLabel.setText(Integer.toString(pc.getAttack()));
                this.defenseLabel.setText(Integer.toString(pc.getDefense()));
                this.xpLabel.setText(pc.getXPString());
                this.levelLabel.setText(party.getMonsterLevelString());
            }
        }
    }

    private void setUpGUI() {
        this.statsPane = new Container();
        this.statsPane.setLayout(new GridLayout(7, 1));
        this.hpLabel = new JLabel("", null, SwingConstants.LEFT);
        this.mpLabel = new JLabel("", null, SwingConstants.LEFT);
        this.goldLabel = new JLabel("", null, SwingConstants.LEFT);
        this.attackLabel = new JLabel("", null, SwingConstants.LEFT);
        this.defenseLabel = new JLabel("", null, SwingConstants.LEFT);
        this.xpLabel = new JLabel("", null, SwingConstants.LEFT);
        this.levelLabel = new JLabel("", null, SwingConstants.LEFT);
        this.statsPane.add(this.hpLabel);
        this.statsPane.add(this.mpLabel);
        this.statsPane.add(this.goldLabel);
        this.statsPane.add(this.attackLabel);
        this.statsPane.add(this.defenseLabel);
        this.statsPane.add(this.xpLabel);
        this.statsPane.add(this.levelLabel);
    }

    void updateImages() {
        final BufferedImageIcon hpImage = EffectImageLoader
                .load(GameEffectImage.HEALTH);
        this.hpLabel.setIcon(hpImage);
        final BufferedImageIcon mpImage = EffectImageLoader
                .load(GameEffectImage.MAGIC);
        this.mpLabel.setIcon(mpImage);
        final BufferedImageIcon goldImage = EffectImageLoader
                .load(GameEffectImage.MONEY);
        this.goldLabel.setIcon(goldImage);
        final BufferedImageIcon attackImage = EffectImageLoader
                .load(GameEffectImage.MELEE_ATTACK);
        this.attackLabel.setIcon(attackImage);
        final BufferedImageIcon defenseImage = EffectImageLoader
                .load(GameEffectImage.DEFENSE);
        this.defenseLabel.setIcon(defenseImage);
        final BufferedImageIcon xpImage = EffectImageLoader
                .load(GameEffectImage.EXPERIENCE);
        this.xpLabel.setIcon(xpImage);
        final BufferedImageIcon levelImage = EffectImageLoader
                .load(GameEffectImage.CREATURE_LEVEL);
        this.levelLabel.setIcon(levelImage);
    }
}
