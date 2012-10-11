package me.makskay.bukkit.gobetween;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
	private GoBetweenPlugin plugin;
	
	public PlayerListener(GoBetweenPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Location from = event.getFrom();
		World fromWorld = from.getWorld();
		
		if (fromWorld.equals(event.getTo().getWorld())) {
			return; // if the teleport happened within one world, ignore it
		}
		
		Player player = event.getPlayer();
		WorldGroup group = null;
		
		if (plugin.worldgroupsEnabled) {
			for (WorldGroup worldgroup : plugin.getWorldGroups()) {
				if (worldgroup.contains(fromWorld.getName())) {
					group = worldgroup;
				}
			}
		}
		
		String groupName;
		
		if (group == null) { // if worldgroups aren't enabled, or if the fromWorld is ungrouped
			groupName = fromWorld.getName();
		}
		
		else {
			groupName = group.getName();
		}
		
		plugin.savePlayerAndLocation(player, from, groupName);
	}
}
