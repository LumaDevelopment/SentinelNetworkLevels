package net.lumadevelopment.netlvl;

public class SNLCommandObj {
	
	private String command;
	private String server;
	private String uuid;
	
	public SNLCommandObj(String command_i, String server_i, String uuid_i) {
		command = command_i;
		server = server_i;
		uuid = uuid_i;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getServer() {
		return server;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public boolean commandExecuted() {
		try {
			RewardsFunctions.removeCommand(this);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
