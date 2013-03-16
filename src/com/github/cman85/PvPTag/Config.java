package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.event.entity.*;

import java.util.*;

public class Config {

   private static PvPTag pvptag;
   private Set<String> disabledWorlds = new HashSet<String>();

   public Config(PvPTag pvptag){
      this.pvptag = pvptag;
   }

   public FileConfiguration getConfig(){
      return pvptag.getConfig();
   }

   public void enable(){
      tryUpdate();
      disabledWorlds();
   }

   private void disabledWorlds(){
      String[] disabled = getConfig().getString("Tagging.Disabled Worlds").split(",");
      for(String s : disabled)
         disabledWorlds.add(s);
   }

   private void tryUpdate(){
/*      if(! config.getString("version").equalsIgnoreCase(pvptag.version)){
         PvPTag.log(Level.INFO, "Updating config!");
         copy(pvptag.getResource("config.yml"), configFile);
         FileConfiguration newConfig = new YamlConfiguration();
         newConfig.load(configFile);
         for(String s : config.getKeys(true)){
            if(! s.equalsIgnoreCase("version"))
               newConfig.set(s, config.get(s));
            else
               newConfig.set(s, newConfig.get(s));
         }
         this.config = newConfig;
         saveYamls();
      }else{
         PvPTag.log(Level.INFO, "Config file up to date.");
      }
      */
      pvptag.getConfig().options().copyDefaults(false);
      pvptag.saveConfig();
   }

   public void disable(){
      pvptag.saveConfig();
   }

   public ChatColor parseNameTagColor(){
      return ChatColor.getByChar(getConfig().getString("Tagging.NameTag Color"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e){
      return disabledWorlds.contains(e.getEntity().getWorld().getName());
   }
}
