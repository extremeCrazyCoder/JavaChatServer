public class ServerStarter {
    /**
     * Nach dem Start ( und eventuell nach einer gewissen Zeit)
     *         aenderung des Stringes mit dem die Passwoerter Verschluesselt werden
     *         Vom Client:                typ (websocket: [handshake], java: JAVA) & version
     *         Vom Server:                coding\t\t[Schluessel]
     * 
     * Protokoll:
     * 1) main Login
     *         Zum Server:                login\t[username]\t[verschluesseltes Passwort]
     *         Vom Server bei Erfolg:    authOK\t
     *         Bei Miserfolg:            authNotOK\t[Message why]
     * 
     * 2) main Login erstellen eines Accounts
     *         Zum Server:                createAccount\t[username]\t[verschluesseltes Passwort]
     *         Vom Server (erfolg):    createAccountOK\t
     *         Vom Server (miserfolg):    createAccountNotOK\t[Message why]
     * 
     * 3) Einloggen in einen Raum
     *         Senden zum Server:        join\t[raum]\t[verschluesseltes Passwort]
     *         Vom Server bei Erfolg:    entryOK
     *         Bei Miserfolg:            entryNotOK
     * 
     * 4) Bekommen aller alten Daten dieses Raumes (und der weiteren nachrichten)
     *         Vom Server:                [name]\t[verschluesselter Text]\t[Zeit]\t[md5 des Textes]
     * 
     * 5) Senden einer nachricht zum Server
     *         Zum Server:                [name]\t[verschluesselter Text]\t\t[md5 des Textes]
     * 
     * 6) Erstellen & verlassen eines Raumes
     *         Zum Server (Erstellen:    createRoom\t[raum]\t[verschluesseltes Passwort]
     *         Vom Server (erfolg):    createRoomOK\t
     *         Vom Server (miserfolg):    createRoomNotOK\t[Message why]
     *         Zum Server (verlassen):    exit\t\t[name]
     *         Vom Server (Admin Loescht Raum): roomdeleted
     * 
     * 7) Ausloggen
     *         Zum Server:                logout\t\t
     * 
     * 8) Vom Raumadministrator
     *         User kicken:            kick\t[username]
     *         Vom Server(bei kick):    kick\t[username des Admins]
     *         Raum schliessen:        closeRoom
     *         Neuen admin ernennen:    admin\t[username]
     *         Vom Server(bei change):    admin\t[username des neuen Admins]
     *         Änderung der Sicherheit (Encryption)    encryptionNeeded\t[neuer Status (0,1)]
     */
    
    //TODO Server GUI neues Layout (kick)
    //TODO Mehr sprachen
    //TODO Eigenes gui fuer die gesammt server verwaltung
    
    public static void main(String[] args) {
        ServerMain s = new ServerMain();
        if(args.length == 0) {
            s.start(System.getProperty("user.dir"));
        }
        else {
            s.start(args[0]);
        }
    }
}