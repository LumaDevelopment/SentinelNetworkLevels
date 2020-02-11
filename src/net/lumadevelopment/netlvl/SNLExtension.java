package net.lumadevelopment.netlvl;

import java.sql.SQLException;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class SNLExtension extends PlaceholderExpansion {
	
    private SNLCore plugin;
	
	public SNLExtension(SNLCore plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean persist() {
		return true;
	}
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public String getAuthor() {
		return "Luma Development";
	}
	
	@Override
	public String getIdentifier() {
		return "snl";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		
		if(identifier.equals("level_plain")) {
			if(player == null) {
				return "";
			}
			
			String level = "";
			
			if(!SNLCore.playerLevelMapContainsUser(player.getUniqueId())) {
				return "Waiting...";
			}
			
			if(SNLCore.playerLevelMapGetLevel(player.getUniqueId()) == -1) {
				try {
					level = LevelFunctions.getLevel(player.getUniqueId()).toString();
				} catch (SQLException e) {
					level = "0";
					
					SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting player level. StackTrace:");
					e.printStackTrace();
				}
				
			}else {
				level = SNLCore.playerLevelMapGetLevel(player.getUniqueId()).toString();
			}
			
			return level;
		}
		
		if(identifier.equals("level")) {
			if(player == null) {
				return "";
			}
			
			Integer level_n = 0;
			String level_f = "";
			String level;
			
			if(!SNLCore.playerLevelMapContainsUser(player.getUniqueId())) {
				return "Waiting...";
			}
			
			if(SNLCore.playerLevelMapGetLevel(player.getUniqueId()) == -1) {
				try {
					level_n = LevelFunctions.getLevel(player.getUniqueId());
					level_f = level_n.toString();
				} catch (SQLException e) {
					level_n = 0;
					level_f = level_n.toString();
					
					SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting player level. StackTrace:");
					e.printStackTrace();
				}
				
			}else {
				level_n = SNLCore.playerLevelMapGetLevel(player.getUniqueId());
				level_f = level_n.toString();
			}
			
			
			if(level_n > 199) {
				level = "" + ChatColor.GOLD + ChatColor.BOLD + level_f;
				return level;
			}else if(level_n > 99) {
				level = ChatColor.GOLD + level_f;
				return level;
			}else {
				level = ChatColor.GRAY + level_f;
				return level;
			}
			
		}
		
		if(identifier.equals("server")) {
			return StringUtils.capitalize(plugin.getConfig().getString("Server"));
		}
		
		return null;
	}
	
}
