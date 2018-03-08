import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ServerRoom {
	private List<ServerClient> users = new ArrayList<ServerClient>();
	private String roomPW;
	private String roomName;
	private ServerClient admin;
	
	//Buffer Contains uncoded messages
	private List<Message> messages = new ArrayList<Message>();
	
	private ServerGUI roomGUI;
	private String logFilePath;
	private int roomLogFileVersion;
	private ServerMain main;
	
	private boolean onlyEncryptedConnectionsAllowed = false;
	
	public ServerRoom(ServerMain main, String name, String roomPW, ServerClient admin,
			String logFilePath) {
		this.main = main;
		this.roomName = name;
		this.roomPW = roomPW;
		this.admin = admin;
		this.logFilePath = logFilePath;
		
		roomLogFileVersion = coder.getNormalVersion();
		try {
			FileAccess.append(logFilePath, roomLogFileVersion + "\t" + CommonUsedFeatures.getMD5String(roomPW));
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
		}
		
		roomGUI = new ServerGUI(this);
	}
	
	public void join(ServerClient user, String password) throws WrongPasswordException {
		if(!password.equals(roomPW))
			throw new WrongPasswordException();
		
		users.add(user);
		user.setRoom(this);
		
		sendOldData(user);
		sendSystemMessage("Der User " + user.getAccountName() +
				" ist dem Raum beigetreten.", null);
	}
	
	public boolean testJoin(ServerClient user, String password) {
		return password.equals(roomPW) &&
				(user.getClientVersion() != -1 || !this.onlyEncryptedConnectionsAllowed);
	}
	
	private void sendOldData(ServerClient user) {
		for(int i = 0; i < messages.size(); i++) {
			user.sendMessage(messages.get(i));
		}
	}
	
	private String reduceToSuportedChars(String message, int clientVersion) {
		StringBuilder newMessage = new StringBuilder();
		
		char defaultChar = '_';
		
		String supportedChars;
		if(clientVersion == 1)
			supportedChars = coder.getSupportedCharsV1();
		else if(clientVersion == 2)
			supportedChars = coder.getSupportedCharsV2();
		else
			return message;
		
		for(int i = 0; i < message.length(); i++) {
			if(supportedChars.indexOf(message.charAt(i)) == -1)
				newMessage.append(defaultChar);
			else
				newMessage.append(message.charAt(i));
		}
		
		return newMessage.toString();
	}
	
	public String decode(String message, int clientVersion) {
		if(clientVersion == 1) return coder.decodeV1(message, roomPW);
		if(clientVersion == 2) return coder.decodeV2(message, roomPW);
		
		//Only for websocket Clients
		if(clientVersion == -1) return message;
		return null;
	}
	
	public String code(String message, int clientVersion) {
		try {
			if(clientVersion == 1) return coder.codeV1(message, roomPW);
			if(clientVersion == 2) return coder.codeV2(message, roomPW);
			
			//for websockets
			if(clientVersion == -1) return message;
		} catch (IOException e) {
			try {
				String newMessage = reduceToSuportedChars(message, clientVersion);
				if(clientVersion == 1) return coder.codeV1(newMessage, roomPW);
				if(clientVersion == 2) return coder.codeV2(newMessage, roomPW);
			} catch (IOException e1) {
				CommonUsedFeatures.showErr(e1);
			}
		}
		return null;
	}
	
	public String getName() {
		return roomName;
	}
	
	private void addToMessagesBuffer(Message toAdd) {
		messages.add(toAdd);
		try {
			FileAccess.append(logFilePath, toAdd.getUserName() + "\t"
					+ this.code(toAdd.getMessage(), roomLogFileVersion) + "\t"+ toAdd.getTime() +
					"\t" + CommonUsedFeatures.getMD5String(toAdd.getMessage()));
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
		}
		roomGUI.addLine(toAdd);
	}
	
	public void sendMessageToAll(String userName, String message, ServerClient not) {
		Message toSend = new Message(userName, message, CommonUsedFeatures.getTime());
		addToMessagesBuffer(toSend);
		
		for(int i = 0; i < users.size(); i++)
			if(!users.get(i).equals(not))
				users.get(i).sendMessage(toSend);
	}
	
	public void sendMessageToOne(String userName, String message, ServerClient who) {
		Message toSend = new Message(userName, message, CommonUsedFeatures.getTime());
		who.sendMessage(toSend);
	}
	
	public void exit(ServerClient user) {
		sendSystemMessage("Der User " + user.getAccountName() +
				" hat den Raum verlassen.", user);
		
		users.remove(user);
		user.setRoom(null);
	}
	
	public void sendSystemMessage(String message, ServerClient not) {
		sendMessageToAll("system", message, not);
	}
	
	public void sendSystemMessageToOne(String message, ServerClient who) {
		sendMessageToOne("system", message, who);
	}
	
	public void closeRoom(String ipAndPort, String username) {
		for(int i = 0; i < users.size(); i++) {
			users.get(i).roomClosedByAdmin();
		}
		main.removeRoom(this);
		roomGUI.setVisible(false);
		Logger.addLogEntry(Logger.DELETEROOM, ipAndPort, username, roomName);
	}
	
	public boolean userIsAdmin(ServerClient client) {
		return client.equals(admin);
	}
	
	public void kick(ServerClient doneBy, ServerClient toKick, String nameOfNew) {
		if(toKick == null) {
			sendSystemMessageToOne("Der user " + nameOfNew + " existiert nicht " +
					"oder ist kein mitglied dieses Raumes", doneBy);
			return;
		}
		if(doneBy.equals(admin)) {
			toKick.kickBy(doneBy.ipAndPort, doneBy.getAccountName());
			users.remove(toKick);
			toKick.setRoom(null);
			adminDisconnects(toKick);
			sendSystemMessage("Der User " + toKick.getAccountName() + " wurde vom Raumadministrator" +
					" hinausgeschmissen.", null);
		}
	}
	
	public void changeAdmin(ServerClient changeTo, ServerClient doneBy, String nameOfNew) {
		if(changeTo == null) {
			sendSystemMessageToOne("Der user " + nameOfNew + " existiert nicht " +
					"oder ist kein mitglied dieses Raumes", doneBy);
			return;
		}
		if(doneBy.equals(admin)) {
			admin = changeTo;
			
			for(int i = 0; i < users.size(); i++)
				users.get(i).announceNewAdmin(admin);
			
			sendSystemMessage("Der User " + changeTo.getAccountName() +
					" ist nun der Raumadministrator.", null);
			Logger.addLogEntry(Logger.ADMINCHANGE, doneBy.ipAndPort,
					doneBy.getAccountName(), this.roomName);
		}
	}
	
	public void adminDisconnects(ServerClient disconnects) {
		if(disconnects.equals(admin)) {
			if(users.get(0).equals(disconnects)) {
				if(users.size() > 1)
					changeAdmin(admin, users.get(1), "");
				else
					closeRoom("system", "automatic");
			}
			else {
				changeAdmin(admin, users.get(0), "");
			}
		}
	}

	public void setEncryptionMode(ServerClient wantsToDoIt, boolean value) {
		this.onlyEncryptedConnectionsAllowed = value;
		
		//Check if we have to kick some People because of Security
		if(this.onlyEncryptedConnectionsAllowed) {
			for(int i = 0; i < users.size(); i++) {
				if(users.get(i).getClientVersion() == -1) {
					users.get(i).kickBy("system", "automatic");
					
					ServerClient toKick = users.get(i);
					users.remove(toKick);
					toKick.setRoom(null);
					adminDisconnects(toKick);
					sendSystemMessage("Der User " + toKick.getAccountName() +
							" wurde aufgrund der neuen sicherheitsrichtlinien hinausgeschmissen.", null);
					i--;
				}
			}
		}
	}
}

@SuppressWarnings("serial")
class WrongPasswordException extends IOException {
	public WrongPasswordException() {
		super();
	}
}