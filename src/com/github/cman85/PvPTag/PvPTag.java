package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.plugin.java.*;
import org.bukkit.potion.*;

import java.util.*;
import java.util.logging.*;

public class PvPTag extends JavaPlugin implements Listener {

   private final MainCommandListener mainCommandListener = new MainCommandListener(this);
   HashMap<String, Long> safeTimes = new HashMap<String, Long>();
   HashMap<String, Long> deathTimes = new HashMap<String, Long>();
   private Set<String> couldFly = new HashSet<String>();
   private Set<String> hadFlight = new HashSet<String>();
   private static Logger logger;
   long SAFE_DELAY = 30000;
   long DEATH_TP_DELAY = 30000;
   boolean useDeathTP = true;
   ChatColor nameTagColor;
   boolean disableFlight = true;
   private DeathChestListener dcl;
   private TagAPEye tagApi;
   String version = "1.2.1";
   private Updater updater;
   private boolean unInvis = true;

   public void onEnable(){
      logger = getLogger();
      dcl = new DeathChestListener(this);
      manageConfig();
      getServer().getPluginManager().registerEvents(new PvPTagListener(this), this);
      task();
   }

   void manageConfig(){
      Config.getInstance().enable(this);
      this.SAFE_DELAY = Config.getInstance().getConfig().getInt("Safe Time") * 1000;
      this.DEATH_TP_DELAY = Config.getInstance().getConfig().getInt("DeathTP Time") * 1000;
      DeathChest.CHEST_BREAK_DELAY = Config.getInstance().getConfig().getInt("Chest Time") * 1000;
      useDeathTP = Config.getInstance().getConfig().getBoolean("DeathTP Enabled");
      PvPLoggerZombie.HEALTH = Config.getInstance().getConfig().getInt("PVPLogger Health");
      this.disableFlight = Config.getInstance().getConfig().getBoolean("Disable Flying");
      this.unInvis = Config.getInstance().getConfig().getBoolean("Remove Invisible");
      this.nameTagColor = Config.getInstance().parseNameTagColor();
      if(Config.getInstance().getConfig().getBoolean("DeathChest Enabled"))
         getServer().getPluginManager().registerEvents(dcl, this);
      if(Config.getInstance().getConfig().getBoolean("Use TagAPI") && getServer().getPluginManager().getPlugin("TagAPI") != null){
         this.tagApi = new TagEnabled(this);
      }else{
         this.tagApi = new TagDisabled();
      }
      getServer().getPluginManager().registerEvents(tagApi, this);

      if(Config.getInstance().getConfig().getBoolean("Auto update"))
         updater = new Updater(this, "pvp-tag", this.getFile(), Updater.UpdateType.DEFAULT, false);
   }

   private void resetNameTagsAuto(){
      Iterator<String> iter = safeTimes.keySet().iterator();
      while(iter.hasNext()){
         String s = iter.next();
         Player player = getServer().getPlayer(s);
         if(player == null){
            iter.remove();
         }else if(isSafe(s)){
            iter.remove();
            player.sendMessage("§cYou are now safe.");
            fixFlying(player);
            refresh(player);
         }
      }
   }

   private void fixFlying(Player player){
      if(couldFly.contains(player.getName())){
         couldFly.remove(player.getName());
         player.setAllowFlight(true);
      }
      if(hadFlight.contains(player.getName())){
         hadFlight.remove(player.getName());
         player.setFlying(true);
      }
   }

   void callSafeAllManual(){
      Iterator<String> iter = safeTimes.keySet().iterator();
      while(iter.hasNext()){
         String s = iter.next();
         iter.remove();
         callSafe(getServer().getPlayer(s));
      }
   }

   public void onDisable(){
      Config.getInstance().disable();
      callSafeAllManual();
      dcl.breakAll();
   }

   public void task(){
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         public void run(){
            resetNameTagsAuto();
         }
      }, 40L, 40L);
   }

   void addUnsafe(Player p){
      safeTimes.put(p.getName(), calcSafeTime(SAFE_DELAY));
      p.sendMessage("§cYou can now be hit anywhere for at least " + (SAFE_DELAY / 1000) + " seconds!");
      removeFlight(p);
      refresh(p);
      unInvis(p);
   }

   private void unInvis(Player p){
      if(unInvis) p.removePotionEffect(PotionEffectType.INVISIBILITY);
   }

   private void removeFlight(Player p){
      if(disableFlight && p.getGameMode() != GameMode.CREATIVE){
         if(p.getAllowFlight()) couldFly.add(p.getName());
         if(p.isFlying()) hadFlight.add(p.getName());
         p.setFlying(false);
         p.setAllowFlight(false);
      }
   }

   void callSafe(Player player){
      if(player != null){
         safeTimes.remove(player.getName());
         refresh(player);
         player.sendMessage("§cYou are now safe.");
      }
   }

   public boolean isSafe(String player){
      if(safeTimes.containsKey(player)){
         return (safeTimes.get(player) < System.currentTimeMillis());
      }
      return true;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
      return mainCommandListener.onCommand(sender, cmd, commandLabel, args);
   }

   long calcSafeTime(Long time){
      return System.currentTimeMillis() + time;
   }

   void refresh(Player p){
      tagApi.refresh(p);
   }

   public void setNameTagColor(ChatColor nameTagColor){
      this.nameTagColor = nameTagColor;
   }

   public ChatColor getNameTagColor(){
      return nameTagColor;
   }

   public static void log(Level level, String message){
      logger.log(level, message);
   }
}