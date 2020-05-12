package net.lumadevelopment.netlvl;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SNLCommand implements CommandExecutor{

	//isInteger function courtesy of corsiKa on StackOverflow
	public static boolean isInteger(String s) {
		
		try {
			@SuppressWarnings("unused")
			Integer i = Integer.valueOf(s);
		} catch (Exception e) {
			return false;
		}
		
	    return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("nl")) {
			if(args.length == 0) {
				sender.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + SNLCore.instance.getDescription().getName() + " v" + SNLCore.instance.getDescription().getVersion() + " by " + SNLCore.instance.getDescription().getAuthors().get(0) + "\n"
						+ ChatColor.RESET
						+ "/nl xp - Check the amount of XP you have.\n"
						+ "/nl xpneeded <level> - Shows how much total XP is needed for a level.\n");
				
				if(sender.hasPermission("network.levels.admin")) {
					sender.sendMessage("/nl set{level} <player> <xp> - Sets the amount of XP (or level) for a player.\n"
							+ "/nl add <player> <xp> - Adds certain amount of XP to player.\n"
							+ "/nl remove <player> <xp> - Removes certain amount of XP from player.\n"
							+ "/nl wipe <player> - Completely wipes a player's XP.\n"
							+ "/nl createreward <level> <server command executes on, put global if it can be executed anywhere> <command> --name <name of reward> - Create reward.\n"
							+ "/nl updatereward <level> <server> <command> --name <name of reward> - Updates reward\n"
							+ "/nl getreward <level> - Get data on reward.\n"
							+ "/nl reload - Reload configuration.");
				}
				
				return true;
			}else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("xp")) {
					if(!(sender instanceof Player)) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You must be a player to use this command!");
						return true;
					}
					
					try {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "Your XP is " + ChatColor.AQUA + LevelFunctions.getXP(((Player) sender).getUniqueId()) + ChatColor.GREEN + ", and you are " + ChatColor.AQUA + "Network Level " + LevelFunctions.getLevel(((Player) sender).getUniqueId()));
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels can not get XP! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}else if(args[0].equalsIgnoreCase("reload")) {
					if(!sender.hasPermission("knightsips.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					SNLCore.instance.reloadConfig();
					sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "SentinelNetworkLevels reloaded!");
					return true;
				}
				
				sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Invalid usage of the command! Use \"/nl\" to see command usages!");
				return true;
			}else if(args.length == 2) {
			
				if(args[0].equalsIgnoreCase("xpneeded")) {
					
					if(!isInteger(args[1])) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[1] + " is not a number!");
						return true;
					}
					
					Integer level = Integer.valueOf(args[1]);
					
					if(level < 1 || level > 290) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Level " + level + " does not fit within the support range! (Levels 1-290)");
						return true;
					}
					
					if(sender instanceof Player) {
						try {
							if(level <= LevelFunctions.getLevel(((Player) sender).getUniqueId())) {
								sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "A player needs " + ChatColor.AQUA + LevelFunctions.getXPForLevel(level) + ChatColor.GREEN + " XP to get to" + ChatColor.AQUA + " Network Level " +  level);
								return true;
							}else {
								Integer xp_diff = LevelFunctions.getXPForLevel(level) - LevelFunctions.getXP(((Player) sender).getUniqueId());
								sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "A player needs " + ChatColor.AQUA + LevelFunctions.getXPForLevel(level) + ChatColor.GREEN + " XP to get to" + ChatColor.AQUA + " Network Level " +  level 
										+ ChatColor.RESET + "\n" + ChatColor.GREEN + "You are " + ChatColor.AQUA + xp_diff + ChatColor.GREEN + " XP away from getting to this level.");
								return true;
							}
						} catch (SQLException e) {
							SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting player level! StackTrace:");
							e.printStackTrace();
						}
					}else {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "A player needs " + ChatColor.AQUA + LevelFunctions.getXPForLevel(level) + ChatColor.GREEN + " XP to get to" + ChatColor.AQUA + " Network Level " +  level);
						return true;
					}
					
				}else if(args[0].equalsIgnoreCase("wipe")) {
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					@SuppressWarnings("deprecation")
					UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
					
					if(uuid == null) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Player " + args[1] + " could not be found!");
						return true;
					}
					
					try {
						LevelFunctions.setXP(uuid, 0);
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having problems setting XP! StackTrace:");
						e.printStackTrace();
					}
					
					sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "All XP for " + ChatColor.AQUA + args[1] + ChatColor.RED + " wiped.");
					
					return true;
				}else if(args[0].equalsIgnoreCase("getreward")) {
					if(!isInteger(args[1])) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[1] + " is not a number!");
						return true;
					}
					
					Integer level = Integer.valueOf(args[1]);
					
					Reward reward;
					try {
						reward = RewardsFunctions.getRewardObject(level);
						
						if(reward == null) {
							sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Reward does not exist!");
							return true;
						}
						
						String server = reward.getServer();
						String command = reward.getCommand();
						String name = reward.getName();
						
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "Requested Reward:\n"
								+ "Level: " + ChatColor.RESET + level + "\n"
								+ ChatColor.GREEN + "Server: " + ChatColor.RESET + server + "\n"
								+ ChatColor.GREEN + "Command: " + ChatColor.RESET + command + "\n"
								+ ChatColor.GREEN + "Name: " + ChatColor.RESET + name + "\n");
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting RewardObject! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}
				
				sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Invalid usage of the command! Use \"/nl\" to see command usages!");
				return true;
				
			}else if(args.length == 3) {
				if(args[0].equalsIgnoreCase("set")) {
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					@SuppressWarnings("deprecation")
					UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
					
					if(uuid == null) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Player " + args[1] + " could not be found!");
						return true;
					}
					
					if(isInteger(args[2]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Long xp_check = Long.valueOf(args[2]);
					
					if(xp_check < 0 || xp_check > 2147483647) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is either too large or too small!");
						return true;
					}
					
					Integer previous_level;
					try {
						previous_level = LevelFunctions.getLevel(uuid);
						
						LevelFunctions.setXP(uuid, Integer.valueOf(args[2]));
						
	                    Integer current_level = LevelFunctions.getLevelFromXP(Integer.valueOf(args[2]));
						
						if(current_level > previous_level) {
							RewardsFunctions.processRewards(uuid, previous_level, current_level);
							if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(uuid))) {
								LevelFunctions.sendLevelUpMessage(Bukkit.getPlayer(uuid), previous_level, current_level);
							}
						}
						
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + args[1] + "'s XP has been set to " + ChatColor.AQUA + Integer.valueOf(args[2]));
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting levels or setting XP! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}else if(args[0].equalsIgnoreCase("add")) {
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					@SuppressWarnings("deprecation")
					UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
					
					if(uuid == null) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Player " + args[1] + " could not be found!");
						return true;
					}
					
					if(isInteger(args[2]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Integer xp_check = Integer.valueOf(args[2]);
					
					if(xp_check < 0 || xp_check > 2147483647) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is either too large or too small!");
						return true;
					}
					
					Integer current_xp;
					try {
						current_xp = LevelFunctions.getXP(uuid) + xp_check;
						
						if(current_xp < 0 || current_xp > 2147483647) {
							sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "The resulting XP is either too large or too small!");
							return true;
						}
						
						Integer previous_level = LevelFunctions.getLevelFromXP(LevelFunctions.getXP(uuid));
						
						LevelFunctions.setXP(uuid, current_xp);
						
						Integer current_level = LevelFunctions.getLevelFromXP(current_xp);
						
						if(current_level > previous_level) {
							RewardsFunctions.processRewards(uuid, previous_level, current_level);
							if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(uuid))) {
								LevelFunctions.sendLevelUpMessage(Bukkit.getPlayer(uuid), previous_level, current_level);
							}
						}
						
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + args[1] + "'s XP has been set to " + ChatColor.AQUA + current_xp);
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting XP or setting XP! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}else if(args[0].equalsIgnoreCase("remove")) {
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					@SuppressWarnings("deprecation")
					UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
					
					if(uuid == null) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Player " + args[1] + " could not be found!");
						return true;
					}
					
					if(isInteger(args[2]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Integer xp_check = Integer.valueOf(args[2]);
					
					if(xp_check < 0 || xp_check > 2147483647) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is either too large or too small!");
						return true;
					}
					
					Integer current_xp;
					try {
						current_xp = LevelFunctions.getXP(uuid) - xp_check;
						
						if(current_xp < 0 || current_xp > 2147483647) {
							sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "The resulting XP is either too large or too small!");
							return true;
						}
						
						LevelFunctions.setXP(uuid, current_xp);
						
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + args[1] + "'s XP has been set to " + ChatColor.AQUA + current_xp);
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting XP or setting XP! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}else if(args[0].equalsIgnoreCase("setlevel")) {
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					@SuppressWarnings("deprecation")
					UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
					
					if(uuid == null) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Player " + args[1] + " could not be found!");
						return true;
					}
					
					if(isInteger(args[2]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Integer level_check = Integer.valueOf(args[2]);
					
					if(level_check < 0 || level_check > 290) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is either too large or too small!");
						return true;
					}
					
					Integer previous_level;
					try {
						previous_level = LevelFunctions.getLevel(uuid);
						
						LevelFunctions.setXP(uuid, LevelFunctions.getXPForLevel(level_check));
						
	                    Integer current_level = LevelFunctions.getLevelFromXP(LevelFunctions.getXPForLevel(level_check));
	                    
						if(current_level > previous_level) {
							RewardsFunctions.processRewards(uuid, previous_level, current_level);
							if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(uuid))) {
								LevelFunctions.sendLevelUpMessage(Bukkit.getPlayer(uuid), previous_level, current_level);
							}
						}
						
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + args[1] + "'s" + ChatColor.AQUA + " Network Level " + ChatColor.GREEN + "has been set to" + ChatColor.AQUA + " Network Level "  + level_check);
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble getting XP or setting XP! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
				}
				
				sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Invalid usage of the command! Use \"/ksnl\" to see command usages!");
				return true;
			}else {
				if(args[0].equalsIgnoreCase("createreward")) {
					
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					if(isInteger(args[1]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Integer level = Integer.valueOf(args[1]);
					String server = args[2];
					
					int args_counter = 3;
					String command = "";
					String name = "";
					while(true) {
						
						if(args[args_counter].equalsIgnoreCase("--name")) {
							args_counter++;
							break;
						}
						
						command = command + args[args_counter] + " ";
						
						args_counter++;
					}
					
					while(true) {
						if(args_counter == (args.length - 1)) {
							name = name + args[args_counter];
							break;
						}
						
						name = name + args[args_counter] + " ";
						
						args_counter++;
					}
					
					try {
						RewardsFunctions.createReward(level, server, command, name);
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "Created reward:\n"
								+ "Level: " + ChatColor.RESET + level + "\n"
								+ ChatColor.GREEN + "Server: " + ChatColor.RESET + server + "\n"
								+ ChatColor.GREEN + "Command: " + ChatColor.RESET + command + "\n"
								+ ChatColor.GREEN + "Name: " + ChatColor.RESET + name + "\n");
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble creating rewards! StackTrace:");
						e.printStackTrace();
					}
					
					
					return true;
					
				}else if(args[0].equalsIgnoreCase("updatereward")) {
					
					if(!sender.hasPermission("network.levels.admin")) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "You do not have permission to use this command!");
						return true;
					}
					
					if(isInteger(args[1]) == false) {
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + args[2] + " is not a number!");
						return true;
					}
					
					Integer level = Integer.valueOf(args[1]);
					String server = args[2];
					
					int args_counter = 3;
					String command = "";
					String name = "";
					while(true) {
						
						if(args[args_counter].equalsIgnoreCase("--name")) {
							args_counter++;
							break;
						}
						
						command = command + args[args_counter] + " ";
						
						args_counter++;
					}
					
					while(true) {
						if(args_counter == (args.length - 1)) {
							name = name + args[args_counter];
							break;
						}
						
						name = name + args[args_counter] + " ";
						
						args_counter++;
					}
					
					try {
						RewardsFunctions.updateReward(level, server, command, name);
						sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.GREEN + "Updated reward:\n"
								+ "Level: " + ChatColor.RESET + level + "\n"
								+ ChatColor.GREEN + "Server: " + ChatColor.RESET + server + "\n"
								+ ChatColor.GREEN + "Command: " + ChatColor.RESET + command + "\n"
								+ ChatColor.GREEN + "Name: " + ChatColor.RESET + name + "\n");
					} catch (SQLException e) {
						SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels is having trouble updating rewards! StackTrace:");
						e.printStackTrace();
					}
					
					return true;
					
				}else {
					sender.sendMessage(SNLCore.getPrefix(sender) + ChatColor.RED + "Invalid usage of the command! Use \"/ksnl\" to see command usages!");
					return true;
				}
			}
		}
		
		return false;
	}

}
