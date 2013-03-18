package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.event.entity.*;

import java.io.*;
import java.util.*;

public class Config {

   private static PvPTag pvptag;
   private Set<String> disabledWorlds = new HashSet<String>();
   private Set<String> bannedCommands = new HashSet<String>();

   public Config(PvPTag pvptag){
      this.pvptag = pvptag;
   }

   public FileConfiguration getConfig(){
      return pvptag.getConfig();
   }

   public void enable(){
      if(! pvptag.getDataFolder().exists()){
         pvptag.getConfig();
         pvptag.saveDefaultConfig();
         pvptag.reloadConfig();

      }
      tryUpdate();

      disabledWorlds();
      bannedCommands();
   }

   private void bannedCommands(){
      String[] banned = getConfig().getString("Tagging.Disabled Commands").split(",");
      for(String s : banned)
         bannedCommands.add(s);
   }

   private static void copy(InputStream in, File file){
      try{
         OutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];
         int len;
         while((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
         }
         out.close();
         in.close();
      }catch (Exception e){
         e.printStackTrace();
      }
   }

   private void disabledWorlds(){
      String[] disabled = getConfig().getString("Tagging.Disabled Worlds").split(",");
      for(String s : disabled)
         disabledWorlds.add(s);
   }

   private void tryUpdate(){
      pvptag.getConfig().options().copyDefaults(false);
      pvptag.saveConfig();
   }

   public void disable(){
      pvptag.saveConfig();
   }

   public void reload(){
      pvptag.reloadConfig();
      pvptag.manageConfig();
   }

   public ChatColor parseNameTagColor(){
      return ChatColor.getByChar(getConfig().getString("Tagging.NameTag Color"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e){
      return disabledWorlds.contains(e.getEntity().getWorld().getName());
   }

   public boolean isBannedCommand(String command){
      for(String s : bannedCommands){
         if(command.startsWith("/" + s)) return true;
      }
      return false;
   }
}
