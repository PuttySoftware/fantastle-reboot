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
package com.puttysoftware.fantastlereboot.game;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.puttysoftware.fantastlereboot.BagOStuff;
import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.Messager;
import com.puttysoftware.fantastlereboot.PreferencesManager;
import com.puttysoftware.fantastlereboot.assets.GameSound;
import com.puttysoftware.fantastlereboot.creatures.party.PartyManager;
import com.puttysoftware.fantastlereboot.effects.EffectManager;
import com.puttysoftware.fantastlereboot.loaders.SoundPlayer;
import com.puttysoftware.fantastlereboot.maze.abc.AbstractMazeObject;
import com.puttysoftware.fantastlereboot.maze.Maze;
import com.puttysoftware.fantastlereboot.maze.MazeManager;
import com.puttysoftware.fantastlereboot.utilities.MazeObjectList;
import com.puttysoftware.fantastlereboot.utilities.ArrowTypeConstants;
import com.puttysoftware.fantastlereboot.utilities.ImageConstants;
import com.puttysoftware.fantastlereboot.utilities.TypeConstants;
import com.puttysoftware.xio.XDataReader;
import com.puttysoftware.xio.XDataWriter;



public class GameManager {
    // Fields
    private JFrame outputFrame;
    private Container outputPane, borderPane;
    private JLabel messageLabel;
    private AbstractMazeObject savedAbstractMazeObject, objectBeingUsed;
    private EventHandler handler;
    private ObjectInventory objectInv, savedObjectInv;
    private boolean pullInProgress;
    private boolean using;
    private int lastUsedObjectIndex;
    private boolean knm;
    private boolean savedGameFlag;
    private int activeArrowType;
    private final PlayerLocationManager plMgr;
    private final GameViewingWindowManager vwMgr;
    private final ScoreTracker st;
    private final StatGUI sg;
    private final EffectManager em;
    private JLabel[][] drawGrid;
    private boolean delayedDecayActive;
    private AbstractMazeObject delayedDecayObject;
    private boolean actingRemotely;
    boolean arrowActive;
    boolean teleporting;
    private int[] remoteCoords;
    private String gameOverMessage;

    // Constructors
    public GameManager() {
        this.plMgr = new PlayerLocationManager();
        this.vwMgr = new GameViewingWindowManager();
        this.st = new ScoreTracker();
        this.sg = new StatGUI();
        this.em = new EffectManager();
        this.setUpGUI();
        this.setPullInProgress(false);
        this.setUsingAnItem(false);
        this.savedAbstractMazeObject = new Empty();
        this.lastUsedObjectIndex = 0;
        this.knm = false;
        this.savedGameFlag = false;
        this.delayedDecayActive = false;
        this.delayedDecayObject = null;
        this.actingRemotely = false;
        this.remoteCoords = new int[4];
        this.arrowActive = false;
        this.teleporting = false;
        this.activeArrowType = ArrowTypeConstants.ARROW_TYPE_PLAIN;
    }

    // Methods
    public boolean newGame() {
        if (this.savedGameFlag) {
            return true;
        } else {
            return PartyManager.createParty(
                    FantastleReboot.getBagOStuff().getOutputFrame());
        }
    }

    public PlayerLocationManager getPlayerManager() {
        return this.plMgr;
    }

    public void updateStatGUI() {
        this.sg.updateImages();
    }

    public StatGUI getStatGUI() {
        return this.sg;
    }

    public ScoreTracker getScoreTracker() {
        return this.st;
    }

    public GameViewingWindowManager getViewManager() {
        return this.vwMgr;
    }

    public JLabel getMessageLabel() {
        return this.messageLabel;
    }

    public void setArrowType(final int type) {
        this.activeArrowType = type;
    }

    void arrowDone() {
        this.arrowActive = false;
        this.activeArrowType = ArrowTypeConstants.ARROW_TYPE_PLAIN;
    }

    public AbstractMazeObject getSavedAbstractMazeObject() {
        return this.savedAbstractMazeObject;
    }

    public void setSavedAbstractMazeObject(final AbstractMazeObject newSavedObject) {
        this.savedAbstractMazeObject = newSavedObject;
    }

