import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends Thread {
    ServerSocket providerSocket;
    Socket connection = null;
    Socket listenerFollow = null;
    ConcurrentHashMap<String, String> logininfo;
    Scanner s;
    String projectDir = Paths.get("").toAbsolutePath().toString();
    File loginInfoFile = new File(projectDir + "\\ServerDirectory\\LoginInfo.txt");
    public static ConcurrentHashMap<String,ClientHandler> activeClients = new ConcurrentHashMap<>();
    public static HashMap<String, Boolean> fileLock = new HashMap<>();


    public static void main(String[] args){
        new Server().openServer();
    }

    void openServer() {
        try {

            /* Create Server Socket */
            providerSocket = new ServerSocket(1337, 8);
            System.out.println("Server is up.");

            try {
                s = new Scanner(loginInfoFile);
            } catch (java.io.FileNotFoundException error) {
                error.printStackTrace();
            }

            logininfo = new ConcurrentHashMap<String, String>();
            while (s.hasNext()) {
                String c = s.next();
                String p = s.next();
                logininfo.put(c, p);
            }

            for (Map.Entry<String, String> entry : logininfo.entrySet()) {
                String key = entry.getKey();
                fileLock.put(key, false);
            }

            while (true) {
                /* Accept the connection */
                connection = providerSocket.accept();
                listenerFollow = providerSocket.accept();
                new ClientHandler(connection, listenerFollow).start();

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            }catch (IOException error){
                error.printStackTrace();
            }
        }

    }

}
