package Client;
import lib.Command;
import lib.ConfigParser;
import lib.Info;
import lib.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    String username;
    boolean connected = false;
    Socket socket;
    String address = "localhost";
    int port = 2000;

    static WriteThread writeThread;
    static ReadThread readThread;

    BufferedReader in;
    PrintWriter out;


    public Client(Path config) {
        ConfigParser configParser = new ConfigParser(config);
        try{
            configParser.parse();
            address = configParser.getValue("address");
            port = configParser.getPort();
        }
        catch (ParseException e){
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchFileException e) {
            System.err.println("Config not found: %s \nUsing default configuration".formatted(e.getMessage()));
        } catch (Exception e){
            System.out.println("hey");
        }
    }

    public void run() {


        System.out.println("Establishing connection to the server...");

        try {
            socket = new Socket(address, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Could not connect to the server :(");
            System.exit(-1);
        }

        readThread = new ReadThread();
        writeThread = new WriteThread();
        assignUsername();
        readThread.start();
        writeThread.start();

    }

    public void assignUsername() {

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]+$");

        System.out.println("Please enter your username: ");
        while (username == null) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine().trim();
            if (!pattern.matcher(line).find()) {
                System.out.println("Invalid username, use only latin letters and/or digits, or _ and -");
                continue;
            }
            out.println(line);

            if (readThread.getBoolResponse()) {
                username = line;
            } else {
                System.out.println("This username already exists. Try another one: ");
            }
        }
    }

    public void printPrompt() {
        System.out.println("\r");
        System.out.printf("%s > ", username);
    }

    class ReadThread extends Thread {

        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("msg")) {
                        Message message = Message.fromRaw(line);
                        if (message.getSender().equals(username)) {
                            continue;
                        }
                        System.out.println("\r\u001B[36m" + message.parsePretty()+"\u001B[0m");
                        System.out.flush();
                    } else if (line.startsWith("info")) {
                        Info info = Info.fromRaw(line);
                        if (info.ok()) continue;
                        System.out.println("\r\u001B[35m" + info.parsePretty()+ "\u001B[0m");
                        System.out.flush();
                    }
                    printPrompt();
                }
            } catch (SocketException e) {
                System.out.println("Disconnected from the server");
            } catch (IOException e) {
                System.out.println("Error while reading server");
            }
        }

        public boolean getBoolResponse() {
            String response = getResponse();
            if (Info.fromRaw(response).ok()) {
                return true;
            }
            return false;
        }

        public String getResponse() {
            String response = "";
            try {
                response = in.readLine();
            } catch (IOException e) {
                System.out.println("Could not read from the server :(");
                cleanAndExit(-1);
            }

            return response;
        }
    }

    class WriteThread extends Thread {
        boolean running = false;
        Scanner scanner = new Scanner(System.in);

        public void stopThread() {
            running = false;
        }


        public void run() {

            running = true;
            try {
                String line;
                while (running) {
                    printPrompt();
                    line = scanner.nextLine();
                    interpret(line);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void disconnect() {
            print(new Command("disconnect"));
        }

        public void print(Object s) {
            out.println(s);
        }

        public void interpret(String line) {
            line = line.trim();

            try {

                if (!line.startsWith("/")) {
                    Message message = Message.toAll(line, username);
                    print(message);
                    return;
                }

                String command = Command.fromUserInput(line).getText();

                switch (command) {
                    case "exit" -> {
                        System.out.println("Disconnecting from server");
                        disconnect();
                        System.exit(0);
                    }
                    case "m" -> {
                        sendTo(line);
                    }
                    case "clear" -> {
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                    }
                    case "banwords" -> {
                        requestBanWords();
                    }
                    case "help" -> {
                        printHelp();
                    }
                    default -> {
                        System.out.println("Unknown command. To get list of commands print /help");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        public void sendTo(String line) throws Exception {
            Pattern pattern = Pattern.compile("/m\\s+(!)?(?:\\(([^)]+)\\)|([^\\s]+))\\s*(.*)");
            Matcher matcher = pattern.matcher(line);

            boolean except_receivers = false;

            if (matcher.find()) {

                if (matcher.group(1) != null) {
                    except_receivers = true;
                }
                String recievers = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                if (matcher.group(4) == null) {
                    throw new Exception("No message text specified.");
                }

                Message message = new Message(matcher.group(4), username, recievers, except_receivers);
                print(message);
            }

        }

        public void requestBanWords() {
            print(new Command("banwords"));
        }
    }

    public void cleanAndExit(int code) {
        writeThread.disconnect();
        System.exit(code);
    }

    public void printHelp() {
        System.out.println("""
                /m (user1,user2) [Text] - send message to multiple users
                /m user1 [Text] - send message to one user
                /banwords - get list of banwords
                /clear - clear the skin
                /exit - exit the chat
                """);
    }

}
