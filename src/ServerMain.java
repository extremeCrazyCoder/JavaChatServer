import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ServerMain {
	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<ServerRoom> rooms = new ArrayList<ServerRoom>();
	private ServerSocket serverS;
	private Timer startTreads;
	private long delayBetweenQuestions = 20;
	private int numLogDir;
	private String roomsLogfilePath;
	private int numRooms = 0;
	private String settingsPath;
	private String workingDirectory;
	
	//TODO CLient disconnects
	public void start(String workingDirectory) {
		this.workingDirectory = workingDirectory;
		CommonUsedFeatures.showHint("Working directory: " + System.getProperty("user.dir"));
		
		initShutdownHook();
		settingsPath = workingDirectory + "/settings.set";
		
		try {
			String settings[] = FileAccess.read(settingsPath);
			numLogDir = Integer.parseInt(settings[0]) + 1;
			settings[0] = "" + numLogDir;
			FileAccess.write(settingsPath,  settings);
		}
		catch(IOException e) {
			numLogDir = 1;
			install();
		}
		
		FileAccess.createDir(workingDirectory + "/logs/" + CommonUsedFeatures.formatNumber(numLogDir, 3) + "/");
		Logger.init(workingDirectory + "/logs/" + CommonUsedFeatures.formatNumber(numLogDir, 3) + "/clients.log");
		roomsLogfilePath = workingDirectory + "/logs/" + CommonUsedFeatures.formatNumber(numLogDir, 3) + "/rooms.log";
		
		startTreads = new Timer();
		startTreads.scheduleAtFixedRate(new TimerTask() {
			public void run() {mainLoop();}}, 0, delayBetweenQuestions);
		
		try {
			serverS = new ServerSocket(5000);
			while(true){
				Socket s = serverS.accept();
				ServerClient c = new ServerClient(s, this);
				
				clients.add(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void install() {
		StringBuilder settings = new StringBuilder();
		
		settings.append("1");
		
		try {
			FileAccess.write(settingsPath, settings.toString());
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
		}
		
		FileAccess.createDir(workingDirectory + "/logs/");
	}

	private void initShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override public void run() {stop();}}));
	}
	
	protected void mainLoop() {
		for(int i = 0; i < clients.size(); i++) {
			clients.get(i).mainLoop();
		}
	}
	
	public void stop() {
		try {
			serverS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ServerRoom getRoom(String roomName) {
		for(int i = 0; i < rooms.size(); i++)
			if(rooms.get(i).getName().equals(roomName))
				return rooms.get(i);
		
		return null;
	}

	public String createRoom(String roomName, String roomPW, ServerClient admin) {
		ServerRoom room = new ServerRoom(this, roomName, roomPW, admin, workingDirectory + "/logs/" +
				CommonUsedFeatures.formatNumber(numLogDir, 3) +
				"/room_" + CommonUsedFeatures.formatNumber(numRooms + 1, 3) + ".log");
		
		try {
			FileAccess.append(roomsLogfilePath, "" + (numRooms + 1) + "\t" + roomName);
		} catch (IOException e1) {
			CommonUsedFeatures.showErr(e1);
			return e1.getMessage();
		}
		
		try {
			room.join(admin, roomPW);
		} catch (WrongPasswordException e) {
			CommonUsedFeatures.showErr(e);
		}
		numRooms++;
		rooms.add(room);
		
		return null;
	}

	public String testCreateRoom(String roomName, String password,
			ServerClient serverClient) {
		
		//TODO Umlaute erneuern
		if(serverClient.getClientVersion() == -1) return "Clients ohne verschl\u00fcsselung d\u00fcrfen aus " +
				"sicherheitsgr\u00fcnden keine R\u00e4ume erstellen";
		if(roomnameExists(roomName)) return "Der Raumnahme existiert bereits";
		return null;
	}

	private boolean roomnameExists(String roomName) {
		return getRoom(roomName) != null;
	}

	public boolean accountExists(String username, String password) {
		String complete = username + "\t" + CommonUsedFeatures.getMD5String(password);
		
		String[] allAccount;
		try {
			allAccount = FileAccess.read(workingDirectory + "/accounts.set");
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
			return false;
		}
		
		for(int i = 0; i < allAccount.length; i++) {
			if(allAccount[i].equals(complete))
				return true;
		}
		return false;
	}

	public String createAccount(String username, String password, ServerClient client) {
		if(accountNameExists(username))
			return "Benutzername bereits vorhanden";
		
		try {
			FileAccess.append(workingDirectory + "/accounts.set", username + "\t" + CommonUsedFeatures.getMD5String(password));
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
			return e.getMessage();
		}
		
		return null;
	}

	private boolean accountNameExists(String username) {
		String[] allAccount;
		try {
			allAccount = FileAccess.read(workingDirectory + "/accounts.set");
		} catch (IOException e) {
			CommonUsedFeatures.showErr(e);
			return false;
		}
		
		for(int i = 0; i < allAccount.length; i++) {
			if(allAccount[i].split("\t")[0].equals(username))
				return true;
		}
		return false;
	}

	public void removeRoom(ServerRoom toRemove) {
		rooms.remove(toRemove);
	}

	public void removeUser(ServerClient client) {
		clients.remove(client);
	}

	public ServerClient getUser(String accountName) {
		for(int i = 0; i < clients.size(); i++)
			if(clients.get(i).getAccountName().equals(accountName))
				return clients.get(i);
		
		return null;
	}
}