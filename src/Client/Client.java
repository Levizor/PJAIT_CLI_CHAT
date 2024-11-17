package Client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    String username;
    boolean connected = false;
    Socket socket;
    String address = "localhost";
    int port = 2000;

    private WriteThread writeThread;
    private ReadThread readThread;

    BufferedReader in;
    PrintWriter out;

    public Client(){
    }

    public void run(){


        System.out.println("Establishing connection to the server...");

        try{
            socket = new Socket(address, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e){
            System.out.println("Could not connect to the server :(");
            System.exit(-1);
        }

        assignUsername();
        writeThread = new WriteThread();
        readThread = new ReadThread();
        readThread.start();
        writeThread.start();

    }

    public void assignUsername(){

        System.out.println("Please enter your username: ");
        while(username == null){
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine().trim();
            out.println(line);
            String response= "";
            try{
                response=in.readLine();
            }catch (IOException e){
                System.out.println("Could not read from the server :(");
                System.exit(-1);
            }
            if(response.equals("info:ok")){
                username = line;
            }else if(response.equals("info:username_exists")){
                System.out.println("This username already exists. Try another one: ");
            }
        }
    }

    public void printPrompt(){
        System.out.printf("%s > ", username);
    }

    class ReadThread extends Thread{


        public void run(){
            try{
                String line;
                while((line = in.readLine()) != null){
                    System.out.println("\r\033[2K"+line);
                    printPrompt();
                }
            }catch (SocketException e){
                System.out.println("Disconnected from the server");
            }catch (IOException e){
                System.out.println("Error while reading server");
            }
        }
    }

    class WriteThread extends Thread{
        boolean running = false;

        public void stopThread(){
            running = false;
        }


        public void run(){

            running = true;
            try{
                Scanner scanner = new Scanner(System.in);
                String line;
                while (running){
                    printPrompt();
                    line = scanner.nextLine();
                    interpret(line);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void disconnect(){
            out.println("cmd:disconnect");
        }

        public void interpret(String line){
            line = line.trim();

            if(!line.startsWith("/")){
                out.println("msg:all:" + line);
            }

            String command = line.substring(1, Helper.getFirstWordEndIndex(line));
            if(command.equals("exit")){
                System.out.println("Disconnecting from server");
                disconnect();
                System.exit(0);
            }

        }
    }

}
