package me.makskay.bukkit.wanderlust;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WanderlustPlugin extends JavaPlugin {
	private HashMap<String, Location> cachedLocations = new HashMap<String, Location>();
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
	}
	
	public void onDisable() { // save the cached locations to disk
		for (String key : cachedLocations.keySet()) {
			int division = key.indexOf('.');
			String path = "players." + key.substring(0, division) + "." + key.substring(division + 1, key.length()) + ".";
			
			getConfig().set(path + "x", cachedLocations.get(key).getX());
			getConfig().set(path + "y", cachedLocations.get(key).getY());
			getConfig().set(path + "z", cachedLocations.get(key).getZ());
		}
		
		saveConfig();
	}
	
	public void savePlayerAndLocation(Player player, Location location) {
		cachedLocations.put(player.getName() + "." + location.getWorld().getName(), location);
	}
	
	public boolean onCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("go")) {
			Player player = (Player) sender;
			
			if (player == null) { // can't teleport a console
				sender.sendMessage("Only a player may use that command!");
				return true;
			}
			
			if (args.length != 1) { // exactly one argument - any more or less and they're doing it wrong
				return false;
			}
			
			if (args[0].equals(player.getWorld().getName())) { // can't return to the world you're already in
				player.sendMessage(ChatColor.RED + "You're already in world \"" + args[0] + "\"");
				return true;
			}
			
			Location location = cachedLocations.get(player.getName() + "." + args[0]); // try to get a cached location
			
			if (location != null) { // sometimes there's a cached location matching the query, and life is good
				player.sendMessage(ChatColor.GRAY + "Returned to last known location in " + args[0]);
				player.teleport(location);
				return true;
			}
			
			// if there's no cached location, try to get a location for the specified world from config.yml
			
			String path = "players." + player.getName() + "." + args[0];
			
			if (getConfig().getString(path + ".x") == null) { // if there's no saved location, bail out
				player.sendMessage(ChatColor.RED + "No location found for world \"" + args[0] + "\"");
				getLogger().info("[DEBUG] No location found from file");
				return true;
			}
				
			double x = getConfig().getDouble(path + ".x");
			double y = getConfig().getDouble(path + ".y");
			double z = getConfig().getDouble(path + ".z");
			World world = Bukkit.getWorld(args[0]);
				
			location = new Location(world, x, y, z);
				
			player.sendMessage(ChatColor.GRAY + "Returned to last known location in " + args[0]);
			player.teleport(location);
			return true;
		}
		
		return false;
	}
}
