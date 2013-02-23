package com.github.cman85.PvPTag;

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
}
