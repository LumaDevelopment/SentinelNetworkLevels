package net.lumadevelopment.netlvl;

public class Reward {

	private Integer level;
	private String server;
	private String command;
	private String name;
	
	public Reward(Integer level_i, String server_i, String command_i, String name_i) {
		level = level_i;
		server = server_i;
		command = command_i;
		name = name_i;
	}
	
	public Integer getLevel() {
		return level;
	}
	
	public String getServer() {
		return server;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getName() {
		return name;
	}
	
}
