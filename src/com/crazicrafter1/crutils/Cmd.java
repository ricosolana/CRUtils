package com.crazicrafter1.crutils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd implements CommandExecutor {

    public Cmd(Main plugin) {
        plugin.getCommand("setskin").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String s, String[] args) {

        //Util.setSkin((Player) sender, args[0]);

        return true;
    }
}
