package me.makskay.bukkit.wanderlust;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
	private WanderlustPlugin plugin;
	
	public PlayerListener(WanderlustPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location from = event.getFrom();
		
		if (from.getWorld().equals(event.getTo().getWorld())) {
			return; // if the teleport happened within one world, ignore it
		}
		
		plugin.savePlayerAndLocation(player, from);
	}
}
