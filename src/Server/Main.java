package Server;
import java.io.File;
import java.net.*;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args){
        Path path = Path.of("server.conf");
        Server server = new Server(path);
        server.run();
    }
}
