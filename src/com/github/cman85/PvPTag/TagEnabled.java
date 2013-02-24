package com.github.cman85.PvPTag;

import org.bukkit.entity.*;
import org.bukkit.event.*;

public class TagEnabled implements TagAPEye, Listener {
   private PvPTag pvptag;

   @Override
   public void refresh(Player p){
      org.kitteh.tag.TagAPI.refreshPlayer(p);
   }

   public TagEnabled(PvPTag pt){
      this.pvptag = pt;
   }

   @EventHandler
   public void onNameTag(org.kitteh.tag.PlayerReceiveNameTagEvent e){
      if(! pvptag.isSafe(e.getNamedPlayer().getName())){
         Player p = e.getNamedPlayer();
         e.setTag(pvptag.nameTagColor + p.getName());
      }else{
         e.setTag(e.getNamedPlayer().getName());
      }
   }
}
