import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class ServerClient{
	private Thread watcher;
	private ServerConnectionToClient connection;
	public ServerRoom room;
	private ServerMain main;
	private int clientVersion;
	private int clientPasswordVersion;
	
	private String PWCode;
	private String accountName;
	
	public InetAddress clientIP;
	public String ipAndPort;
	
	public static final int NOTLOGEDIN = 0;
	public static final int LOGEDIN = 2;
	public static final int CONNECTED = 4;
	public int state = NOTLOGEDIN;
	
	public ServerClient(Socket s, ServerMain main) throws IOException
	{
		this.main = main;
		clientIP = s.getInetAddress();
		ipAndPort = clientIP.toString() + ":" + s.getPort();
		connection = new ServerConnectionToClient(s, this);
		
		watcher = new Thread(connection);
		watcher.start();
		
		Logger.addLogEntry(Logger.CONNECT, ipAndPort, null, null);
	}
	
	protected void mainLoop()
	{
		if(connection.linesAvailable()) {
			String line = connection.getLine();
			String parts[] = line.split("\t");
			if(state == NOTLOGEDIN) {
				if(line.startsWith("login") && parts.length == 3) {
					if(main.accountExists(parts[1], decodePW(parts[2]))) {
						state = LOGEDIN;
						accountName = parts[1];
						connection.sendData("authOK");
						Logger.addLogEntry(Logger.LOGIN, ipAndPort, parts[1], null);
					}
					else {
						connection.sendData("authNotOK");
						Logger.addLogEntry(Logger.LOGINFAILED, ipAndPort, parts[1], null);
					}
				}
				else if(line.startsWith("createAccount")) {
					String works = main.createAccount(parts[1], decodePW(parts[2]), this);
					if(works == null) {
						state = LOGEDIN;
						accountName = parts[1];
						connection.sendData("createAccountOK\t");

						Logger.addLogEntry(Logger.CREATEACCOUNT, ipAndPort, parts[1], null);
						Logger.addLogEntry(Logger.LOGIN, ipAndPort, parts[1], null);
					}
					else {
						connection.sendData("createAccountNotOK\t" + works);
					}
				}
				else
					CommonUsedFeatures.showWrongDataFromClientMessage(this, line, "");
			}
			else if(state == LOGEDIN) {
				if(line.startsWith("join")) {
					try {
						ServerRoom r = main.getRoom(parts[1]);
						
						if(r != null && r.testJoin(this, decodePW(parts[2]))) {
							connection.sendData("entryOK");
							state = CONNECTED;
							r.join(this, decodePW(parts[2]));
						}
						else {
							connection.sendData("entryNotOK");
						}
						
						Logger.addLogEntry(Logger.JOIN, ipAndPort, accountName, parts[1]);
					}
					catch(WrongPasswordException e) {
						Logger.addLogEntry(Logger.JOINFAILED, ipAndPort, accountName, parts[1]);
					}
				}
				else if(line.startsWith("createRoom")) {
					String works = main.testCreateRoom(parts[1], decodePW(parts[2]), this);
					
					if(works == null) {
						state = CONNECTED;
						connection.sendData("createRoomOK\t");
						main.createRoom(parts[1], decodePW(parts[2]), this);
						
						Logger.addLogEntry(Logger.CREATEROOM, ipAndPort, accountName, parts[1]);
						Logger.addLogEntry(Logger.JOIN, ipAndPort, accountName, parts[1]);
					}
					else {
						connection.sendData("createRoomNotOK\t" + works);
					}
				}
				else if(line.startsWith("logout")) {
					logout();
				}
				else
					CommonUsedFeatures.showWrongDataFromClientMessage(this, line, "");
			}
			else if(state == CONNECTED) {
				if(line.startsWith("exit")) {
					exit();
				}
				else if(line.startsWith("kick")) {
					room.kick(this, main.getUser(parts[1]), parts[1]);
				}
				else if(line.startsWith("closeRoom")) {
					if(room.userIsAdmin(this)) {
						room.closeRoom(ipAndPort, accountName);
					}
				}
				else if(line.startsWith("admin")) {
					if(room.userIsAdmin(this)) {
						room.changeAdmin(main.getUser(parts[1]), this, parts[1]);
					}
				}
				else if(line.startsWith(this.accountName)) {
					String uncoded = room.decode(parts[1], clientVersion);
					
					//FIXME no md5 for websocket
					if(clientVersion == -1 || CommonUsedFeatures.getMD5String(uncoded).equals(parts[3]))
						//if correct message
						room.sendMessageToAll(accountName, uncoded, this);
					else
						CommonUsedFeatures.showWrongDataFromClientMessage(this, line, "Unable to decode correct");
				}
				else if(line.startsWith("encryptionNeeded")) {
					room.setEncryptionMode(this, !parts[1].equals("0"));
				}
				else {
					CommonUsedFeatures.showWrongDataFromClientMessage(this, line, "");
				}
			}
			else {
				CommonUsedFeatures.showProblem("Wrong state: " + state + "\tLine got: " + line);
			}
		}
	}

	public void setClientVersion(int version)
	{
		this.clientVersion = version;
	}

	public void setClientPasswordVersion(int version) {
		this.clientPasswordVersion = version;
	}
	
	public void sendMessage(Message message)
	{
		//[name]\t[verschluesselter Text]\t[Zeit]\t[md5 des Textes]
		connection.sendData(message.getUserName() + 
				"\t" + room.code(message.getMessage(), clientVersion) +
				"\t"+ message.getTime() +
				"\t" + CommonUsedFeatures.getMD5String(message.getMessage()));
	}
	
	public void sendNewPWCode()
	{
		PWCode = createPWCode();
		connection.sendData("coding\t\t" + PWCode);
	}
	
	private String createPWCode()
	{
		byte[] code = new byte[30];
		
		for(int i = 0; i < code.length; i++)
			code[i] = (byte) (256 * Math.random());
		
		return CommonUsedFeatures.base64(code);
	}

	private String decodePW(String codedPassword) {
		if(this.clientPasswordVersion == 1)
			return PWCoder.decodeV1(codedPassword, PWCode);

		//Only for websocket Clients
		if(this.clientPasswordVersion == -1)
			return codedPassword;
		
		return null;
	}

	public int getClientVersion()
	{
		return this.clientVersion;
	}
	
	public String getAccountName()
	{
		return accountName;
	}

	public void roomClosedByAdmin()
	{
		connection.sendData("roomdeleted");
		state = LOGEDIN;
	}
	
	public void setRoom(ServerRoom room)
	{
		this.room = room;
	}

	public void clientDisconnects()
	{
		if(room.userIsAdmin(this))
			room.adminDisconnects(this);
		
		if(state == CONNECTED)
			exit();
		
		if(state == LOGEDIN)
			logout();
		
		disconnect();
	}
	
	private void logout()
	{
		Logger.addLogEntry(Logger.LOGOUT, ipAndPort, accountName, null);
		
		accountName = null;
		state = NOTLOGEDIN;
	}

	private void exit()
	{
		Logger.addLogEntry(Logger.EXIT, ipAndPort, accountName, room.getName());
		
		state = LOGEDIN;
		this.room.exit(this);
	}

	public void closeConnection()
	{
		connection.closeStream();
		disconnect();
	}
	
	private void disconnect()
	{
		Logger.addLogEntry(Logger.DISCONNECT, ipAndPort, null, null);
		main.removeUser(this);
	}

	public void kickBy(String ipAndPort, String accountName) {
		state = LOGEDIN;
		
		connection.sendData("kick\t" + accountName);
		
		Logger.addLogEntry(Logger.KICK, ipAndPort,
				accountName, room.getName());
	}

	public void announceNewAdmin(ServerClient admin) {
		connection.sendData("admin\t" + admin.getAccountName());
	}
}

class Message
{
	private String userName;
	//Uncodeded Message
	private String message;
	private String time;

	public Message(String userName, String message, String time)
	{
		this.userName = userName;
		this.message = message;
		this.time = time;
	}

	public String getTime()
	{
		return time;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public String getUserName()
	{
		return userName;
	}
}