package net.lumadevelopment.netlvl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RewardsFunctions {

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
	
	public static boolean createSnlRewardsTable() throws SQLException {
		Connection db_connect = databaseConnect();
		
		if(db_connect == null) {
			return false;
		}
		
		try {
			PreparedStatement create = db_connect.prepareStatement("CREATE TABLE IF NOT EXISTS snlrewards ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "level INT NOT NULL, "
					+ "server VARCHAR(256) NOT NULL, "
					+ "command VARCHAR(256) NOT NULL, "
					+ "name VARCHAR(256) NOT NULL, "
					+ "PRIMARY KEY ( id ));");
			create.executeUpdate();
			
			db_connect.close();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			db_connect.close();
			return false;
		}
		
	}
	
	public static boolean createSnlCommandsTableTwo() throws SQLException {
        Connection db_connect = databaseConnect();
		
		if(db_connect == null) {
			return false;
		}
		
		try {
			PreparedStatement create = db_connect.prepareStatement("CREATE TABLE IF NOT EXISTS snlcommands ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "command VARCHAR(256) NOT NULL, "
					+ "server VARCHAR(256) NOT NULL, "
					+ "uuid VARCHAR(256) NOT NULL, "
					+ "PRIMARY KEY ( id ));");
			create.executeUpdate();
			
			db_connect.close();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			db_connect.close();
			return false;
		}
	}
	
	public static void processRewards(UUID uuid, Integer oldlevel, Integer newlevel) {
		for(int i = oldlevel + 1; i < (newlevel + 1); i++) {
			try {
				if(RewardsFunctions.getRewardObject(i) != null) {
					Reward r = RewardsFunctions.getRewardObject(i);
					
					String cmd = r.getCommand().replaceAll("~PLAYER~", Bukkit.getOfflinePlayer(uuid).getName());
					RewardsFunctions.createCommand(cmd, r.getServer(), uuid.toString());
				}
			} catch (SQLException e) {
				SNLCore.instance.getLogger().log(Level.SEVERE, "Error on player leveling up's reward! StackTrace:");
				e.printStackTrace();
			}	
		}
	}
	
	public static List<SNLCommandObj> getRewardCommands() throws SQLException{
		Connection con = databaseConnect();
		
		try {
			
            PreparedStatement sta = con.prepareStatement("SELECT * FROM snlcommands");
			
			ResultSet result = sta.executeQuery();
			
			List<SNLCommandObj> commands = new ArrayList<SNLCommandObj>();
			
			if(result != null) {
				while(result.next()) {
					SNLCommandObj obj = new SNLCommandObj(result.getString("command"), result.getString("server"), result.getString("uuid"));
					commands.add(obj);
				}
			}
			
			con.close();
			
			return commands;
		}catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return null;
		}
	}
	
	public static boolean removeCommand(SNLCommandObj obj) throws SQLException {
		Connection con = databaseConnect();
		
		try {
			PreparedStatement posted = con.prepareStatement("DELETE FROM snlcommands WHERE command='" + obj.getCommand() + "' AND server='" + obj.getServer() + "';");
			posted.executeUpdate();
			
			con.close();
			
			return true;
		}catch(SQLException e) {
			e.printStackTrace();
			con.close();
			return false;
		}
	}
	
	public static boolean createCommand(String command, String server, String uuid) throws SQLException {
		Connection con = databaseConnect();
		
		try {
			PreparedStatement posted = con.prepareStatement("INSERT INTO snlcommands (command, server, uuid) VALUES ('" + command + "', '" + server + "', '" + uuid + "')");
			posted.executeUpdate();
			
			con.close();
			
			return true;
		}catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return false;
		}
	}
	
	public static boolean createReward(Integer level, String server, String command, String name) throws SQLException {
		
		String server_sql;
		String name_sql;
		
		if(server == null) {
			server_sql = "global";
		}else {
			server_sql = server;
		}
		
		if(name == null) {
			name_sql = command;
		}else {
			name_sql = name;
		}
		
		Connection con = databaseConnect();
		
		try {
			PreparedStatement posted = con.prepareStatement("INSERT INTO snlrewards (level, server, command, name) VALUES ("
					+ "'" + level + "', "
					+ "'" + server_sql + "', "
					+ "'" + command + "', "
					+ "'" + name_sql + "'"
					+ ")");
			posted.executeUpdate();
			
			con.close();
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return false;
		}
		
	}
	
	public static boolean updateReward(Integer level, String server, String command, String name) throws SQLException {
		if(level == null) {
			return false;
		}
		
		Connection con = databaseConnect();
		
		if(server != null) {
			try {
				
				PreparedStatement update = con.prepareStatement("UPDATE snlrewards SET server='" + server + "' WHERE level='" + level + "'");
				update.executeUpdate();
			} catch(SQLException e) {
				e.printStackTrace();
				con.close();
				return false;
			}
		}
		
		if(command != null) {
			try {
				PreparedStatement update = con.prepareStatement("UPDATE snlrewards SET command='" + command + "' WHERE level='" + level + "'");
				update.executeUpdate();
			} catch(SQLException e) {
				e.printStackTrace();
				con.close();
				return false;
			}
		}
		
		
		if(name != null) {
			try {
				PreparedStatement update = con.prepareStatement("UPDATE snlrewards SET name='" + name + "' WHERE level='" + level + "'");
				update.executeUpdate();
			} catch(SQLException e) {
				e.printStackTrace();
				con.close();
				return false;
			}
		}
		
		con.close();
		
		return true;
	}
	
	public static Reward getRewardObject(Integer level) throws SQLException {
		Connection con = databaseConnect();
		
		try {
			PreparedStatement sta = con.prepareStatement("SELECT * FROM snlrewards WHERE level='" + level + "';");
			
			ResultSet result = sta.executeQuery();
			
			List<Reward> rewards = new ArrayList<Reward>();
			if(result != null) {
				while(result.next()) {
					Reward rew = new Reward(result.getInt("level"), result.getString("server"), result.getString("command"), result.getString("name"));
					rewards.add(rew);
				}
			}
			
			con.close();
			
			if(rewards.size() != 1) {
				return null;
			}else {
				return rewards.get(0);
			}
		}catch (SQLException e) {
			e.printStackTrace();
			con.close();
			return null;
		}
	}
	
}
