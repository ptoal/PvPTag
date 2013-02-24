package com.github.cman85.PvPTag;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;

import java.io.*;
import java.util.logging.*;

public class Config {

   private static Config instance = new Config();
   public File configFile;

   private FileConfiguration config;
   private PvPTag pvptag;

   private Config(){
   }

   public static Config getInstance(){
      return instance;
   }

   public FileConfiguration getConfig(){
      return config;
   }

   public void enable(PvPTag tag){
      this.pvptag = tag;
      configFile = new File(pvptag.getDataFolder(), "config.yml");
      try{
         firstRun();
      }catch (Exception e){
         e.printStackTrace();
      }
      config = new YamlConfiguration();
      loadYamls();
      try{
         tryUpdate();
      }catch (Exception e){
         e.printStackTrace();
      }
   }

   private void tryUpdate() throws IOException, InvalidConfigurationException{
      if(! config.getString("version").equalsIgnoreCase(pvptag.version)){
         PvPTag.log(Level.CONFIG, "Updating config!");
         File temp = new File(pvptag.getDataFolder(), "config.yml");
         if(! temp.exists()) temp.createNewFile();
         copy(pvptag.getResource("config.yml"), temp);
         FileConfiguration newConfig = new YamlConfiguration();
         newConfig.load(temp);
         for(String s : config.getKeys(false)){
            if(! s.equalsIgnoreCase("version"))
               newConfig.set(s, config.get(s));
            else
               newConfig.set(s, newConfig.get(s));
         }
         saveYamls();
      }else{
         PvPTag.log(Level.INFO, "Config file up to date.");
      }
   }

   public void disable(){
      saveYamls();
   }

   private void firstRun() throws Exception{
      if(configFile.exists()){
         pvptag.getLogger().log(Level.INFO, "Config file found!");
      }else{
         pvptag.getLogger().log(Level.INFO, "Config file NOT found, creating now!");
         configFile.getParentFile().mkdirs();
         copy(pvptag.getResource("config.yml"), configFile);
      }
   }

   private void copy(InputStream in, File file){
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

   public void loadYamls(){
      try{
         config.load(configFile);
      }catch (Exception e){
         e.printStackTrace();
      }
   }

   public void saveYamls(){
      try{
         config.save(configFile);
      }catch (IOException e){
         e.printStackTrace();
      }
   }

   public void reload(){
      loadYamls();
      pvptag.manageConfig();
   }

   public ChatColor parseNameTagColor(){
      return ChatColor.getByChar(config.getString("NameTag Color"));
   }
}
