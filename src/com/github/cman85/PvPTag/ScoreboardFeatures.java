package com.github.cman85.PvPTag;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardFeatures {
   private Scoreboard board;

   public ScoreboardFeatures(boolean health, boolean safeTime) {
      ScoreboardManager manager = Bukkit.getScoreboardManager();
      board = manager.getNewScoreboard();

      //Broken
      if(false) {
         board.registerNewObjective("displayHealth", "health");
         Objective objective = board.getObjective("displayHealth");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.setDisplayName("/ 20 HP");

      }
      if(safeTime) {
         board.registerNewObjective("displaySafeTime", "dummy");
         Objective objective = board.getObjective("displaySafeTime");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.setDisplayName("Safe Times:");

      }
   }

   public Scoreboard getBoard() {
      return board;
   }
}
