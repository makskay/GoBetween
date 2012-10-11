package me.makskay.bukkit.gobetween;

import java.util.List;

public class WorldGroup {
	private String name;
	private List<String> worlds;
	
	public WorldGroup (String name, List<String> worlds) {
		this.name = name;
		this.worlds = worlds;
	}
	
	public boolean contains(String worldname) {
		return worlds.contains(worldname);
	}
	
	public String getName() {
		return name;
	}
}
