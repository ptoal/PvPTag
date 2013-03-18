package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;

public class PvPTagListener implements Listener {
   private PvPTag pvptag;
   private long lastLogout = System.currentTimeMillis();

   public PvPTagListener(PvPTag pt){
      this.pvptag = pt;
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onHit(EntityDamageByEntityEvent e){
      if(! pvptag.configuration.isPVPWorld(e)) return;
      if(! pvptag.taggingEnabled) return;
      if(e.getDamager() instanceof Snowball) e.setCancelled(true);
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

         }else if(e.getDamager() instanceof Zombie){
            if(PvPLoggerZombie.isPvPZombie((Zombie)e.getDamager())){
               if(pvptag.isSafe(hitted.getName())) e.setCancelled(true);
            }
            return;
         }else{
            return;
         }
         if(! e.isCancelled()){
            if(pvptag.isSafe(hitted.getName())){
               pvptag.addUnsafe(hitted);
            }
            if(pvptag.isSafe(hitter.getName())){
               pvptag.addUnsafe(hitter);
            }
         }else{
            if(! pvptag.isSafe(hitted.getName()) && hitter.getInventory().getItemInHand() != null){
               if(pvptag.isSafe(hitter.getName())){
                  if(! pvptag.antiPilejump){
                     e.setCancelled(false);
                     pvptag.addUnsafe(hitter);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onDeath(PlayerRespawnEvent e){
      pvptag.safeTimes.remove(e.getPlayer().getName());
      if(pvptag.useDeathTP)
         pvptag.deathTimes.put(e.getPlayer().getName(), pvptag.calcSafeTime(pvptag.DEATH_TP_DELAY));
   }

   @EventHandler(priority = EventPriority.HIGH)
   public void onTpEvent(PlayerTeleportEvent e){
      if(! pvptag.isSafe(e.getPlayer().getName()) && ! e.getPlayer().isOp()){
         e.setCancelled(true);
         e.getPlayer().sendMessage(ChatColor.RED + "You cannot teleport until you are safe.");
      }else{
         if(pvptag.deathTimes.containsKey(e.getPlayer().getName()) && pvptag.useDeathTP){
            Long deathTime = pvptag.deathTimes.get(e.getPlayer().getName());
            Long currTime = System.currentTimeMillis();
            if(deathTime > currTime){
               e.getPlayer().sendMessage("§cYou cannot teleport for " + (pvptag.DEATH_TP_DELAY / 1000) + " seconds after dying. Time left: §6" + (deathTime / 1000 - currTime / 1000));
               e.setCancelled(true);
            }else{

            }
         }
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
         pvptag.addUnsafe(e.getPlayer());
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

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onFlight(PlayerToggleFlightEvent e){
      if(pvptag.disableFlight && ! pvptag.isSafe(e.getPlayer().getName())){
         e.getPlayer().setFlying(false);
         e.getPlayer().setAllowFlight(false);
         e.setCancelled(true);
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e){
      if(! pvptag.isSafe(e.getPlayer().getName()) && pvptag.pvpZombEnabled){
         lastLogout = System.currentTimeMillis();
         new PvPLoggerZombie(e.getPlayer().getName());
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onCreature(CreatureSpawnEvent e){
      if(e.getEntity() instanceof Zombie){
         if(System.currentTimeMillis() - lastLogout < 20){
            e.setCancelled(false);
         }else{
         }
      }
   }

   @EventHandler
   public void onChunk(ChunkUnloadEvent e){
      Chunk c = e.getChunk();
      for(Entity en : c.getEntities()){
         if(en.getType() == EntityType.ZOMBIE){
            Zombie z = (Zombie)en;
            if(PvPLoggerZombie.isPvPZombie(z)){
               PvPLoggerZombie pz = PvPLoggerZombie.getByZombie(z);
               pz.despawnDrop(true);
               pz.killOwner();
            }
         }
      }
   }

   @EventHandler
   public void onProject(ProjectileLaunchEvent e){
      if(pvptag.disableEnderpearls)
         if(e.getEntity() instanceof EnderPearl){
            EnderPearl pearl = (EnderPearl)e.getEntity();
            if(pearl.getShooter() instanceof Player){
               Player p = (Player)pearl.getShooter();
               if(! pvptag.isSafe(p.getName())) e.setCancelled(true);
            }
         }
   }

   @EventHandler
   public void command(PlayerCommandPreprocessEvent e){
      if(pvptag.configuration.isBannedCommand(e.getMessage()) && ! pvptag.isSafe(e.getPlayer().getName()))
         e.setCancelled(true);
   }

}
