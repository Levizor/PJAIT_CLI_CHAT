package Client;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Path config = Path.of("client.conf");
        Client client = new Client(config);
        client.run();
    }
}
