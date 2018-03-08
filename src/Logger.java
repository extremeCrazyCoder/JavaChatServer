import java.io.IOException;


public class Logger {
    public static final int CONNECT = 0;
    public static final int DISCONNECT = 1;
    public static final int LOGIN = 2;
    public static final int LOGINFAILED = 3;
    public static final int CREATEACCOUNT = 4;
    public static final int LOGOUT = 5;
    public static final int JOIN = 6;
    public static final int JOINFAILED = 7;
    public static final int CREATEROOM = 8;
    public static final int DELETEROOM = 9;
    public static final int EXIT = 10;
    public static final int KICK = 11;
    public static final int ADMINCHANGE = 12;
    
    private static String logFilePath;
    
    public static void init(String logFile) {
        Logger.logFilePath = logFile;
        try {
            FileAccess.write(logFilePath, "");
        } catch (IOException e) {
            CommonUsedFeatures.showErr(e);
        }
    }

    public static void addLogEntry(int mode, String ipAndPort, String accountName,
            String roomName) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(ipAndPort);
        logMessage.append("\t");
        
        switch(mode) {
            case CONNECT: logMessage.append("connected"); break;
            case DISCONNECT: logMessage.append("disconnected"); break;
            case LOGIN: logMessage.append("login"); break;
            case LOGINFAILED: logMessage.append("loginfailed"); break;
            case CREATEACCOUNT: logMessage.append("createAcc"); break;
            case LOGOUT: logMessage.append("logout"); break;
            case JOIN: logMessage.append("join"); break;
            case JOINFAILED: logMessage.append("joinfailed"); break;
            case CREATEROOM: logMessage.append("createroom"); break;
            case DELETEROOM: logMessage.append("deleteRoom"); break;
            case EXIT: logMessage.append("exit"); break;
            case KICK: logMessage.append("kick"); break;
            case ADMINCHANGE: logMessage.append("adminchange"); break;
        }
        
        logMessage.append("\t");
        if(accountName != null)
            logMessage.append(accountName);
        logMessage.append("\t");
        if(accountName != null)
            logMessage.append(roomName);
        
        try {
            FileAccess.append(logFilePath, logMessage.toString());
        } catch (IOException e) {
            CommonUsedFeatures.showErr(e);
        }
    }
}