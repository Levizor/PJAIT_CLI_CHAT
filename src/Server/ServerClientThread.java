package Server;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ServerClientThread extends Thread{
    Socket socket;
    boolean connected = false;
    BufferedReader in;
    PrintWriter out;

    String username;

    public ServerClientThread(Socket socket) {
        super(""+socket.getPort());
        this.socket = socket;
    }

    public void loop() throws IOException{
    }

    public void assignName(){

        System.out.println("Running client on port "+socket.getPort());
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String name = in.readLine();
            while (userNameExist(name)){
                print("info:username_exists");
                name = in.readLine();
            }

            username = name;
            connected = true;
            print("info:ok");
            System.out.println(username + " connected");

        }catch (Exception e) {
        }
    }

    public void disconnect(){
        connected = false;
        try{
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(username + " disconnected");
        Server.clientThreads.remove(this);
        sendAll("info:disconnected:%s".formatted(username));
    }

    public void run(){
        assignName();
        try{
            String line;
            while(connected && (line=in.readLine())!=null){
                System.out.println(line);
                if(line.startsWith("cmd")){
                    if(line.equals("cmd:disconnect")){
                        disconnect();
                    }
                }else if(line.startsWith("info")){

                }else if(line.startsWith("msg")){
                    String[] arr = line.split(":");
                    if (arr.length < 3){
                        print("info:bad");
                        return;
                    }
                    if(arr[1].equals("all")){
                        sendAll("msg:%s:".formatted(username) + line.substring("msg:all:".length()));
                    }

                }

            }
        }catch(SocketException e){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(String s){
        out.println(s);
    }

    public void sendAll(String s){
        System.out.println("sending string "+s);
        Server.clientThreads.stream().forEach(sct -> sct.print(s));
    }

    public static boolean userNameExist(String s){
        return Server.clientThreads.stream().anyMatch(sct -> s.equals(sct.username));
    }

}
