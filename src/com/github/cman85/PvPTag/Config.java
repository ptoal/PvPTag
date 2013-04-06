package com.github.cman85.PvPTag;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class Config {

   private static PvPTag pvptag;
   private Set<String> disabledWorlds = new HashSet<String>();
   private Set<String> bannedCommands = new HashSet<String>();

   public Config(PvPTag pvptag) {
      this.pvptag = pvptag;
   }

   public FileConfiguration getConfig() {
      return pvptag.getConfig();
   }

   public void enable() {
      if(! pvptag.getDataFolder().exists()) {
         pvptag.getConfig();
         pvptag.saveDefaultConfig();
         pvptag.reloadConfig();

      }
      try {
         addDefaultConfig();
      } catch (IOException e) {
         e.printStackTrace();
      }
      tryUpdate();

      disabledWorlds();
      bannedCommands();
   }

   private void addDefaultConfig() throws IOException {
      File file = new File(pvptag.getDataFolder(), "latestConfig.yml");
      if(! file.exists()) {
         file.createNewFile();
      }
      pvptag.getConfig().save(file);
   }

   private void bannedCommands() {
      String[] banned = getConfig().getString("Tagging.Disabled Commands").split(",");
      for(String s : banned)
         bannedCommands.add(s);
   }

   private static void copy(InputStream in, File file) {
      try {
         OutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];
         int len;
         while((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
         }
         out.close();
         in.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void disabledWorlds() {
      String[] disabled = getConfig().getString("Tagging.Disabled Worlds").split(",");
      for(String s : disabled)
         disabledWorlds.add(s);
   }

   private void tryUpdate() {
      if(! new File(pvptag.getDataFolder(), "config.yml").exists()) {
         pvptag.saveDefaultConfig();
         pvptag.saveConfig();
      }

   }

   public void reload() {
      pvptag.reloadConfig();
      pvptag.manageConfig();
   }

   public ChatColor parseNameTagColor() {
      return ChatColor.getByChar(getConfig().getString("Tagging.NameTag Color"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e) {
      return ! disabledWorlds.contains(e.getEntity().getWorld().getName());
   }

   public boolean isBannedCommand(String command) {
      for(String s : bannedCommands) {
         if(command.startsWith("/" + s)) return true;
      }
      return false;
   }
}
