package me.makskay.bukkit.gobetween;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GoBetweenPlugin extends JavaPlugin {
	private HashMap<String, Location> cachedLocations = new HashMap<String, Location>();
	private ConfigAccessor configManager;
	private ConfigAccessor locationsManager;
	HashSet<WorldGroup> worldGroups = new HashSet<WorldGroup>();
	boolean worldgroupsEnabled;
	
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		
		configManager = new ConfigAccessor(this, "config.yml");
		locationsManager = new ConfigAccessor(this, "locations.yml");
		
		configManager.reloadConfig();
		configManager.saveDefaultConfig();
		
		worldgroupsEnabled = configManager.getConfig().getBoolean("enable-world-groups");
		
		if (worldgroupsEnabled) { // build a list of worldgroups
			for (String key : configManager.getConfig().getConfigurationSection("world-groups").getKeys(false)) {
				WorldGroup group = new WorldGroup(key, configManager.getConfig().getStringList("world-groups." + key));
				worldGroups.add(group);
			}
		}
	}
	
	public void onDisable() { // save the cached locations to disk
		for (String key : cachedLocations.keySet()) {
			int division = key.indexOf('.');
			String path = "players." + key.substring(0, division) + "." + key.substring(division + 1, key.length()) + ".";
			
			locationsManager.getConfig().set(path + "x", cachedLocations.get(key).getX());
			locationsManager.getConfig().set(path + "y", cachedLocations.get(key).getY());
			locationsManager.getConfig().set(path + "z", cachedLocations.get(key).getZ());
			locationsManager.getConfig().set(path + "world", cachedLocations.get(key).getWorld().getName());
		}
		
		locationsManager.saveConfig();
	}
	
	public void savePlayerAndLocation(Player player, Location location, String groupName) {
		String playerName = player.getName();
		if (groupName == null) {
			groupName = location.getWorld().getName(); // groupName is used in the path
		}
		
		cachedLocations.put(playerName + "." + groupName, location);
		String path = "players." + playerName + "." + groupName + ".";
		
		locationsManager.getConfig().set(path + "x", location.getX());
		locationsManager.getConfig().set(path + "y", location.getY());
		locationsManager.getConfig().set(path + "z", location.getZ());
		locationsManager.getConfig().set(path + "world", location.getWorld().getName()); // world will always be the actual world
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
			
			// if there's no cached location, try to get a location for the specified world or worldgroup from config.yml
			
			String path = "players." + player.getName() + "." + args[0];
			
			if (locationsManager.getConfig().getString(path + ".world") == null) { // if there's no saved location, bail out
				player.sendMessage(ChatColor.RED + "No location found for world \"" + args[0] + "\"");
				return true;
			}
				
			double x = locationsManager.getConfig().getDouble(path + ".x");
			double y = locationsManager.getConfig().getDouble(path + ".y");
			double z = locationsManager.getConfig().getDouble(path + ".z");
			String worldName = locationsManager.getConfig().getString(path + ".world");
			World world = Bukkit.getWorld(worldName);
				
			location = new Location(world, x, y, z);
				
			player.sendMessage(ChatColor.GRAY + "Returned to last known location in " + args[0]);
			player.teleport(location);
			return true;
		}
		
		return false;
	}
	
	public String getGroupOf(String worldname) {
		for (WorldGroup group : worldGroups) {
			if (group.contains(worldname)) {
				return group.getName();
			}
		}
		
		return null;
	}
	
	public HashSet<WorldGroup> getWorldGroups() {
		return worldGroups;
	}
}
