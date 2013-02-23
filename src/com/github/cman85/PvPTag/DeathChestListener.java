package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;

public class DeathChestListener implements Listener {

   private PvPTag plugin;
   private Set<DeathChest> chests = new HashSet<DeathChest>();

   public DeathChestListener(PvPTag plugin){
      this.plugin = plugin;
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onChestOpen(PlayerInteractEvent e){
      if(e.getClickedBlock() != null){
         if(e.getClickedBlock().getType() == Material.CHEST){
            Block clickedChest = e.getClickedBlock();
            DeathChest dc = getDeathChest(clickedChest);
            if(dc != null){

               if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                  if(dc.canAccess(e.getPlayer().getName())){
                     //Chest Has been accessed successfully.
                     if(e.isCancelled()) e.setCancelled(false);
                  }else{
                     //Chest isn't theirs.
                     e.setCancelled(true);
                     e.getPlayer().sendMessage("§cYou cannot access this death chest as you did not kill the person who dropped it. \nTry to left click it and see if it's time is up.");
                  }

               }else if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                  if(dc.canAccess(e.getPlayer().getName())){
                     //Time is up.
                     if(dc.getTimeLeft() - 5 < 40){
                        clickedChest.breakNaturally();
                        chests.remove(dc);
                     }else{
                        e.getPlayer().sendMessage("§cThe chest was not broken because it has just spawned.");
                     }
                  }else{
                     //Time isn't up.
                     e.getPlayer().sendMessage("§cThis chest's time isn't up yet. The time left is: " + dc.getTimeLeft() + " seconds.");
                  }
               }else{
                  //This chest isn't logged by this plugin.
               }

            }
         }
      }
   }

   @EventHandler
   public void onDeathChest(PlayerDeathEvent e){

      Player dead = e.getEntity();
      Player killer;
      if(e.getEntity().getKiller() != null){
         killer = e.getEntity().getKiller();
      }else{
         return;
      }

      if(dead.getInventory().getContents().length != 0){
         //Player has items.
         if(isEligibleForChest(dead)){
            //Player had armor

            Location chestLoc;
            if(dead.getLocation().getBlock().getTypeId() == 0)
               chestLoc = dead.getLocation();
            else{
               if(dead.getEyeLocation().getBlock().getTypeId() == 0)
                  chestLoc = dead.getEyeLocation();
               else
                  return;
            }
            if(isChestNear(chestLoc.getBlock())) return;

            Block chest = e.getEntity().getWorld().getBlockAt(chestLoc);
            chest.setType(Material.CHEST);

            Block aboveChest = chest.getRelative(BlockFace.UP);
            Location aboveChestLoc = aboveChest.getLocation();

            Chest dc = (Chest)chest.getState();

            DeathChest deathChest = new DeathChest(dead.getName(), killer.getName(), dc.getLocation());
            chests.add(deathChest);

            e.getDrops().clear();
            ItemStack[] loadedItems = null;
            List<ItemStack> addtoChest = new ArrayList<ItemStack>();

            for(ItemStack is : dead.getInventory().getArmorContents()){
               if(is != null && is.getTypeId() != 0){
                  addtoChest.add(is);
               }
               loadedItems = addtoChest.toArray(new ItemStack[addtoChest.size()]);
               dead.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
            }
            for(ItemStack istack : dead.getInventory()){
               if(istack != null && istack.getTypeId() != 0 && loadedItems != null){
                  if(loadedItems.length < 27){
                     addtoChest.add(istack);
                     loadedItems = addtoChest.toArray(new ItemStack[addtoChest.size()]);
                  }else{
                     dead.getWorld().dropItemNaturally(aboveChestLoc, istack);
                  }
               }
            }
            dc.getInventory().setContents(loadedItems);
         }
      }
   }

   public boolean isChestNear(final Block fb){
      if(fb.getRelative(BlockFace.NORTH).getType() == Material.CHEST) return true;
      if(fb.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) return true;
      if(fb.getRelative(BlockFace.EAST).getType() == Material.CHEST) return true;
      if(fb.getRelative(BlockFace.WEST).getType() == Material.CHEST) return true;
      return false;
   }

   private boolean isEligibleForChest(Player dead){
      return dead.getInventory().getBoots() != null || dead.getInventory().getHelmet() != null || dead.getInventory().getLeggings() != null || dead.getInventory().getChestplate() != null ||
          dead.getInventory().contains(Material.DIAMOND_SWORD) || dead.getInventory().contains(Material.DIAMOND_PICKAXE) || dead.getInventory().contains(Material.DIAMOND_SPADE) ||
          dead.getInventory().contains(Material.IRON_SWORD) || dead.getInventory().contains(Material.IRON_PICKAXE) || dead.getInventory().contains(Material.IRON_SPADE);
   }

   private DeathChest getDeathChest(Block b){
      for(DeathChest dc : chests){
         if(dc.getChestLoc().equals(b.getLocation())) return dc;
      }
      return null;
   }

   public void breakAll(){
      for(DeathChest dc : chests)
         dc.getChestLoc().getBlock().breakNaturally();
      chests.clear();
   }

}
