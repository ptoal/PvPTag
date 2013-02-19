package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_4_R1.entity.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.logging.*;

public class PvPLoggerZombie {
   public static Set<PvPLoggerZombie> zombies = new HashSet<PvPLoggerZombie>();
   public static Set<String> waitingToDie = new HashSet<String>();
   private Zombie zombie;
   private String player;
   private PlayerInventory contents;

   public PvPLoggerZombie(String player, Zombie zombie){
      this.player = player;
      this.zombie = zombie;
      zombie.getWorld().playEffect(zombie.getLocation(), Effect.MOBSPAWNER_FLAMES, 1, 1);
      zombie.setRemoveWhenFarAway(false);
      setInventoryContents(Bukkit.getPlayer(player).getInventory());
      if(PvPTag.version.equalsIgnoreCase("1_4_R1")){
         ((CraftZombie)zombie).getHandle().bh = 0.46f;
      }
      Iterator<PvPLoggerZombie> it = zombies.iterator();
      while(it.hasNext()){
         PvPLoggerZombie pz = it.next();
         if(pz.getPlayer().equalsIgnoreCase(player)){
            despawnDrop(false);
            it.remove();
         }
      }
      zombies.add(this);
   }

   public Zombie getZombie(){
      return zombie;
   }

   public void setZombie(Zombie zombie){
      this.zombie = zombie;
   }

   public String getPlayer(){
      return player;
   }

   public void setPlayer(String player){
      this.player = player;
   }

   public void setInventoryContents(PlayerInventory pi){
      zombie.setMaxHealth(50);
      zombie.setHealth(50);
      zombie.setRemoveWhenFarAway(false);
      zombie.setCanPickupItems(false);
      zombie.getEquipment().setArmorContents(pi.getArmorContents());
      zombie.getEquipment().setItemInHand(pi.getItemInHand());
      zombie.getEquipment().setBootsDropChance(100);
      zombie.getEquipment().setChestplateDropChance(100);
      zombie.getEquipment().setHelmetDropChance(100);
      zombie.getEquipment().setLeggingsDropChance(100);
      zombie.getEquipment().setItemInHandDropChance(100);
      pi.setArmorContents(new ItemStack[]{null, null, null, null});
      pi.setItemInHand(null);
      this.contents = pi;
   }

   public List<ItemStack> itemsToDrop(){
      List<ItemStack> itemsToDrop = new ArrayList<ItemStack>();
      for(ItemStack i : contents.getContents()){
         if(i != null) itemsToDrop.add(i);
      }
      return itemsToDrop;
   }

   public void despawnNoDrop(boolean giveToOwner, boolean iterate){
      zombie.getEquipment().setBootsDropChance(0);
      zombie.getEquipment().setChestplateDropChance(0);
      zombie.getEquipment().setHelmetDropChance(0);
      zombie.getEquipment().setLeggingsDropChance(0);
      zombie.getEquipment().setItemInHandDropChance(0);
      if(giveToOwner){
         Player p = Bukkit.getPlayer(player);
         if(p == null){
            PvPTag.log(Level.WARNING, "Player was null!");
            return;
         }
         p.getInventory().setContents(contents.getContents());
         p.getInventory().setArmorContents(zombie.getEquipment().getArmorContents());
         p.getInventory().setItemInHand(zombie.getEquipment().getItemInHand());
      }
      zombie.remove();
      if(iterate)
         despawn();
   }

   public void despawn(){
      Iterator<PvPLoggerZombie> it = zombies.iterator();
      while(it.hasNext()){
         PvPLoggerZombie pz = it.next();
         if(pz.getPlayer().equalsIgnoreCase(player)) it.remove();
      }
   }

   public void despawnDrop(boolean iterate){
      zombie.setCanPickupItems(false);
      for(ItemStack is : contents.getContents()){
         if(is != null)
            zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
      }
      zombie.getWorld().playEffect(zombie.getLocation(), Effect.ENDER_SIGNAL, 1, 1);
      zombie.setHealth(0);
      zombie.remove();
      if(iterate)
         despawn();
   }

   public static PvPLoggerZombie getByOwner(String owner){
      for(PvPLoggerZombie pz : zombies){
         if(pz.getPlayer().equalsIgnoreCase(owner)) return pz;
      }
      return null;
   }

   public static PvPLoggerZombie getByZombie(Zombie z){
      for(PvPLoggerZombie pz : zombies){
         if(pz.getZombie().equals(z)) return pz;
      }
      return null;
   }
}
