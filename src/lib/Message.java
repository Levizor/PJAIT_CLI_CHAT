package lib;

import Server.Server;

import java.time.LocalTime;
import java.util.Arrays;

public class Message extends Sendable{
    String[] receivers;
    String sender;
    String text;
    boolean except_receivers=false;


    public Message() {
    }

    public Message(String message, String sender, String[] receivers) {
        this.text = message;
        this.receivers = receivers;
        this.sender = sender;
    }

    public Message(String message, String sender, String receivers) {
        this.text = message;
        this.receivers = receivers.split(",");
        this.sender = sender;
    }

    public Message(String message, String sender, String receivers, boolean except_receivers) {
        this.text = message;
        this.receivers = receivers.split(",");
        this.sender = sender;
        this.except_receivers = except_receivers;
    }

    public static Message fromRaw(String msg) {
        Message mes = new Message();
        mes.parseRaw(msg);
        return mes;
    }

    public static Message toAll(String message, String sender){
        String[] array = new String[]{"all"};
        Message mes = new Message(message, sender, array);
        mes.setTime();

        return mes;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public String[] getReceivers() {
        return receivers;
    }

    public void parseRaw(String msg) {
        String[] args = msg.substring(0, msg.indexOf("|")).split("::");
        timestamp = args[1];
        sender = args[2];
        except_receivers = Boolean.parseBoolean(args[3]);
        receivers = Arrays.stream(args[4].split(",")).map(String::trim).toArray(String[]::new);
        text = msg.substring(msg.indexOf("|")+1);
    }

    public String parsePretty(){
        if(toAll()){
            return "%s > %s ".formatted(sender, text);
        }
        else{
            return "%s > %s(%s): %s |%s|".formatted(sender, except_receivers? "!": "", getReceiversString(), text, timestamp);
        }
    }

    public boolean toAll(){
        return receivers[0].equals("all");
    }

    public boolean isExcept_receivers(){
        return except_receivers;
    }

    public boolean containsBannedPhrases(){
        return Server.getBannedPhrases().stream().anyMatch(p -> text.contains(p));
    }

    private String getReceiversString(){
        return String.join(",", receivers);
    }

    @Override
    public String toString() {
        return "msg::%s::%s::%s::%s|%s".formatted(timestamp, sender, except_receivers, String.join(",", receivers), text);
    }

}
