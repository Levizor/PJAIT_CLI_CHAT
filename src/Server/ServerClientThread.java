package Server;

import lib.Command;
import lib.Info;
import lib.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class ServerClientThread extends Thread {
    Socket socket;
    boolean connected = false;
    BufferedReader in;
    PrintWriter out;

    String username;
    static final Info OK = new Info("ok");

    public ServerClientThread(Socket socket) {
        super("" + socket.getPort());
        this.socket = socket;
    }

    public void assignName() {

        System.out.println("Running client on port " + socket.getPort());
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String name = in.readLine();
            while (userNameExist(name)) {
                print(new Info("username_exists"));
                name = in.readLine();
            }
            agree();
            username = name;
            connected = true;
            notifyConnected();
        } catch (Exception e) {
        }
    }

    public void agree() {
        print(OK);
    }

    public void printInfo(String s) {
        Info info = new Info(s);
        print(info);
    }

    public void notifyConnected() {
        String info = (new Info(username + " connected.")).toString();
        Server.clientThreads.stream().forEach(ct -> ct.print(info));
    }

    public void disconnect() {
        connected = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.clientThreads.remove(this);
        sendAll(new Info("%s disconnected".formatted(username)));

    }

    public void run() {
        assignName();
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("cmd")) {
                    Command command = Command.fromRaw(line);
                    switch (command.getText()) {
                        case "disconnect" -> {
                            disconnect();
                        }
                        case "banwords" -> {
                            printInfo(String.join(", ", Server.getBannedPhrases()));
                        }
                    }
                } else if (line.startsWith("msg")) {
                    Message message = Message.fromRaw(line);
                    if (message.containsBannedPhrases()) {
                        printInfo("Message contains banned phrases. To look at the list of banned phrases use /banwords");
                        continue;
                    }
                    if (message.toAll()) {
                        agree();
                        sendAll(line);
                    } else {
                        sendTo(message);
                    }

                }
            }
        } catch (SocketException e) {
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }finally {
            try{
                socket.close();
                disconnect();
            } catch (IOException e) {
                System.err.println("Error closing a socket");
            }
        }
    }

    public void print(Object s) {
        System.out.println(s);
        out.println(s);
    }

    public void sendAll(Object s) {
        Server.clientThreads.forEach(sct -> sct.print(s));
    }

    public void sendTo(Message message) {

        boolean except = message.isExcept_receivers();
        String[] receivers = message.getReceivers();
        String[] notInList = (String[]) Arrays.stream(receivers)
                .filter(
                        r -> Server.clientThreads.stream().noneMatch(ct -> r.equals(ct.username))
                )
                .toArray(String[]::new);
        if (notInList.length > 0) {
            printInfo("%s do not exist: %s".formatted(except ? "Excepted receivers" : "Receivers", String.join(", ", notInList)));
            return;
        }
        agree();

        if(except) {
            Server.clientThreads.stream()
                    .filter(ct -> Arrays.stream(receivers).noneMatch(r -> r.equals(ct.username)))
                    .forEach(ct -> ct.print(message));
        }else {

            Server.clientThreads.stream().
                    filter(
                            ct ->
                                    Arrays.stream(receivers)
                                            .peek(System.out::println)
                                            .anyMatch(
                                                    r -> r.equals(ct.username)
                                            )
                    )
                    .forEach(ct -> ct.print(message));
        }

    }

    public static boolean userNameExist(String s) {
        return Server.clientThreads.stream().anyMatch(sct -> s.equals(sct.username));
    }

}
