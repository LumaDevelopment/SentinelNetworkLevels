package net.lumadevelopment.netlvl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LevelFunctions {

	private static String ip = SNLCore.ip;
	private static String port = SNLCore.port;
	private static String database = SNLCore.database;
	private static String user = SNLCore.user;
	private static String password = SNLCore.password;
	
	//private static Connection conn;
	
	public static Connection databaseConnect() {
		
		try {
			Connection db_connect = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC", user, password);
			return db_connect;
		}catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
		
	}
	
	public static boolean createSnlTable() throws SQLException {
		Connection db_connect = databaseConnect();
		
		if(db_connect == null) {
			return false;
		}
		
		try {
			PreparedStatement create = db_connect.prepareStatement("CREATE TABLE IF NOT EXISTS sentinelnetworklevels ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid VARCHAR(36) NOT NULL, "
					+ "xp INT NOT NULL, "
					+ "PRIMARY KEY ( id ));");
			create.executeUpdate();
			
			//rs.getString("uuid")
			db_connect.close();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			db_connect.close();
			return false;
		}
		
	}
	
	public static void sendLevelUpMessage(Player p, Integer oldlevel, Integer newlevel) {
		
		if(p.hasPermission("network.levels.xp_bar")) {
			p.setLevel(0);
			p.setExp(0);
			
			try {
				p.giveExpLevels(LevelFunctions.getLevel(p.getUniqueId()));
			} catch (SQLException e) {
				SNLCore.instance.getLogger().log(Level.SEVERE, "SentinelNetworkLevels can not obtain player level! StackTrace:");
				e.printStackTrace();
			}
		}
		
		p.sendMessage(ChatColor.GREEN + "---------------------------------------------------\n"
				+ " \n"
				+ "You have leveled up!\n"
				+ ChatColor.AQUA + "Network Level " + oldlevel + ChatColor.GREEN + " -> " + ChatColor.AQUA + "Network Level " + newlevel
				+ ChatColor.RESET + "\n \n"
				+ ChatColor.GREEN + "You've earned these rewards:");
		
		for(int i = oldlevel + 1; i < (newlevel + 1); i++) {
			try {
				if(RewardsFunctions.getRewardObject(i) == null) {
					p.sendMessage("- N/A");
				}else {
					Reward r = RewardsFunctions.getRewardObject(i);
					p.sendMessage("- " + ChatColor.GOLD + r.getName());
				}
			} catch (SQLException e) {
				SNLCore.instance.getLogger().log(Level.SEVERE, "Error on player leveling up's reward! StackTrace:");
				e.printStackTrace();
			}	
		}
		
		p.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
	}
	
	public static Integer getXPForLevel(Integer level) {
		return (int) (Math.round((double) 0.04 * Math.pow(level, 3) + 0.8 * Math.pow(level, 3) + 2 * level) * 100);
	}
	
	public static Integer getLevelFromXP(Integer XP) {
		for(int level = 0; level < 296; level++) {
			long xp_formulated = (int) (Math.round((double) 0.04 * Math.pow(level, 3) + 0.8 * Math.pow(level, 3) + 2 * level) * 100);
			
			if(XP < getXPForLevel(1)) {
				return 0;
			}
			
			if(xp_formulated > XP) {
				return level - 1;
			}
		}
		
		return 0;
	}
	
	public static Integer getLevel(UUID u) throws SQLException {
		return getLevelFromXP(getXP(u));
	}
	
	public static Integer getXP(UUID u) throws SQLException {
		String uuid = u.toString();
		
		Connection con = databaseConnect();
		try {
			
			PreparedStatement statement = con.prepareStatement("SELECT xp FROM sentinelnetworklevels WHERE uuid='" + uuid + "';");
			
			ResultSet result = statement.executeQuery();
			
			ArrayList<Integer> xp_array = new ArrayList<Integer>();
			
			if(result != null) {
				while(result.next()) {
					xp_array.add(result.getInt("xp"));
				}
			}
			
			if(xp_array.size() != 1) {
				con.close();
				return null;
			}else {
				con.close();
				return xp_array.get(0);
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return null;
		}
	}
	
	public static boolean setXP(UUID u, Integer XP) throws SQLException {
		if(SNLCore.player_level.containsKey(u)) {
			SNLCore.player_level.remove(u);	
		}
		
		SNLCore.player_level.put(u, getLevelFromXP(XP));
		
		Connection db_connect = databaseConnect();
		try {
			
			PreparedStatement update = db_connect.prepareStatement("UPDATE sentinelnetworklevels SET xp='" + XP.toString() + "' WHERE uuid='" + u.toString() + "'");
			update.executeUpdate();
			
			db_connect.close();
			
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			db_connect.close();
			return false;
		}
	}
	
	public static boolean addUser(UUID u, Integer XP) throws SQLException {
		String uuid = u.toString();
		
		if(getXP(u) != null) {
			return true;
		}
		
		Connection con = databaseConnect();
		
		try {
			PreparedStatement posted = con.prepareStatement("INSERT INTO sentinelnetworklevels (uuid, xp) VALUES ('" + uuid + "', '" + XP.toString() + "')");
			posted.executeUpdate();
			
			con.close();
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return false;
		}
		
	}
	
	public static boolean userExists(UUID u) throws SQLException {
		String uuid = u.toString();
		
		Connection con = databaseConnect();
		
		try {
			PreparedStatement statement = con.prepareStatement("SELECT id FROM sentinelnetworklevels WHERE uuid='" + uuid + "';");
			
			ArrayList<Integer> id_array = new ArrayList<Integer>();
			ResultSet results = statement.executeQuery();
			
			if(results != null) {
				while(results.next()) {
					id_array.add(results.getInt("id"));
				}
				
				con.close();
			}
			
			if(id_array.size() != 1) {
				return false;
			}else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return false;
		}
	}
	
}