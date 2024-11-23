package Server;

import lib.ConfigParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Server {
    String address="localhost";
    int port;
    String name;
    static Set<String> bannedPhrases;

    ServerSocket serverSocket;
    Socket clientSocket;

    static List<ServerClientThread> clientThreads = new LinkedList<>();

    public Server(Path conf) {
        ConfigParser parser = new ConfigParser(conf);
        try{
            parser.parse();
        }
        catch(NoSuchFileException e){
            System.err.println("Config file does not exist");
            System.out.println("Default configuration will be loaded:");
            System.out.printf("Port: %d\nName: %s\n", port, name);
        }
        catch (ParseException e){
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(parser);
        port = parser.getPort();
        name = parser.getName();
        bannedPhrases = parser.getBannedPhrases();
    }

    public void run(){
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Started server instance\nListening on port: " + port);

        while(true){
            try{
                clientSocket = serverSocket.accept();
            }catch (IOException e){
                System.out.println("Client connection failed");
            }

            ServerClientThread clientThread = new ServerClientThread(clientSocket);
            clientThreads.add(clientThread);
            clientThread.start();

        }
    }

    public static Set<String> getBannedPhrases(){
        return bannedPhrases;
    }

}
