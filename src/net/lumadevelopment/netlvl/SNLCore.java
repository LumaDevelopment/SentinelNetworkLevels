package net.lumadevelopment.netlvl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.ChatColor;

public class SNLCore extends JavaPlugin implements Listener{
	
	//Permissions
	//network.levels.admin - The permission to check and modify xp
	//network.levels.xp_bar - The permission to have your xp bar set to your network levels
	//network.levels.get_play_xp - The permission to get xp amount defined as xp_per_played_interval every played_interval minutes
	
	public static SNLCore instance;
	public static HashMap<UUID, Integer> player_time_counters = new HashMap<UUID, Integer>();
	public static HashMap<UUID, Integer> player_level = new HashMap<UUID, Integer>();
	//private static List<UUID> re_init_ed = new ArrayList<UUID>();
	
	public static String prefix;
	
	//Time Statistics
	public static Integer xp_per_played_interval;
	public static Integer played_interval;
	
	//MySQL Information
	public static String ip;
	public static String port;
	public static String database;
	public static String user;
	public static String password;
	
	//Level XP Required
	
	@Override
	public void onEnable(){
		instance = this;
		
		if(getConfig().getString("Server") == null) {
			getConfig().set("Server", "CONFIGURE");
			saveConfig();
		}
		
		if(getConfig().getString("SQL.IP") == null) {
			getConfig().set("SQL.IP", "localhost");
			saveConfig();
		}
		
		if(getConfig().getString("SQL.Port") == null) {
			getConfig().set("SQL.Port", "3306");
			saveConfig();
		}
		
		if(getConfig().getString("SQL.DatabaseName") == null) {
			getConfig().set("SQL.DatabaseName", "database");
			saveConfig();
		}
		
		if(getConfig().getString("SQL.Username") == null) {
			getConfig().set("SQL.Username", "root");
			saveConfig();
		}
		
		if(getConfig().getString("SQL.Password") == null) {
			getConfig().set("SQL.Password", "password");
			saveConfig();
		}
		
		if(getConfig().getInt("XPForPlaying.MinutesToGetXP") == 0) {
			getConfig().set("XPForPlaying.MinutesToGetXP", 30);
			saveConfig();
		}
		
		if(getConfig().getInt("XPForPlaying.XPPerMinutes") == 0) {
			getConfig().set("XPForPlaying.XPPerMinutes", 50);
			saveConfig();
		}
		
		if(getConfig().getString("Prefix") == null) {
			getConfig().set("Prefix", "&a&lNetwork&r&e&lLevels&r &7➤&r ");
			saveConfig();
		}
			
		reloadConfig();
		ip = getConfig().getString("SQL.IP");
		port = getConfig().getString("SQL.Port");
		database = getConfig().getString("SQL.DatabaseName");
		user = getConfig().getString("SQL.Username");
		password = getConfig().getString("SQL.Password");
		xp_per_played_interval = getConfig().getInt("XPForPlaying.XPPerMinutes");
		played_interval = getConfig().getInt("XPForPlaying.MinutesToGetXP");
		prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix"));
		
		try {
			LevelFunctions.createSnlTable();
			RewardsFunctions.createSnlCommandsTableTwo();
			RewardsFunctions.createSnlRewardsTable();
		} catch (SQLException e) {
			getLogger().log(Level.SEVERE, "SentinelNetworkLevels could not establish SQL connections! StackTrace:");
			e.printStackTrace();
		}
		
		BukkitScheduler scheduler = getServer().getScheduler();
		
		/*if(getConfig().getInt("LevelRefreshInSeconds") == 0) {
			getConfig().set("LevelRefreshInSeconds", 3);
			saveConfig();
		}*/
		
		//Long refresh = (long) (getConfig().getInt("LevelRefreshInSeconds") * 20);
		
		//reloadConfig();
		/*
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				HashMap<UUID, Integer> lvl = new HashMap<UUID, Integer>();
				for(Player p : Bukkit.getOnlinePlayers()) {
					try {
						Integer level = LevelFunctions.getLevel(p.getUniqueId());
						lvl.put(p.getUniqueId(), level);
					} catch (SQLException e) {
						getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting a player level! StackTrace:");
						e.printStackTrace();
						
						lvl.put(p.getUniqueId(), 0);
					}
				}
				
				player_level = lvl;
			}
		}, 0L, refresh);
		*/	
		
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new SNLExtension(instance).register();
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("nl").setExecutor(new SNLCommand());
		
		if(!Bukkit.getBukkitVersion().contains("1.8")) {
			if(!getConfig().contains("UseXPBar")) {
				getConfig().set("UseXPBar", "false");
				saveConfig();
				reloadConfig();
				return;
			}
			
			if((!getConfig().getString("UseXPBar").equalsIgnoreCase("true")) 
					&& 
					(!getConfig().getString("UseXPBar").equalsIgnoreCase("false"))) {
				getConfig().set("UseXPBar", "true");
				saveConfig();
			}
		}
		
		reloadConfig();
		
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				
				try {
					for(SNLCommandObj obj : RewardsFunctions.getRewardCommands()) {
						
						if(obj.getServer().equalsIgnoreCase(getConfig().getString("Server")) || obj.getServer().equalsIgnoreCase("global")) {
							//make this thing below not needed
							
							if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(UUID.fromString(obj.getUUID())))) {
								
								getServer().dispatchCommand(Bukkit.getConsoleSender(), obj.getCommand());
								obj.commandExecuted();
							}
						}
					}
				} catch (CommandException | SQLException e) {
					getLogger().log(Level.SEVERE, "SentinelNetworkLevels could not execute command! StackTrace:");
					e.printStackTrace();
				}
			}
		}, 0L, 100L);
		
		if(getConfig().getString("UseXPBar").equalsIgnoreCase("true")) {
			//SPEREATE REWARD REDEMPTION FROM XP BAR
			
			if(getConfig().getInt("XPBarRefreshInSeconds") == 0) {
				getConfig().set("XPBarRefreshInSeconds", 60);
				saveConfig();
			}
			
			Long refresh_xp = (long) (getConfig().getInt("XPBarRefreshInSeconds") * 20);
			
			scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					
					for(Player p : Bukkit.getOnlinePlayers()) {
						
						if(p.hasPermission("network.levels.xp_bar")) {
							p.setLevel(0);
							p.setExp(0);
							
							try {
								p.giveExpLevels(LevelFunctions.getLevel(p.getUniqueId()));
							} catch (SQLException e) {
								getLogger().log(Level.SEVERE, "SentinelNetworkLevels can not obtain player level! StackTrace:");
								e.printStackTrace();
							}
						}
					}
				}
			}, 0L, refresh_xp);
		}
	}
	
	public static boolean playerLevelMapContainsUser(UUID u) {
		if(player_level.keySet().contains(u)) {
			return true;
		}else {
			return false;
		}
	}
	
	public static Integer playerLevelMapGetLevel(UUID u) {
		if (!playerLevelMapContainsUser(u)) {
			return -1;
		}else {
			return player_level.get(u);
		}
	}
	
	@EventHandler
	public void xpChange(PlayerExpChangeEvent e) {
		if(getConfig().getString("UseXPBar").equalsIgnoreCase("true")) {
			Player p = e.getPlayer();
			
			if(p.hasPermission("network.levels.xp_bar")) {
				p.setLevel(0);
				p.setExp(0);
				
				try {
					p.giveExpLevels(LevelFunctions.getLevel(p.getUniqueId()));
				} catch (SQLException e1) {
					getLogger().log(Level.SEVERE, "SentinelNetworkLevels can not obtain player level! StackTrace:");
					e1.printStackTrace();
				}
			}
		}
		
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e) {
		
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {	
			
            @Override
            public void run() {
            	
            	Player p = e.getPlayer();
    			
    			try {
    				if(!LevelFunctions.userExists(e.getPlayer().getUniqueId())) {
    					LevelFunctions.addUser(e.getPlayer().getUniqueId(), 1);
    				}
    			} catch (SQLException e1) {
    				getLogger().log(Level.SEVERE, "Can not check if user exists or add user! StackTrace:");
    				e1.printStackTrace();
    			}
    			
    			if(!player_level.keySet().contains(e.getPlayer().getUniqueId())) {
    				try {
    					player_level.put(e.getPlayer().getUniqueId(), LevelFunctions.getLevelFromXP(LevelFunctions.getXP(e.getPlayer().getUniqueId())));
    				} catch (SQLException e1) {
    					System.out.println("Issue adding player to level map! Scoreboard issues are bound to follow.");
    					e1.printStackTrace();
    				}
    			}
            	
            	if(getConfig().getString("UseXPBar").equalsIgnoreCase("true")) {
        			if(p.hasPermission("network.levels.xp_bar")) {
        				p.setLevel(0);
        				p.setExp(0);
        				
        				try {
        					p.giveExpLevels(LevelFunctions.getLevel(p.getUniqueId()));
        				} catch (SQLException e1) {
        					getLogger().log(Level.SEVERE, "SentinelNetworkLevels can not obtain player level! StackTrace:");
        					e1.printStackTrace();
        				}
        			}
        		}
        		
        		//30 minutes -> 1800 seconds -> 36000 ticks
        		long played_interval_f = played_interval * 60 * 20;
        		
        		if(!e.getPlayer().hasPermission("network.levels.get_play_xp")) {
        			return;
        		}
        		
        		BukkitScheduler scheduler = getServer().getScheduler();
        		Integer i = scheduler.scheduleSyncRepeatingTask(instance, new Runnable() {
        			@Override
        			public void run() {
        				Integer pre_level;
        				try {
        					pre_level = LevelFunctions.getLevelFromXP(LevelFunctions.getXP(e.getPlayer().getUniqueId()));
        					Integer xp_to_set = LevelFunctions.getXP(e.getPlayer().getUniqueId()) + xp_per_played_interval;
        					LevelFunctions.setXP(e.getPlayer().getUniqueId(), xp_to_set);
        					Integer current_level = LevelFunctions.getLevelFromXP(xp_to_set);
        					
        					RewardsFunctions.processRewards(e.getPlayer().getUniqueId(), pre_level, current_level);
        					if(pre_level < current_level) {
        						LevelFunctions.sendLevelUpMessage(e.getPlayer(), pre_level, current_level);
        					}
        				} catch (SQLException e1) {
        					getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having XP errors!");
        					e1.printStackTrace();
        				}
        				
        			}
        		}, played_interval_f, played_interval_f);
        		
        		player_time_counters.put(e.getPlayer().getUniqueId(), i);
            }
        });
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if(player_level.containsKey(e.getPlayer().getUniqueId())) {
			player_level.remove(e.getPlayer().getUniqueId());
		}
		
		if(player_time_counters.containsKey(e.getPlayer().getUniqueId())) {
			getServer().getScheduler().cancelTask(player_time_counters.get(e.getPlayer().getUniqueId()));
			return;
		}
	}
	
}
