/*  FantastleReboot: An RPG
Copyright (C) 2008-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.fantastlereboot.game;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.puttysoftware.diane.gui.MainWindow;
import com.puttysoftware.fantastlereboot.FantastleReboot;
import com.puttysoftware.fantastlereboot.creatures.party.PartyManager;
import com.puttysoftware.fantastlereboot.maze.Maze;
import com.puttysoftware.fantastlereboot.maze.MazeManager;

public class LevelLoadTask extends Thread {
  // Fields
  private MainWindow loadFrame;
  private final int level;
  private final JPanel content = new JPanel();

  // Constructors
  public LevelLoadTask(final int offset) {
    this.level = offset;
    this.setName("Level Loader");
    final JProgressBar loadBar = new JProgressBar();
    loadBar.setIndeterminate(true);
    this.content.add(loadBar);
  }

  // Methods
  @Override
  public void run() {
    try {
      this.loadFrame = MainWindow.getOutputFrame();
      this.loadFrame.setTitle("Loading...");
      this.loadFrame.attachContent(this.content);
      this.loadFrame.pack();
      final Maze gameMaze = MazeManager.getMaze();
      Game.disableEvents();
      gameMaze.switchLevelOffset(this.level);
      gameMaze.offsetPlayerLocationW(this.level);
      PartyManager.getParty().offsetMonsterLevel(this.level);
      Game.resetViewingWindow();
      Game.enableEvents();
      Game.showOutput();
      Game.redrawMaze();
    } catch (final Exception ex) {
      FantastleReboot.exception(ex);
    }
  }
}
