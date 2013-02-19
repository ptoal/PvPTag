package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.*;
import org.kitteh.tag.*;

import java.util.*;
import java.util.logging.*;

public class PvPTag extends JavaPlugin implements Listener {

   HashMap<String, Long> safeTimes = new HashMap<String, Long>();
   HashMap<String, Long> deathTimes = new HashMap<String, Long>();
   private static Logger logger;
   private long SAFE_DELAY = 30000;
   private long DEATH_TP_DELAY = 30000;
   private DeathChestListener dcl;
   public static String version;

   public void onEnable(){
      String p = this.getServer().getClass().getPackage().getName();
      version = p.substring(p.lastIndexOf('.') + 1);
      logger = getLogger();
      dcl = new DeathChestListener(this);
      getServer().getPluginManager().registerEvents(this, this);
      getServer().getPluginManager().registerEvents(dcl, this);
      task();
      getServer();
   }

   public void onDisable(){
      callSafeAllManual();
      dcl.breakAll();
   }

   public void task(){
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         public void run(){
            resetNameTagsAuto();
         }
      }, 20L, 20L);
   }

   private void callSafeAllManual(){
      Iterator<String> iter = safeTimes.keySet().iterator();
      while(iter.hasNext()){
         String s = iter.next();
         iter.remove();
         callSafe(getServer().getPlayer(s));
      }
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
            TagAPI.refreshPlayer(player);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onHit(EntityDamageByEntityEvent e){
      if(e.getDamager() instanceof Snowball) e.setCancelled(true);
      if(! e.getEntity().getWorld().getName().equalsIgnoreCase("ArenaWorld")){
         if(e.getEntity() instanceof Player){
            Player hitter;
            Player hitted = (Player)e.getEntity();
            if(e.getDamager() instanceof Arrow){
               Arrow arrow = (Arrow)e.getDamager();
               if(arrow.getShooter() instanceof Player){
                  hitter = (Player)arrow.getShooter();
               }else{
                  return;
               }
            }else if(e.getDamager() instanceof Player){
               hitter = (Player)e.getDamager();

            }else{
               return;
            }
            if(! e.isCancelled()){
               if(isSafe(hitted.getName())){
                  addUnsafe(hitted);
               }
               if(isSafe(hitter.getName())){
                  addUnsafe(hitter);
               }
               safeTimes.put(hitted.getName(), calcSafeTime(SAFE_DELAY));
               safeTimes.put(hitter.getName(), calcSafeTime(SAFE_DELAY));
            }else{
               if(! isSafe(hitted.getName()) && hitter.getInventory().getItemInHand() != null){
                  e.setCancelled(false);
                  safeTimes.put(hitted.getName(), calcSafeTime(SAFE_DELAY));
                  safeTimes.put(hitter.getName(), calcSafeTime(SAFE_DELAY));
                  if(isSafe(hitter.getName())){
                     addUnsafe(hitter);
                  }
               }
            }

         }
      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
      if(cmd.getName().equalsIgnoreCase("callsafe") || cmd.getName().equalsIgnoreCase("csafe")){
         if(sender.isOp()){
            if(args.length == 1){
               if(! args[0].equalsIgnoreCase("all")){
                  Player p = getServer().getPlayer(args[0]);
                  if(p == null){
                     sender.sendMessage("§cYou must specify an online player.");
                     return true;
                  }else{
                     if(! isSafe(p.getName())){
                        callSafe(p);
                        sender.sendMessage("§c" + p.getName() + " is no longer hittable.");
                        TagAPI.refreshPlayer(p);
                        return true;
                     }else{
                        sender.sendMessage("§c" + p.getName() + " was not hittable.");
                        return true;
                     }
                  }
               }else{
                  callSafeAllManual();
               }
            }else{
               sender.sendMessage("§cUsage: /callsafe [name] or /callsafe all");
            }
         }else{
            sender.sendMessage("§cYou must be an operator to use this command.");
         }
      }else if(cmd.getName().equalsIgnoreCase("callhit") || cmd.getName().equalsIgnoreCase("ch")){
         if(sender.isOp()){
            Player p;
            if(args.length != 1)
               return false;
            else{
               p = getServer().getPlayer(args[0]);
               if(p == null){
                  sender.sendMessage("§cYou must specify an online player.");
                  return true;
               }
            }

            if(isSafe(p.getName())){
               p.damage(1);
               addUnsafe(p);
            }
         }
      }
      return true;
   }

   private long calcSafeTime(Long time){
      return System.currentTimeMillis() + time;
   }

   private void addUnsafe(Player p){
      safeTimes.put(p.getName(), calcSafeTime(SAFE_DELAY));
      p.sendMessage("§cYou can now be hit anywhere for at least 30 seconds!");
      TagAPI.refreshPlayer(p);
   }

   private void callSafe(Player player){
      if(player != null){
         safeTimes.remove(player.getName());
         TagAPI.refreshPlayer(player);
         player.sendMessage("§cYou are no longer hittable.");
      }
   }

   public boolean isSafe(String player){
      if(safeTimes.containsKey(player)){
         return (safeTimes.get(player) < System.currentTimeMillis());
      }
      return true;
   }

   public static void log(Level level, String message){
      logger.log(level, message);
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent e){
      if(! isSafe(e.getEntity().getName())){
         callSafe(e.getEntity());
      }
      deathTimes.put(e.getEntity().getName(), calcSafeTime(DEATH_TP_DELAY));

   }

   @EventHandler
   public void onNameTag(PlayerReceiveNameTagEvent e){
      if(! isSafe(e.getNamedPlayer().getName())){
         Player p = e.getNamedPlayer();
         e.setTag(ChatColor.DARK_RED + p.getName());
      }else{
         if(e.getNamedPlayer().isOnline())
            e.setTag(e.getNamedPlayer().getName());
      }
   }

   @EventHandler(priority = EventPriority.HIGH)
   public void onTp(PlayerCommandPreprocessEvent e){
      if(e.getMessage().toLowerCase().trim().contains("/warp arena")){
         if(deathTimes.containsKey(e.getPlayer().getName())){
            Long deathTime = deathTimes.get(e.getPlayer().getName());
            Long currTime = System.currentTimeMillis();
            if(deathTime > currTime){
               e.getPlayer().sendMessage("§cYou cannot teleport to the arena for 30 seconds after dying. Time left: §6" + (30 - (deathTime / 1000 - currTime / 1000)));
               e.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGH)
   public void onTpEvent(PlayerTeleportEvent ev){
      if(! isSafe(ev.getPlayer().getName()) && ! ev.getPlayer().isOp()){
         ev.setCancelled(true);
         ev.getPlayer().sendMessage(ChatColor.RED + "You cannot teleport until you are safe.");
      }
   }

   @SuppressWarnings("deprecation")
   @EventHandler
   public void onJoin(PlayerJoinEvent e){
      if(PvPLoggerZombie.waitingToDie.contains(e.getPlayer().getName())){
         e.getPlayer().getInventory().clear();
         e.getPlayer().setHealth(0);
         e.getPlayer().updateInventory();
         PvPLoggerZombie.waitingToDie.remove(e.getPlayer().getName());
      }
      PvPLoggerZombie pz = PvPLoggerZombie.getByOwner(e.getPlayer().getName());
      if(pz != null){
         pz.despawnNoDrop(true, true);
      }
   }

   @EventHandler
   public void entityDeath(EntityDeathEvent e){
      if(e.getEntity() instanceof Zombie){
         PvPLoggerZombie pz = PvPLoggerZombie.getByZombie((Zombie)e.getEntity());
         if(pz != null){
            PvPLoggerZombie.waitingToDie.add(pz.getPlayer());
            pz.despawnDrop(true);
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e){
      if(! isSafe(e.getPlayer().getName())){
         System.out.println(e.getPlayer().getName() + " Has logged out unsafe");
         Zombie z = (Zombie)e.getPlayer().getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.ZOMBIE);
         new PvPLoggerZombie(e.getPlayer().getName(), z);
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onCreatue(CreatureSpawnEvent e){
      if(e.getEntity() instanceof Zombie){
         PvPLoggerZombie pz = PvPLoggerZombie.getByZombie((Zombie)e.getEntity());
         if(pz != null){
            System.out.println("Creature Spawn Event uncancelled");
            e.setCancelled(false);
         }
      }
   }

}