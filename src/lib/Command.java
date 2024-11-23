package lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command extends Info{

    public Command(){}

    public Command(String text){
        this.text = text;
    }

    @Override
    public String toString(){
        return "cmd::%s|%s".formatted(timestamp, text);
    }

    public static Command fromRaw(String raw){
        Command cmd = new Command();
        cmd.parseRaw(raw);
        return cmd;
    }

    public static Command fromUserInput(String input) throws Exception{
        Pattern pattern = Pattern.compile("^/(\\w+)");
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()){
            Command cmd = new Command();
            cmd.text = matcher.group(1);
            return cmd;
        }
        throw new Exception("Command not found");
    }
}
