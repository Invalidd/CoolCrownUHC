package com.coolcrown.UHC;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Rate implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendRawMessage(ChatColor.GREEN+"" +ChatColor.BOLD+ "[CoolCrown] " + ChatColor.GOLD + "The heal rate is " + ChatColor.BLUE + CoolCrownUHC.HEAL_RATE);
            return true;
        }
        try {
            CoolCrownUHC.HEAL_RATE = Double.parseDouble(args[0]);
            p.sendRawMessage(ChatColor.GREEN+"" +ChatColor.BOLD+ "[CoolCrown] " + ChatColor.GOLD + "Healing rate now set to " + args[0]);
        }
        catch (Exception e){
            p.sendRawMessage(ChatColor.GREEN+"" +ChatColor.BOLD+ "[CoolCrown] " + ChatColor.RED + "Something went wrong setting the heal rate. Use /healrate <rate>");
        }
        return true;
    }
}
