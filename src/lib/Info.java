package lib;

import java.util.Random;

public class Info extends Sendable{
    String text;

    public Info() {
    }

    public Info(String text) {
        this.text = text;
    }

    public static Info fromRaw(String raw){
        Info info = new Info();
        info.parseRaw(raw);
        return info;
    }

    public void parseRaw(String raw){
        String[] args = raw.substring(0, raw.indexOf("|")).split("::");
        timestamp = args[1];
        text = raw.substring(raw.indexOf("|")+1);
    }

    public String parsePretty(){
        return "[%s]: %s".formatted(timestamp, text);
    }

    public String getText() {
        return text;
    }

    public boolean ok(){
        return text.equals("ok");
    }

    @Override
    public String toString(){
        return "info::%s|%s".formatted(timestamp, text);
    }



}
