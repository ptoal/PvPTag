package com.github.cman85.PvPTag;

import org.bukkit.*;

public class DeathChest {

   private String dead;
   private String killer;
   private long openTime;
   private Location chestLoc;
   private static long CHEST_BREAK_DELAY = 45000;

   public DeathChest(String dead, String killer, Location chestLoc){
      this.dead = dead;
      this.killer = killer;
      this.chestLoc = chestLoc;
      this.openTime = System.currentTimeMillis() + DeathChest.CHEST_BREAK_DELAY;
   }

   public boolean canAccess(String player){
      return (isTimeUp() || player.equalsIgnoreCase(dead) || player.equalsIgnoreCase(killer) || Bukkit.getServer().getPlayer(player).isOp());
   }

   private boolean isTimeUp(){
      return openTime < System.currentTimeMillis();
   }

   public Location getChestLoc(){
      return chestLoc;
   }

   public int getTimeLeft(){
      return (int)((openTime / 1000 - System.currentTimeMillis() / 1000));
   }

   public String getOwners(){
      return dead + ":" + killer;
   }
}