    public boolean isFloorBelow() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ() - 1,
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public boolean isFloorAbove() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ() + 1,
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public boolean isLevelBelow() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW() - 1, Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public boolean isLevelAbove() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW() + 1, Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public boolean doesFloorExist(final int floor) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(), floor,
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public boolean doesLevelExist(final int level) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(), level, Maze.LAYER_OBJECT);
            return true;
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        } catch (final NullPointerException np) {
            return false;
        }
    }

    public void resetObjectInventory() {
        this.objectInv = new ObjectInventory();
    }

    public void setSavedGameFlag(final boolean value) {
        this.savedGameFlag = value;
    }

    private void saveObjectInventory() {
        this.savedObjectInv = this.objectInv.clone();
    }

    private void restoreObjectInventory() {
        if (this.savedObjectInv != null) {
            this.objectInv = this.savedObjectInv.clone();
        } else {
            this.objectInv = new ObjectInventory();
        }
    }

    public boolean usingAnItem() {
        return this.using;
    }

    public boolean isTeleporting() {
        return this.teleporting;
    }

    public void setUsingAnItem(final boolean isUsing) {
        this.using = isUsing;
    }

    public void setPullInProgress(final boolean pulling) {
        this.pullInProgress = pulling;
    }

    public boolean isPullInProgress() {
        return this.pullInProgress;
    }

    public void setStatusMessage(final String msg) {
        this.messageLabel.setText(msg);
    }

    private void decayEffects() {
        this.em.decayEffects();
    }

    public void activateEffect(final int effectID, final int duration) {
        this.em.activateEffect(effectID, duration);
    }

    private void deactivateAllEffects() {
        this.em.deactivateAllEffects();
    }

    int[] doEffects(final int x, final int y) {
        return this.em.doEffects(x, y);
    }

    public void setRemoteAction(final int x, final int y, final int z,
            final int w) {
        this.remoteCoords = new int[] { x, y, z, w };
        this.actingRemotely = true;
    }

    public void doRemoteAction(final int x, final int y, final int z,
            final int w) {
        this.setRemoteAction(x, y, z, w);
        final AbstractMazeObject acted = FantastleReboot.getBagOStuff().getMazeManager()
                .getAbstractMazeObject(x, y, z, w, Maze.LAYER_OBJECT);
        acted.postMoveAction(false, x, y, this.objectInv);
        if (acted.doesChainReact()) {
            acted.chainReactionAction(x, y, z, w);
        }
    }

    public void doClockwiseRotate(final int r) {
        final Maze m = FantastleReboot.getBagOStuff().getMazeManager()
                .getMaze();
        boolean b = false;
        if (this.actingRemotely) {
            b = m.rotateRadiusClockwise(this.remoteCoords[0],
                    this.remoteCoords[1], this.remoteCoords[2],
                    this.remoteCoords[3], r);
        } else {
            b = m.rotateRadiusClockwise(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), r);
        }
        if (b) {
            this.findPlayerAndAdjust();
        } else {
            this.keepNextMessage();
            Messager.showMessage("Rotation failed!");
        }
    }

    public void findPlayerAndAdjust() {
        // Find the player, adjust player location
        final Maze m = FantastleReboot.getBagOStuff().getMazeManager()
                .getMaze();
        final int w = this.plMgr.getPlayerLocationW();
        m.findPlayerOnLevel(w);
        this.plMgr.setPlayerLocation(m.getFindResultColumn(w),
                m.getFindResultRow(w), m.getFindResultFloor(w), w);
        this.resetViewingWindow();
        this.redrawMaze();
    }

    public void doCounterclockwiseRotate(final int r) {
        final Maze m = FantastleReboot.getBagOStuff().getMazeManager()
                .getMaze();
        boolean b = false;
        if (this.actingRemotely) {
            b = m.rotateRadiusCounterclockwise(this.remoteCoords[0],
                    this.remoteCoords[1], this.remoteCoords[2],
                    this.remoteCoords[3], r);
        } else {
            b = m.rotateRadiusCounterclockwise(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), r);
        }
        if (b) {
            this.findPlayerAndAdjust();
        } else {
            this.keepNextMessage();
            Messager.showMessage("Rotation failed!");
        }
    }

    public void fireArrow(final int x, final int y) {
        final ArrowTask at = new ArrowTask(x, y, this.activeArrowType);
        this.arrowActive = true;
        at.start();
    }

    public void updatePositionRelative(final int ox, final int oy) {
        this.actingRemotely = false;
        int px = this.plMgr.getPlayerLocationX();
        int py = this.plMgr.getPlayerLocationY();
        final int pz = this.plMgr.getPlayerLocationZ();
        final int pw = this.plMgr.getPlayerLocationW();
        final int[] mod = this.doEffects(ox, oy);
        final int x = mod[0];
        final int y = mod[1];
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        m.tickTimers(pw, pz);
        boolean proceed = false;
        AbstractMazeObject o = new Empty();
        AbstractMazeObject acted = new Empty();
        AbstractMazeObject groundInto = new Empty();
        AbstractMazeObject below = null;
        AbstractMazeObject previousBelow = null;
        AbstractMazeObject nextBelow = null;
        AbstractMazeObject nextAbove = null;
        AbstractMazeObject nextNextBelow = null;
        AbstractMazeObject nextNextAbove = null;
        final boolean isXNonZero = x != 0;
        final boolean isYNonZero = y != 0;
        int pullX = 0, pullY = 0, pushX = 0, pushY = 0;
        if (isXNonZero) {
            final int signX = (int) Math.signum(x);
            pushX = (Math.abs(x) + 1) * signX;
            pullX = (Math.abs(x) - 1) * signX;
        }
        if (isYNonZero) {
            final int signY = (int) Math.signum(y);
            pushY = (Math.abs(y) + 1) * signY;
            pullY = (Math.abs(y) - 1) * signY;
        }
        do {
            try {
                try {
                    o = m.getCell(px + x, py + y, pz, pw, Maze.LAYER_OBJECT);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    o = new Empty();
                }
                try {
                    below = m.getCell(px, py, pz, pw, Maze.LAYER_GROUND);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    below = new Empty();
                }
                try {
                    nextBelow = m.getCell(px + x, py + y, pz, pw,
                            Maze.LAYER_GROUND);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    nextBelow = new Empty();
                }
                try {
                    nextAbove = m.getCell(px + x, py + y, pz, pw,
                            Maze.LAYER_OBJECT);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    nextAbove = new Wall();
                }
                try {
                    previousBelow = m.getCell(px - x, py - y, pz, pw,
                            Maze.LAYER_GROUND);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    previousBelow = new Empty();
                }
                try {
                    nextNextBelow = m.getCell(px + 2 * x, py + 2 * y, pz, pw,
                            Maze.LAYER_GROUND);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    nextNextBelow = new Empty();
                }
                try {
                    nextNextAbove = m.getCell(px + 2 * x, py + 2 * y, pz, pw,
                            Maze.LAYER_OBJECT);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    nextNextAbove = new Wall();
                }
                try {
                    proceed = o.preMoveAction(true, px + x, py + y,
                            this.objectInv);
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    proceed = true;
                } catch (final InfiniteRecursionException ir) {
                    proceed = false;
                }
            } catch (final NullPointerException np) {
                proceed = false;
                o = new Empty();
            }
            if (proceed) {
                this.plMgr.savePlayerLocation();
                this.vwMgr.saveViewingWindow();
                try {
                    if (this.checkSolid(x + px, y + py, this.savedAbstractMazeObject,
                            below, nextBelow, nextAbove)) {
                        if (this.delayedDecayActive) {
                            this.doDelayedDecay();
                        }
                        m.setCell(this.savedAbstractMazeObject, px, py, pz, pw,
                                Maze.LAYER_OBJECT);
                        acted = new Empty();
                        try {
                            acted = m.getCell(px - x, py - y, pz, pw,
                                    Maze.LAYER_OBJECT);
                        } catch (final ArrayIndexOutOfBoundsException ae) {
                            // Do nothing
                        }
                        if (acted.isPullable() && this.isPullInProgress()) {
                            if (!this.checkPull(x, y, pullX, pullY, acted,
                                    previousBelow, below,
                                    this.savedAbstractMazeObject)) {
                                // Pull failed - object can't move that way
                                acted.pullFailedAction(this.objectInv, x, y,
                                        pullX, pullY);
                                this.st.deductStep();
                                this.decayEffects();
                            }
                        } else if (!acted.isPullable()
                                && this.isPullInProgress()) {
                            // Pull failed - object not pullable
                            acted.pullFailedAction(this.objectInv, x, y, pullX,
                                    pullY);
                            this.st.deductStep();
                            this.decayEffects();
                        }
                        this.plMgr.offsetPlayerLocationX(x);
                        this.plMgr.offsetPlayerLocationY(y);
                        px += x;
                        py += y;
                        this.vwMgr.offsetViewingWindowLocationX(y);
                        this.vwMgr.offsetViewingWindowLocationY(x);
                        this.savedAbstractMazeObject = m.getCell(px, py, pz, pw,
                                Maze.LAYER_OBJECT);
                        groundInto = m.getCell(px, py, pz, pw,
                                Maze.LAYER_GROUND);
                        m.setCell(new Player(), px, py, pz, pw,
                                Maze.LAYER_OBJECT);
                        this.decayEffects();
                        this.redrawMaze();
                        app.getMazeManager().setDirty(true);
                        this.st.deductStep();
                        if (app.getMode() != BagOStuff.STATUS_BATTLE) {
                            if (groundInto.overridesDefaultPostMove()) {
                                groundInto.postMoveAction(false, px, py,
                                        this.objectInv);
                            } else {
                                this.savedAbstractMazeObject.postMoveAction(false, px,
                                        py, this.objectInv);
                            }
                        }
                        this.objectInv.fireStepActions();
                    } else {
                        acted = m.getCell(px + x, py + y, pz, pw,
                                Maze.LAYER_OBJECT);
                        if (acted.isPushable()) {
                            if (this.checkPush(x, y, pushX, pushY, acted,
                                    nextBelow, nextNextBelow, nextNextAbove)) {
                                if (this.delayedDecayActive) {
                                    this.doDelayedDecay();
                                }
                                m.setCell(this.savedAbstractMazeObject, px, py, pz, pw,
                                        Maze.LAYER_OBJECT);
                                this.plMgr.offsetPlayerLocationX(x);
                                this.plMgr.offsetPlayerLocationY(y);
                                px += x;
                                py += y;
                                this.vwMgr.offsetViewingWindowLocationX(y);
                                this.vwMgr.offsetViewingWindowLocationY(x);
                                this.savedAbstractMazeObject = m.getCell(px, py, pz, pw,
                                        Maze.LAYER_OBJECT);
                                groundInto = m.getCell(px, py, pz, pw,
                                        Maze.LAYER_GROUND);
                                m.setCell(new Player(), px, py, pz, pw,
                                        Maze.LAYER_OBJECT);
                                this.decayEffects();
                                this.redrawMaze();
                                app.getMazeManager().setDirty(true);
                                this.st.deductStep();
                                if (app.getMode() != BagOStuff.STATUS_BATTLE) {
                                    if (groundInto.overridesDefaultPostMove()) {
                                        groundInto.postMoveAction(false, px, py,
                                                this.objectInv);
                                    } else {
                                        this.savedAbstractMazeObject.postMoveAction(
                                                false, px, py, this.objectInv);
                                    }
                                }
                                this.objectInv.fireStepActions();
                            } else {
                                // Push failed - object can't move that way
                                acted.pushFailedAction(this.objectInv, x, y,
                                        pushX, pushY);
                                this.decayEffects();
                                this.st.deductStep();
                            }
                        } else if (acted.doesChainReact()) {
                            acted.chainReactionAction(px + x, py + y, pz, pw);
                        } else {
                            // Move failed - object is solid in that direction
                            this.fireMoveFailedActions(px + x, py + y,
                                    this.savedAbstractMazeObject, below, nextBelow,
                                    nextAbove);
                            this.decayEffects();
                            this.st.deductStep();
                        }
                    }
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    this.vwMgr.restoreViewingWindow();
                    this.plMgr.restorePlayerLocation();
                    m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                            this.plMgr.getPlayerLocationY(),
                            this.plMgr.getPlayerLocationZ(),
                            this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                    // Move failed - attempted to go outside the maze
                    o.moveFailedAction(false, this.plMgr.getPlayerLocationX(),
                            this.plMgr.getPlayerLocationY(), this.objectInv);
                    this.decayEffects();
                    this.st.deductStep();
                    Messager.showMessage("Can't go outside the maze");
                    o = new Empty();
                }
            } else {
                // Move failed - pre-move check failed
                o.moveFailedAction(false, px + x, py + y, this.objectInv);
                this.decayEffects();
                this.st.deductStep();
            }
        } while (proceed
                && !groundInto.hasFrictionConditionally(this.objectInv, false)
                && this.checkSolid(x, y, this.savedAbstractMazeObject, below, nextBelow,
                        nextAbove));
        this.updateStats();
    }

    public void backUpPlayer() {
        final Maze m = FantastleReboot.getBagOStuff().getMazeManager()
                .getMaze();
        final int pz = this.plMgr.getPlayerLocationZ();
        final int pw = this.plMgr.getPlayerLocationW();
        this.plMgr.restorePlayerLocation();
        this.vwMgr.restoreViewingWindow();
        final int opx = this.plMgr.getPlayerLocationX();
        final int opy = this.plMgr.getPlayerLocationY();
        this.savedAbstractMazeObject = m.getCell(opx, opy, pz, pw, Maze.LAYER_OBJECT);
        m.setCell(new Player(), opx, opy, pz, pw, Maze.LAYER_OBJECT);
        this.redrawMaze();
    }

    private boolean checkSolid(final int x, final int y,
            final AbstractMazeObject inside, final AbstractMazeObject below,
            final AbstractMazeObject nextBelow, final AbstractMazeObject nextAbove) {
        final int px = this.plMgr.getPlayerLocationX();
        final int py = this.plMgr.getPlayerLocationY();
        final boolean insideSolid = inside.isConditionallyDirectionallySolid(
                false, x - px, y - py, this.objectInv);
        final boolean belowSolid = below.isConditionallyDirectionallySolid(
                false, x - px, y - py, this.objectInv);
        final boolean nextBelowSolid = nextBelow
                .isConditionallyDirectionallySolid(true, x - px, y - py,
                        this.objectInv);
        final boolean nextAboveSolid = nextAbove
                .isConditionallyDirectionallySolid(true, x - px, y - py,
                        this.objectInv);
        if (insideSolid || belowSolid || nextBelowSolid || nextAboveSolid) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSolidAbsolute(final AbstractMazeObject inside,
            final AbstractMazeObject below, final AbstractMazeObject nextBelow,
            final AbstractMazeObject nextAbove) {
        final boolean insideSolid = inside.isConditionallySolid(this.objectInv);
        final boolean belowSolid = below.isConditionallySolid(this.objectInv);
        final boolean nextBelowSolid = nextBelow
                .isConditionallySolid(this.objectInv);
        final boolean nextAboveSolid = nextAbove
                .isConditionallySolid(this.objectInv);
        if (insideSolid || belowSolid || nextBelowSolid || nextAboveSolid) {
            return false;
        } else {
            return true;
        }
    }

    private void fireMoveFailedActions(final int x, final int y,
            final AbstractMazeObject inside, final AbstractMazeObject below,
            final AbstractMazeObject nextBelow, final AbstractMazeObject nextAbove) {
        final int px = this.plMgr.getPlayerLocationX();
        final int py = this.plMgr.getPlayerLocationY();
        final boolean insideSolid = inside.isConditionallyDirectionallySolid(
                false, x - px, y - py, this.objectInv);
        final boolean belowSolid = below.isConditionallyDirectionallySolid(
                false, x - px, y - py, this.objectInv);
        final boolean nextBelowSolid = nextBelow
                .isConditionallyDirectionallySolid(true, x - px, y - py,
                        this.objectInv);
        final boolean nextAboveSolid = nextAbove
                .isConditionallyDirectionallySolid(true, x - px, y - py,
                        this.objectInv);
        if (insideSolid) {
            inside.moveFailedAction(false, x, y, this.objectInv);
        }
        if (belowSolid) {
            below.moveFailedAction(false, x, y, this.objectInv);
        }
        if (nextBelowSolid) {
            nextBelow.moveFailedAction(false, x, y, this.objectInv);
        }
        if (nextAboveSolid) {
            nextAbove.moveFailedAction(false, x, y, this.objectInv);
        }
    }

    private boolean checkPush(final int x, final int y, final int pushX,
            final int pushY, final AbstractMazeObject acted, final AbstractMazeObject nextBelow,
            final AbstractMazeObject nextNextBelow, final AbstractMazeObject nextNextAbove) {
        final int px = this.plMgr.getPlayerLocationX();
        final int py = this.plMgr.getPlayerLocationY();
        final int pz = this.plMgr.getPlayerLocationZ();
        final int pw = this.plMgr.getPlayerLocationW();
        final boolean nextBelowAccept = nextBelow.isPushableOut();
        final boolean nextNextBelowAccept = nextNextBelow.isPushableInto();
        final boolean nextNextAboveAccept = nextNextAbove.isPushableInto();
        if (nextBelowAccept && nextNextBelowAccept && nextNextAboveAccept) {
            nextBelow.pushOutAction(this.objectInv, acted, px + pushX,
                    py + pushY, pz, pw);
            acted.pushAction(this.objectInv, nextNextAbove, x, y, pushX, pushY);
            nextNextAbove.pushIntoAction(this.objectInv, acted, px + pushX,
                    py + pushY, pz, pw);
            nextNextBelow.pushIntoAction(this.objectInv, acted, px + pushX,
                    py + pushY, pz, pw);
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPull(final int x, final int y, final int pullX,
            final int pullY, final AbstractMazeObject acted,
            final AbstractMazeObject previousBelow, final AbstractMazeObject below,
            final AbstractMazeObject above) {
        final int px = this.plMgr.getPlayerLocationX();
        final int py = this.plMgr.getPlayerLocationY();
        final int pz = this.plMgr.getPlayerLocationZ();
        final int pw = this.plMgr.getPlayerLocationW();
        final boolean previousBelowAccept = previousBelow.isPullableOut();
        final boolean belowAccept = below.isPullableInto();
        final boolean aboveAccept = above.isPullableInto();
        if (previousBelowAccept && belowAccept && aboveAccept) {
            previousBelow.pullOutAction(this.objectInv, acted, px - pullX,
                    py - pullY, pz, pw);
            acted.pullAction(this.objectInv, above, x, y, pullX, pullY);
            above.pullIntoAction(this.objectInv, acted, px - pullX, py - pullY,
                    pz, pw);
            below.pullIntoAction(this.objectInv, acted, px - pullX, py - pullY,
                    pz, pw);
            return true;
        } else {
            return false;
        }
    }

    public void updatePushedPosition(final int x, final int y, final int pushX,
            final int pushY, final AbstractMazeObject o) {
        final int xInc = (int) Math.signum(x), yInc = (int) Math.signum(y);
        int cumPushX = pushX, cumPushY = pushY, cumX = x, cumY = y;
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final MazeManager mm = app.getMazeManager();
        AbstractMazeObject there = mm.getAbstractMazeObject(
                this.plMgr.getPlayerLocationX() + cumX,
                this.plMgr.getPlayerLocationY() + cumY,
                this.plMgr.getPlayerLocationZ(),
                this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
        if (there != null) {
            do {
                this.movePushedObjectPosition(cumX, cumY, cumPushX, cumPushY, o,
                        there);
                cumX += xInc;
                cumY += yInc;
                cumPushX += xInc;
                cumPushY += yInc;
                there = mm.getAbstractMazeObject(this.plMgr.getPlayerLocationX() + cumX,
                        this.plMgr.getPlayerLocationY() + cumY,
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
                if (there == null) {
                    break;
                }
            } while (!there.hasFrictionConditionally(this.objectInv, true));
        }
    }

    private void movePushedObjectPosition(final int x, final int y,
            final int pushX, final int pushY, final AbstractMazeObject o,
            final AbstractMazeObject g) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(new Empty(), this.plMgr.getPlayerLocationX() + x,
                    this.plMgr.getPlayerLocationY() + y,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            m.setCell(o, this.plMgr.getPlayerLocationX() + pushX,
                    this.plMgr.getPlayerLocationY() + pushY,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            if (g.overridesDefaultPostMove()) {
                g.postMoveAction(false, this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(), this.objectInv);
            }
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        }
    }

    public void updatePulledPosition(final int x, final int y, final int pullX,
            final int pullY, final AbstractMazeObject o) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(new Empty(), this.plMgr.getPlayerLocationX() - x,
                    this.plMgr.getPlayerLocationY() - y,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            m.setCell(o, this.plMgr.getPlayerLocationX() - pullX,
                    this.plMgr.getPlayerLocationY() - pullY,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        }
    }

    public void updatePushedIntoPositionAbsolute(final int x, final int y,
            final int z, final int w, final int x2, final int y2, final int z2,
            final int w2, final GenericMovableObject pushedInto,
            final AbstractMazeObject source) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            if (!m.getCell(x, y, z, w, Maze.LAYER_OBJECT)
                    .isConditionallySolid(this.objectInv)) {
                final AbstractMazeObject saved = m.getCell(x, y, z, w,
                        Maze.LAYER_OBJECT);
                m.setCell(pushedInto, x, y, z, w, Maze.LAYER_OBJECT);
                m.setCell(source, x2, y2, z2, w2, Maze.LAYER_OBJECT);
                saved.pushIntoAction(this.objectInv, pushedInto, x2, y2, z2 - 1,
                        w2);
                this.redrawMaze();
                app.getMazeManager().setDirty(true);
            }
        } catch (final ArrayIndexOutOfBoundsException ae) {
            m.setCell(new Empty(), x2, y2, z2, w2, Maze.LAYER_OBJECT);
        }
    }

    public boolean tryUpdatePositionRelative(final int x, final int y) {
        try {
            final BagOStuff app = FantastleReboot.getBagOStuff();
            final Maze m = app.getMazeManager().getMaze();
            final AbstractMazeObject below = m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
            final AbstractMazeObject nextBelow = m.getCell(
                    this.plMgr.getPlayerLocationX() + x,
                    this.plMgr.getPlayerLocationY() + y,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
            final AbstractMazeObject nextAbove = m.getCell(
                    this.plMgr.getPlayerLocationX() + x,
                    this.plMgr.getPlayerLocationY() + y,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            return this.checkSolid(x, y, this.savedAbstractMazeObject, below, nextBelow,
                    nextAbove);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        }
    }

    public boolean tryUpdatePositionAbsolute(final int x, final int y,
            final int z) {
        try {
            final BagOStuff app = FantastleReboot.getBagOStuff();
            final Maze m = app.getMazeManager().getMaze();
            final AbstractMazeObject below = m.getCell(this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
            final AbstractMazeObject nextBelow = m.getCell(x, y, z,
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_GROUND);
            final AbstractMazeObject nextAbove = m.getCell(x, y, z,
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            return this.checkSolidAbsolute(this.savedAbstractMazeObject, below,
                    nextBelow, nextAbove);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            return false;
        }
    }

    public void updatePositionAbsolute(final int x, final int y, final int z,
            final int w) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.getCell(x, y, z, w, Maze.LAYER_OBJECT).preMoveAction(true, x, y,
                    this.objectInv);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
        this.plMgr.savePlayerLocation();
        this.vwMgr.saveViewingWindow();
        try {
            if (!m.getCell(x, y, z, w, Maze.LAYER_OBJECT)
                    .isConditionallySolid(this.objectInv)) {
                m.setCell(this.savedAbstractMazeObject, this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                this.plMgr.setPlayerLocation(x, y, z, w);
                this.vwMgr.setViewingWindowLocationX(
                        this.plMgr.getPlayerLocationY()
                                - GameViewingWindowManager.getOffsetFactorX());
                this.vwMgr.setViewingWindowLocationY(
                        this.plMgr.getPlayerLocationX()
                                - GameViewingWindowManager.getOffsetFactorY());
                this.savedAbstractMazeObject = m.getCell(
                        this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                this.redrawMaze();
                app.getMazeManager().setDirty(true);
                this.savedAbstractMazeObject.postMoveAction(false, x, y,
                        this.objectInv);
            }
        } catch (final ArrayIndexOutOfBoundsException ae) {
            this.plMgr.restorePlayerLocation();
            this.vwMgr.restoreViewingWindow();
            m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            Messager.showMessage("Can't go outside the maze");
        } catch (final NullPointerException np) {
            this.plMgr.restorePlayerLocation();
            this.vwMgr.restoreViewingWindow();
            m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            Messager.showMessage("Can't go outside the maze");
        }
    }

    public void updatePositionAbsoluteNoEvents(final int x, final int y,
            final int z, final int w) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        this.plMgr.savePlayerLocation();
        this.vwMgr.saveViewingWindow();
        try {
            if (!m.getCell(x, y, z, w, Maze.LAYER_OBJECT)
                    .isConditionallySolid(this.objectInv)) {
                m.setCell(this.savedAbstractMazeObject, this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                this.plMgr.setPlayerLocation(x, y, z, w);
                this.vwMgr.setViewingWindowLocationX(
                        this.plMgr.getPlayerLocationY()
                                - GameViewingWindowManager.getOffsetFactorX());
                this.vwMgr.setViewingWindowLocationY(
                        this.plMgr.getPlayerLocationX()
                                - GameViewingWindowManager.getOffsetFactorY());
                this.savedAbstractMazeObject = m.getCell(
                        this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                        this.plMgr.getPlayerLocationY(),
                        this.plMgr.getPlayerLocationZ(),
                        this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
                this.redrawMaze();
                app.getMazeManager().setDirty(true);
            }
        } catch (final ArrayIndexOutOfBoundsException ae) {
            this.plMgr.restorePlayerLocation();
            this.vwMgr.restoreViewingWindow();
            m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            Messager.showMessage("Can't go outside the maze");
        } catch (final NullPointerException np) {
            this.plMgr.restorePlayerLocation();
            this.vwMgr.restoreViewingWindow();
            m.setCell(new Player(), this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(),
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), Maze.LAYER_OBJECT);
            Messager.showMessage("Can't go outside the maze");
        }
    }

    public void invalidateScore() {
        this.st.invalidateScore();
    }

    public void showCurrentScore() {
        this.st.showCurrentScore();
    }

    public void showScoreTable() {
        this.st.showScoreTable();
    }

    public void validateScore() {
        this.st.validateScore();
    }

    public void redrawMaze() {
        this.updateStats();
        // Draw the maze, if it is visible
        if (this.outputFrame.isVisible()) {
            // Rebuild draw grid
            final EmptyBorder eb = new EmptyBorder(0, 0, 0, 0);
            this.outputPane.removeAll();
            for (int x = 0; x < GameViewingWindowManager
                    .getViewingWindowSizeX(); x++) {
                for (int y = 0; y < GameViewingWindowManager
                        .getViewingWindowSizeY(); y++) {
                    this.drawGrid[x][y] = new JLabel();
                    // Fix to make draw grid line up properly
                    this.drawGrid[x][y].setBorder(eb);
                    this.outputPane.add(this.drawGrid[x][y]);
                }
            }
            this.redrawMazeNoRebuild();
        }
    }

    public void redrawMazeNoRebuild() {
        this.updateStats();
        // Draw the maze, if it is visible
        if (this.outputFrame.isVisible()) {
            final BagOStuff app = FantastleReboot.getBagOStuff();
            int x, y, u, v;
            int xFix, yFix;
            boolean visible;
            u = this.plMgr.getPlayerLocationX();
            v = this.plMgr.getPlayerLocationY();
            for (x = this.vwMgr.getViewingWindowLocationX(); x <= this.vwMgr
                    .getLowerRightViewingWindowLocationX(); x++) {
                for (y = this.vwMgr.getViewingWindowLocationY(); y <= this.vwMgr
                        .getLowerRightViewingWindowLocationY(); y++) {
                    xFix = x - this.vwMgr.getViewingWindowLocationX();
                    yFix = y - this.vwMgr.getViewingWindowLocationY();
                    visible = app.getMazeManager().getMaze().isSquareVisible(u,
                            v, y, x, this.plMgr.getPlayerLocationW());
                    try {
                        if (visible) {
                            final String name1 = app.getMazeManager().getMaze()
                                    .getCell(y, x,
                                            this.plMgr.getPlayerLocationZ(),
                                            this.plMgr.getPlayerLocationW(),
                                            Maze.LAYER_GROUND)
                                    .gameRenderHook(y, x,
                                            this.plMgr.getPlayerLocationZ(),
                                            this.plMgr.getPlayerLocationW());
                            final String name2 = app.getMazeManager().getMaze()
                                    .getCell(y, x,
                                            this.plMgr.getPlayerLocationZ(),
                                            this.plMgr.getPlayerLocationW(),
                                            Maze.LAYER_OBJECT)
                                    .gameRenderHook(y, x,
                                            this.plMgr.getPlayerLocationZ(),
                                            this.plMgr.getPlayerLocationW());
                            this.drawGrid[xFix][yFix].setIcon(ObjectImageManager
                                    .getCompositeImage(name1, name2));
                        } else {
                            this.drawGrid[xFix][yFix].setIcon(
                                    ObjectImageManager.getImage("Darkness"));
                        }
                    } catch (final ArrayIndexOutOfBoundsException ae) {
                        this.drawGrid[xFix][yFix].setIcon(ObjectImageManager
                                .getImage(new EmptyVoid().gameRenderHook(y, x,
                                        this.plMgr.getPlayerLocationZ(),
                                        this.plMgr.getPlayerLocationW())));
                    } catch (final NullPointerException np) {
                        this.drawGrid[xFix][yFix].setIcon(ObjectImageManager
                                .getImage(new EmptyVoid().gameRenderHook(y, x,
                                        this.plMgr.getPlayerLocationZ(),
                                        this.plMgr.getPlayerLocationW())));
                    }
                }
            }
            if (this.knm) {
                this.knm = false;
            } else {
                this.setStatusMessage(" ");
            }
            this.outputFrame.pack();
        }
    }

    void redrawOneSquare(final int x, final int y, final boolean useDelay,
            final String name3) {
        // Draw the square, if the maze is visible
        final BagOStuff app = FantastleReboot.getBagOStuff();
        if (this.outputFrame.isVisible()) {
            int xFix, yFix;
            boolean visible;
            xFix = y - this.vwMgr.getViewingWindowLocationX();
            yFix = x - this.vwMgr.getViewingWindowLocationY();
            visible = app.getMazeManager().getMaze().isSquareVisible(
                    this.plMgr.getPlayerLocationX(),
                    this.plMgr.getPlayerLocationY(), x, y,
                    this.plMgr.getPlayerLocationW());
            try {
                if (visible) {
                    final String name1 = app.getMazeManager().getMaze()
                            .getCell(x, y, this.plMgr.getPlayerLocationZ(),
                                    this.plMgr.getPlayerLocationW(),
                                    Maze.LAYER_GROUND)
                            .gameRenderHook(x, y,
                                    this.plMgr.getPlayerLocationZ(),
                                    this.plMgr.getPlayerLocationW());
                    final String name2 = app.getMazeManager().getMaze()
                            .getCell(x, y, this.plMgr.getPlayerLocationZ(),
                                    this.plMgr.getPlayerLocationW(),
                                    Maze.LAYER_OBJECT)
                            .gameRenderHook(x, y,
                                    this.plMgr.getPlayerLocationZ(),
                                    this.plMgr.getPlayerLocationW());
                    this.drawGrid[xFix][yFix].setIcon(ObjectImageManager
                            .getVirtualCompositeImage(name1, name2, name3));
                } else {
                    this.drawGrid[xFix][yFix]
                            .setIcon(ObjectImageManager.getImage("Darkness"));
                }
                this.drawGrid[xFix][yFix].repaint();
            } catch (final ArrayIndexOutOfBoundsException ae) {
                // Do nothing
            } catch (final NullPointerException np) {
                // Do nothing
            }
            this.outputFrame.pack();
            if (useDelay) {
                // Delay, for animation purposes
                try {
                    Thread.sleep(60);
                } catch (final InterruptedException ie) {
                    // Ignore
                }
            }
        }
    }

    public void updateStats() {
        // Update stats
        this.sg.updateStats();
        // Check for game over
        if (!PartyManager.getParty().getLeader().isAlive()) {
            this.gameOver();
        }
    }

    public void resetViewingWindowAndPlayerLocation() {
        this.resetPlayerLocation();
        this.resetViewingWindow();
    }

    public void resetViewingWindow() {
        this.vwMgr.setViewingWindowLocationX(this.plMgr.getPlayerLocationY()
                - GameViewingWindowManager.getOffsetFactorX());
        this.vwMgr.setViewingWindowLocationY(this.plMgr.getPlayerLocationX()
                - GameViewingWindowManager.getOffsetFactorY());
    }

    public void resetPlayerLocation() {
        this.resetPlayerLocation(0);
    }

    public void resetPlayerLocation(final int level) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        this.plMgr.setPlayerLocation(m.getStartColumn(level),
                m.getStartRow(level), m.getStartFloor(level), level);
    }

    public void resetCurrentLevel() {
        this.resetLevel(this.plMgr.getPlayerLocationW());
    }

    public void resetGameState() {
        this.deactivateAllEffects();
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        app.getMazeManager().setDirty(false);
        m.restore();
        this.setSavedGameFlag(false);
        this.st.resetScore(app.getMazeManager().getScoresFileName());
        this.decay();
        this.objectInv = new ObjectInventory();
        this.savedObjectInv = new ObjectInventory();
        final int startW = m.getStartLevel();
        final boolean playerExists = m.findPlayerOnLevel(startW);
        if (playerExists) {
            m.findAllStarts();
            this.plMgr.setPlayerLocation(m.getFindResultColumn(startW),
                    m.getFindResultRow(startW), m.getFindResultFloor(startW),
                    startW);
            m.save();
        }
    }

    public void resetLevel(final int level) {
        PartyManager.getParty().getLeader().healAndRegenerateFully();
        this.deactivateAllEffects();
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        app.getMazeManager().setDirty(true);
        m.restoreLevel(level);
        final boolean playerExists = m.findPlayerOnLevel(level);
        if (playerExists) {
            this.st.resetScore(app.getMazeManager().getScoresFileName());
            this.resetPlayerLocation(level);
            this.resetViewingWindow();
            m.setVisionRadiusToMaximum(level);
            this.decay();
            this.restoreObjectInventory();
            this.redrawMaze();
        }
    }

    public void solvedLevel() {
        this.deactivateAllEffects();
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        final boolean playerExists = m
                .findPlayerOnLevel(this.plMgr.getPlayerLocationW() + 1);
        if (playerExists) {
            m.restoreLevel(this.plMgr.getPlayerLocationW());
            this.resetPlayerLocation(this.plMgr.getPlayerLocationW() + 1);
            this.resetViewingWindow();
            m.setVisionRadiusToMaximum(this.plMgr.getPlayerLocationW());
            this.decay();
            this.saveObjectInventory();
            this.redrawMaze();
        } else {
            this.solvedMaze();
        }
    }

    public void solvedLevelWarp(final int level) {
        this.deactivateAllEffects();
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        final boolean playerExists = m.findPlayerOnLevel(level);
        if (playerExists) {
            m.restoreLevel(this.plMgr.getPlayerLocationW());
            this.resetPlayerLocation(level);
            this.resetViewingWindow();
            this.decay();
            this.saveObjectInventory();
            this.redrawMaze();
        } else {
            this.solvedMaze();
        }
    }

    private void gameOver() {
        if (FantastleReboot.getBagOStuff().getPrefsManager()
                .getSoundEnabled(PreferencesManager.SOUNDS_UI)) {
            SoundPlayer.playSound(GameSound.GAME_OVER);
        }
        if (this.gameOverMessage == null) {
            Messager.showDialog("You have died - Game Over!");
        } else {
            Messager.showDialog(this.gameOverMessage);
        }
        this.solvedMaze();
    }

    public void gameOverWithMessage(final String msg) {
        this.gameOverMessage = msg;
    }

    public void solvedMaze() {
        PartyManager.getParty().getLeader().healAndRegenerateFully();
        this.deactivateAllEffects();
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        // Restore the maze
        m.restoreLevel(this.plMgr.getPlayerLocationW());
        final boolean playerExists = m.findPlayerOnLevel(0);
        if (playerExists) {
            this.resetViewingWindowAndPlayerLocation();
        } else {
            app.getMazeManager().setLoaded(false);
        }
        // Wipe the inventory
        this.objectInv = new ObjectInventory();
        // Reset saved game flag
        this.savedGameFlag = false;
        app.getMazeManager().setDirty(false);
        if (this.st.checkScore()) {
            app.playHighScoreSound();
        }
        this.st.commitScore();
        this.hideOutput();
        app.getGUIManager().showGUI();
    }

    public JFrame getOutputFrame() {
        return this.outputFrame;
    }

    public void decay() {
        if (this.actingRemotely) {
            FantastleReboot.getBagOStuff().getMazeManager().getMaze().setCell(
                    new Empty(), this.remoteCoords[0], this.remoteCoords[1],
                    this.remoteCoords[2], this.remoteCoords[3],
                    Maze.LAYER_OBJECT);
        } else {
            this.savedAbstractMazeObject = new Empty();
        }
    }

    public void decayTo(final AbstractMazeObject obj) {
        if (this.actingRemotely) {
            FantastleReboot.getBagOStuff().getMazeManager().getMaze().setCell(
                    obj, this.remoteCoords[0], this.remoteCoords[1],
                    this.remoteCoords[2], this.remoteCoords[3],
                    Maze.LAYER_OBJECT);
        } else {
            this.savedAbstractMazeObject = obj;
        }
    }

    private void doDelayedDecay() {
        if (this.actingRemotely) {
            FantastleReboot.getBagOStuff().getMazeManager().getMaze().setCell(
                    this.delayedDecayObject, this.remoteCoords[0],
                    this.remoteCoords[1], this.remoteCoords[2],
                    this.remoteCoords[3], Maze.LAYER_OBJECT);
        } else {
            this.savedAbstractMazeObject = this.delayedDecayObject;
        }
        this.delayedDecayActive = false;
    }

    public void delayedDecayTo(final AbstractMazeObject obj) {
        this.delayedDecayActive = true;
        this.delayedDecayObject = obj;
    }

    public void morph(final AbstractMazeObject morphInto, final int x, final int y,
            final int z, final int w) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(morphInto, x, y, z, w, morphInto.getLayer());
            this.redrawMazeNoRebuild();
            app.getMazeManager().setDirty(true);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
    }

    public void morph(final AbstractMazeObject morphInto, final int x, final int y,
            final int z, final int w, final String msg) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(morphInto, x, y, z, w, morphInto.getLayer());
            Messager.showMessage(msg);
            this.keepNextMessage();
            this.redrawMazeNoRebuild();
            app.getMazeManager().setDirty(true);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
    }

    public void morph(final AbstractMazeObject morphInto, final int x, final int y,
            final int z, final int w, final int e) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(morphInto, x, y, z, w, e);
            this.redrawMazeNoRebuild();
            app.getMazeManager().setDirty(true);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
    }

    public void morph(final AbstractMazeObject morphInto, final int x, final int y,
            final int z, final int w, final int e, final String msg) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(morphInto, x, y, z, w, e);
            Messager.showMessage(msg);
            this.keepNextMessage();
            this.redrawMazeNoRebuild();
            app.getMazeManager().setDirty(true);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
    }

    public void morphOther(final AbstractMazeObject morphInto, final int x, final int y,
            final int e) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        try {
            m.setCell(morphInto, this.plMgr.getPlayerLocationX() + x,
                    this.plMgr.getPlayerLocationY() + y,
                    this.plMgr.getPlayerLocationZ(),
                    this.plMgr.getPlayerLocationW(), e);
            this.redrawMazeNoRebuild();
            app.getMazeManager().setDirty(true);
        } catch (final ArrayIndexOutOfBoundsException ae) {
            // Do nothing
        } catch (final NullPointerException np) {
            // Do nothing
        }
    }

    public void keepNextMessage() {
        this.knm = true;
    }

    public void showEquipmentDialog() {
        final String[] equipString = PartyManager.getParty().getLeader()
                .getItems().generateEquipmentStringArray();
        Messager.showInputDialog("Equipment", "Equipment", equipString,
                equipString[0]);
    }

    public void showInventoryDialog() {
        final String[] inv1String = this.objectInv
                .generateInventoryStringArray();
        final String[] inv2String = PartyManager.getParty().getLeader()
                .getItems().generateInventoryStringArray(inv1String.length);
        final String[] invString = new String[inv1String.length
                + inv2String.length];
        for (int x = 0; x < inv1String.length + inv2String.length; x++) {
            if (x < inv1String.length) {
                invString[x] = inv1String[x];
            } else {
                invString[x] = inv2String[x - inv1String.length];
            }
        }
        Messager.showInputDialog("Inventory", "Inventory", invString,
                invString[0]);
    }

    public void showUseDialog() {
        int x;
        final MazeObjectList list = FantastleReboot.getBagOStuff().getObjects();
        final AbstractMazeObject[] choices = list.getAllUsableObjects();
        final String[] userChoices = this.objectInv.generateUseStringArray();
        final String result = Messager.showInputDialog("Use which item?",
                "Fantastle", userChoices,
                userChoices[this.lastUsedObjectIndex]);
        try {
            for (x = 0; x < choices.length; x++) {
                if (result.equals(userChoices[x])) {
                    this.lastUsedObjectIndex = x;
                    this.objectBeingUsed = choices[x];
                    if (this.objectInv.getUses(this.objectBeingUsed) == 0) {
                        Messager.showMessage(
                                "That item has no more uses left.");
                        this.setUsingAnItem(false);
                    } else if (!this.objectBeingUsed
                            .isOfType(TypeConstants.TYPE_BOW)) {
                        Messager.showMessage("Click to set target");
                    } else {
                        this.useItemHandler(0, 0);
                        this.setUsingAnItem(false);
                    }
                    return;
                }
            }
        } catch (final NullPointerException np) {
            this.setUsingAnItem(false);
        }
    }

    public ObjectInventory getObjectInventory() {
        return this.objectInv;
    }

    public void setObjectInventory(final ObjectInventory newObjectInventory) {
        this.objectInv = newObjectInventory;
    }

    public void useItemHandler(final int x, final int y) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        final int xOffset = this.vwMgr.getViewingWindowLocationX()
                - GameViewingWindowManager.getOffsetFactorX();
        final int yOffset = this.vwMgr.getViewingWindowLocationY()
                - GameViewingWindowManager.getOffsetFactorY();
        final int destX = x / ImageConstants.SIZE
                + this.vwMgr.getViewingWindowLocationX() - xOffset + yOffset;
        final int destY = y / ImageConstants.SIZE
                + this.vwMgr.getViewingWindowLocationY() + xOffset - yOffset;
        final int destZ = this.plMgr.getPlayerLocationZ();
        final int destW = this.plMgr.getPlayerLocationW();
        if (this.usingAnItem() && app.getMode() == BagOStuff.STATUS_GAME) {
            if (!this.objectBeingUsed.isOfType(TypeConstants.TYPE_BOW)) {
                final boolean visible = app.getMazeManager().getMaze()
                        .isSquareVisible(this.plMgr.getPlayerLocationX(),
                                this.plMgr.getPlayerLocationY(), destX, destY,
                                this.plMgr.getPlayerLocationW());
                try {
                    final AbstractMazeObject target = m.getCell(destX, destY, destZ,
                            destW, Maze.LAYER_OBJECT);
                    final String name = this.objectBeingUsed.getName();
                    if ((target.isSolid() || !visible)
                            && name.equals(new TeleportWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Can't teleport there");
                    }
                    if (target.getName().equals(new Player().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Don't aim at yourself!");
                    }
                    if (!target.isDestroyable()
                            && name.equals(new AnnihilationWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Can't destroy that");
                    }
                    if (!target.isDestroyable()
                            && name.equals(new WallMakingWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Can't create a wall there");
                    }
                    if (!target.isDestroyable()
                            && name.equals(new FinishMakingWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Can't create a finish there");
                    }
                    if ((!target.isDestroyable()
                            || !target.isOfType(TypeConstants.TYPE_WALL))
                            && name.equals(new WallBreakingWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Aim at a wall");
                    }
                    if ((!target.isDestroyable()
                            || !target.isOfType(TypeConstants.TYPE_TRAP))
                            && name.equals(new DisarmTrapWand().getName())) {
                        this.setUsingAnItem(false);
                        Messager.showMessage("Aim at a trap");
                    }
                } catch (final ArrayIndexOutOfBoundsException ae) {
                    this.setUsingAnItem(false);
                    Messager.showMessage("Aim within the maze");
                } catch (final NullPointerException np) {
                    this.setUsingAnItem(false);
                }
            }
            if (this.usingAnItem()) {
                this.objectInv.use(this.objectBeingUsed, destX, destY, destZ,
                        destW);
                this.redrawMaze();
            }
        }
    }

    public void controllableTeleport() {
        this.teleporting = true;
        Messager.showMessage("Click to set destination");
    }

    void controllableTeleportHandler(final int x, final int y) {
        if (this.teleporting) {
            final int xOffset = this.vwMgr.getViewingWindowLocationX()
                    - GameViewingWindowManager.getOffsetFactorX();
            final int yOffset = this.vwMgr.getViewingWindowLocationY()
                    - GameViewingWindowManager.getOffsetFactorY();
            final int destX = x / ImageConstants.SIZE
                    + this.vwMgr.getViewingWindowLocationX() - xOffset
                    + yOffset;
            final int destY = y / ImageConstants.SIZE
                    + this.vwMgr.getViewingWindowLocationY() + xOffset
                    - yOffset;
            final int destZ = this.plMgr.getPlayerLocationZ();
            final int destW = this.plMgr.getPlayerLocationW();
            this.updatePositionAbsolute(destX, destY, destZ, destW);
            if (FantastleReboot.getBagOStuff().getPrefsManager()
                    .getSoundEnabled(PreferencesManager.SOUNDS_GAME)) {
                SoundPlayer.playSound(GameSound.TELEPORT);
            }
            this.teleporting = false;
        }
    }

    public void identifyObject(final int x, final int y) {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        final Maze m = app.getMazeManager().getMaze();
        final int xOffset = this.vwMgr.getViewingWindowLocationX()
                - GameViewingWindowManager.getOffsetFactorX();
        final int yOffset = this.vwMgr.getViewingWindowLocationY()
                - GameViewingWindowManager.getOffsetFactorY();
        final int destX = x / ImageConstants.SIZE
                + this.vwMgr.getViewingWindowLocationX() - xOffset + yOffset;
        final int destY = y / ImageConstants.SIZE
                + this.vwMgr.getViewingWindowLocationY() + xOffset - yOffset;
        final int destZ = this.plMgr.getPlayerLocationZ();
        final int destW = this.plMgr.getPlayerLocationW();
        try {
            final AbstractMazeObject target1 = m.getCell(destX, destY, destZ, destW,
                    Maze.LAYER_GROUND);
            final AbstractMazeObject target2 = m.getCell(destX, destY, destZ, destW,
                    Maze.LAYER_OBJECT);
            target1.determineCurrentAppearance(destX, destY, destZ, destW);
            target2.determineCurrentAppearance(destX, destY, destZ, destW);
            final String gameName1 = target1.getGameName();
            final String gameName2 = target2.getGameName();
            Messager.showMessage(gameName2 + " on " + gameName1);
            if (FantastleReboot.getBagOStuff().getPrefsManager()
                    .getSoundEnabled(PreferencesManager.SOUNDS_GAME)) {
                AbstractMazeObject.playIdentifySound();
            }
        } catch (final ArrayIndexOutOfBoundsException ae) {
            final EmptyVoid ev = new EmptyVoid();
            ev.determineCurrentAppearance(destX, destY, destZ, destW);
            Messager.showMessage(ev.getGameName());
            if (FantastleReboot.getBagOStuff().getPrefsManager()
                    .getSoundEnabled(PreferencesManager.SOUNDS_GAME)) {
                AbstractMazeObject.playIdentifySound();
            }
        }
    }

    public void loadGameHook(final XDataReader mazeFile,
            final int formatVersion) throws IOException {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        this.objectInv = ObjectInventory.readInventory(mazeFile, formatVersion);
        app.getMazeManager().getMaze().readSavedMazeState(mazeFile,
                formatVersion);
        this.savedObjectInv = ObjectInventory.readInventory(mazeFile,
                formatVersion);
        app.getMazeManager().setScoresFileName(mazeFile.readString());
        this.st.setScore(mazeFile.readLong());
    }

    public void saveGameHook(final XDataWriter mazeFile) throws IOException {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        this.objectInv.writeInventory(mazeFile);
        app.getMazeManager().getMaze().writeSavedMazeState(mazeFile);
        this.savedObjectInv.writeInventory(mazeFile);
        mazeFile.writeString(app.getMazeManager().getScoresFileName());
        mazeFile.writeLong(this.st.getScore());
    }

    public void playMaze() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        if (app.getMazeManager().getLoaded()) {
            app.getGUIManager().hideGUI();
            app.setInGame(true);
            this.savedAbstractMazeObject = new Empty();
            if (this.savedGameFlag) {
                this.st.setScoreFile(app.getMazeManager().getScoresFileName());
            } else {
                this.saveObjectInventory();
                this.st.resetScore(app.getMazeManager().getScoresFileName());
            }
            // Make sure message area is attached to the border pane
            this.borderPane.removeAll();
            this.borderPane.add(this.outputPane, BorderLayout.CENTER);
            this.borderPane.add(this.messageLabel, BorderLayout.SOUTH);
            this.borderPane.add(this.sg.getStatsPane(), BorderLayout.EAST);
            this.gameOverMessage = null;
            app.playStartSound();
            this.showOutput();
            this.redrawMaze();
        } else {
            Messager.showDialog("No Maze Opened");
        }
    }

    public void showOutput() {
        final BagOStuff app = FantastleReboot.getBagOStuff();
        app.getMenuManager().setGameMenus();
        this.outputFrame.setVisible(true);
        app.getMenuManager().attachMenus();
    }

    public void hideOutput() {
        if (this.outputFrame != null) {
            this.outputFrame.setVisible(false);
        }
    }

    private void setUpGUI() {
        this.objectInv = new ObjectInventory();
        this.handler = new EventHandler();
        this.borderPane = new Container();
        this.borderPane.setLayout(new BorderLayout());
        this.messageLabel = new JLabel(" ");
        this.messageLabel.setOpaque(true);
        this.outputFrame = new JFrame("Fantastle");
        this.outputPane = new Container();
        this.outputFrame.setContentPane(this.borderPane);
        this.outputFrame
                .setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.outputPane.setLayout(
                new GridLayout(GameViewingWindowManager.getViewingWindowSizeX(),
                        GameViewingWindowManager.getViewingWindowSizeY()));
        this.outputFrame.setResizable(false);
        this.outputFrame.addKeyListener(this.handler);
        this.outputFrame.addWindowListener(this.handler);
        this.outputPane.addMouseListener(this.handler);
        this.drawGrid = new JLabel[GameViewingWindowManager
                .getViewingWindowSizeX()][GameViewingWindowManager
                        .getViewingWindowSizeY()];
        for (int x = 0; x < GameViewingWindowManager
                .getViewingWindowSizeX(); x++) {
            for (int y = 0; y < GameViewingWindowManager
                    .getViewingWindowSizeY(); y++) {
                this.drawGrid[x][y] = new JLabel();
                // Mac OS X-specific fix to make draw grid line up properly
                if (System.getProperty("os.name").startsWith("Mac OS X")) {
                    this.drawGrid[x][y].setBorder(new EmptyBorder(0, 0, 0, 0));
                }
                this.outputPane.add(this.drawGrid[x][y]);
            }
        }
        this.borderPane.add(this.outputPane, BorderLayout.CENTER);
        this.borderPane.add(this.messageLabel, BorderLayout.SOUTH);
        this.borderPane.add(this.sg.getStatsPane(), BorderLayout.EAST);
    }

    private class EventHandler
            implements KeyListener, WindowListener, MouseListener {
        public EventHandler() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            if (!GameManager.this.arrowActive) {
                if (!FantastleReboot.getBagOStuff().getPrefsManager()
                        .oneMove()) {
                    if (e.isAltDown()) {
                        this.handleArrows(e);
                    } else {
                        this.handleMovement(e);
                    }
                }
            }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            if (!GameManager.this.arrowActive) {
                if (FantastleReboot.getBagOStuff().getPrefsManager()
                        .oneMove()) {
                    if (e.isAltDown()) {
                        this.handleArrows(e);
                    } else {
                        this.handleMovement(e);
                    }
                }
            }
        }

        @Override
        public void keyTyped(final KeyEvent e) {
            // Do nothing
        }

        public void handleMovement(final KeyEvent e) {
            try {
                final GameManager gm = GameManager.this;
                final int keyCode = e.getKeyCode();
                if (e.isShiftDown()) {
                    gm.setPullInProgress(true);
                }
                switch (keyCode) {
                case KeyEvent.VK_NUMPAD4:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(-1, 0);
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_X:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(0, 1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD6:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(1, 0);
                    }
                    break;
                case KeyEvent.VK_NUMPAD8:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(0, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD7:
                case KeyEvent.VK_Q:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(-1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD9:
                case KeyEvent.VK_E:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                case KeyEvent.VK_C:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(1, 1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD1:
                case KeyEvent.VK_Z:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(-1, 1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD5:
                case KeyEvent.VK_S:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.updatePositionRelative(0, 0);
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (gm.usingAnItem()) {
                        gm.setUsingAnItem(false);
                        Messager.showMessage(" ");
                    } else if (gm.isTeleporting()) {
                        gm.teleporting = false;
                        Messager.showMessage(" ");
                    }
                    break;
                default:
                    break;
                }
                if (gm.isPullInProgress()) {
                    gm.setPullInProgress(false);
                }
            } catch (final Exception ex) {
                FantastleReboot.logError(ex);
            }
        }

        public void handleArrows(final KeyEvent e) {
            try {
                final GameManager gm = GameManager.this;
                final int keyCode = e.getKeyCode();
                if (e.isShiftDown()) {
                    gm.setPullInProgress(true);
                }
                switch (keyCode) {
                case KeyEvent.VK_NUMPAD4:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(-1, 0);
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_X:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(0, 1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD6:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(1, 0);
                    }
                    break;
                case KeyEvent.VK_NUMPAD8:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(0, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD7:
                case KeyEvent.VK_Q:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(-1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD9:
                case KeyEvent.VK_E:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                case KeyEvent.VK_C:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(1, 1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD1:
                case KeyEvent.VK_Z:
                    if (!gm.usingAnItem() && !gm.isTeleporting()) {
                        gm.fireArrow(-1, 1);
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (gm.usingAnItem()) {
                        gm.setUsingAnItem(false);
                        Messager.showMessage(" ");
                    } else if (gm.isTeleporting()) {
                        gm.teleporting = false;
                        Messager.showMessage(" ");
                    }
                    break;
                default:
                    break;
                }
                if (gm.isPullInProgress()) {
                    gm.setPullInProgress(false);
                }
            } catch (final Exception ex) {
                FantastleReboot.logError(ex);
            }
        }

        // Handle windows
        @Override
        public void windowActivated(final WindowEvent we) {
            // Do nothing
        }

        @Override
        public void windowClosed(final WindowEvent we) {
            // Do nothing
        }

        @Override
        public void windowClosing(final WindowEvent we) {
            try {
                final BagOStuff app = FantastleReboot.getBagOStuff();
                boolean success = false;
                int status = 0;
                if (app.getMazeManager().getDirty()) {
                    app.getMazeManager();
                    status = com.puttysoftware.fantastlereboot.maze.MazeManager.showSaveDialog();
                    if (status == JOptionPane.YES_OPTION) {
                        success = app.getMazeManager().saveMaze();
                        if (success) {
                            app.getGameManager().solvedMaze();
                        }
                    } else if (status == JOptionPane.NO_OPTION) {
                        app.getGameManager().solvedMaze();
                    }
                } else {
                    app.getGameManager().solvedMaze();
                }
            } catch (final Exception ex) {
                FantastleReboot.logError(ex);
            }
        }

        @Override
        public void windowDeactivated(final WindowEvent we) {
            // Do nothing
        }

        @Override
        public void windowDeiconified(final WindowEvent we) {
            // Do nothing
        }

        @Override
        public void windowIconified(final WindowEvent we) {
            // Do nothing
        }

        @Override
        public void windowOpened(final WindowEvent we) {
            // Do nothing
        }

        // handle mouse
        @Override
        public void mousePressed(final MouseEvent e) {
            // Do nothing
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            // Do nothing
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            try {
                final GameManager gm = GameManager.this;
                if (gm.usingAnItem()) {
                    final int x = e.getX();
                    final int y = e.getY();
                    gm.useItemHandler(x, y);
                    gm.setUsingAnItem(false);
                } else if (e.isShiftDown()) {
                    final int x = e.getX();
                    final int y = e.getY();
                    gm.identifyObject(x, y);
                } else if (gm.isTeleporting()) {
                    final int x = e.getX();
                    final int y = e.getY();
                    gm.controllableTeleportHandler(x, y);
                }
            } catch (final Exception ex) {
                FantastleReboot.logError(ex);
            }
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            // Do nothing
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            // Do nothing
        }
    }
}
