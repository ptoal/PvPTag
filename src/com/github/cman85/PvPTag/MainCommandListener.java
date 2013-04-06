package com.github.cman85.PvPTag;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class MainCommandListener {
   private final PvPTag pvPTag;

   public MainCommandListener(PvPTag pvPTag) {
      this.pvPTag = pvPTag;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if(cmd.getName().equalsIgnoreCase("callsafe") || cmd.getName().equalsIgnoreCase("csafe")) {
         if(sender.isOp() || sender.hasPermission("pvptag.callsafe") || sender instanceof ConsoleCommandSender) {
            if(args.length == 1) {
               if(! args[0].equalsIgnoreCase("all")) {
                  Player p = pvPTag.getServer().getPlayer(args[0]);
                  if(p == null) {
                     sender.sendMessage("§cYou must specify an online player.");
                     return true;
                  } else {
                     if(! pvPTag.isSafe(p.getName())) {
                        pvPTag.callSafe(p);
                        sender.sendMessage("§c" + p.getName() + " is no longer hittable.");
                        pvPTag.refresh(p);
                        return true;
                     } else {
                        sender.sendMessage("§c" + p.getName() + " was not hittable.");
                        return true;
                     }
                  }
               } else {
                  pvPTag.callSafeAllManual();
               }
            } else {
               sender.sendMessage("§cUsage: /callsafe [name] or /callsafe all");
            }
         } else {
            sender.sendMessage("§cYou must be an operator to use this command.");
         }
      } else if(cmd.getName().equalsIgnoreCase("callhit") || cmd.getName().equalsIgnoreCase("chit")) {
         if(sender.isOp() || sender.hasPermission("pvptag.callhit") || sender instanceof ConsoleCommandSender) {
            Player p;
            if(args.length != 1)
               return false;
            else {
               p = pvPTag.getServer().getPlayer(args[0]);
               if(p == null) {
                  sender.sendMessage("§cYou must specify an online player.");
                  return true;
               }
            }

            if(pvPTag.isSafe(p.getName())) {
               p.damage(1);
               pvPTag.addUnsafe(p);
            }
         }
      } else if(cmd.getName().equalsIgnoreCase("pvptag")) {
         if(sender.isOp() || sender.hasPermission("pvptag.pvptag") || sender instanceof ConsoleCommandSender) {
            if(args.length > 0) {
               if(args[0].equalsIgnoreCase("reload")) {
                  pvPTag.configuration.reload();
                  sender.sendMessage("§cSettings reloaded!");
               } else if(args[0].equalsIgnoreCase("setcolor")) {
                  if(args.length == 2) {
                     pvPTag.setNameTagColor(ChatColor.getByChar(args[1]));
                     pvPTag.configuration.getConfig().set("Tagging.NameTag Color", pvPTag.getNameTagColor().getChar());
                     sender.sendMessage("§cColor changed to: " + pvPTag.getNameTagColor() + "this.");
                     pvPTag.saveConfig();
                  }
               } else if(args[0].equalsIgnoreCase("save")) {
                  pvPTag.saveConfig();
                  sender.sendMessage("§cConfig saved!");
               }
            }
         }

      }
      return true;
   }
}