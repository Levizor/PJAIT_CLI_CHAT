package lib;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

public class ConfigParser {
    Path configFile;
    int port = 2222;
    String name = "ChatServer";
    Set<String> bannedPhrases = new HashSet<>();

    static int lineNum = 0;
    static boolean parsingPhrases = false;
    HashMap<String, String> map;

    public ConfigParser(Path configFile) {
        this.configFile = configFile;
    }

    public void parse() throws ParseException, NoSuchFileException, IOException {
        map = new HashMap<>();
        StringBuilder bannedPhrasesStr = new StringBuilder();
        Stream<String> lines = Files.lines(configFile);
            lines
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(line -> !line.startsWith("#"))
                    .forEach(l -> {
                                lineNum++;
                                String[] arr = Arrays.stream(l.split("=")).map(String::trim).toArray(String[]::new);
                                if(arr[0].equals("bannedphrases")) {
                                    parsingPhrases = true;
                                    if(arr.length<=1 || !arr[1].startsWith("(")){
                                        throw new RuntimeException(new ParseException("Start arrays with (", lineNum));
                                    }
                                    if(!l.endsWith(")")) bannedPhrasesStr.append(arr[1].substring(1));
                                    else{
                                        bannedPhrasesStr.append(arr[1].substring(1, arr[1].length() - 1));
                                    }
                                } else if (parsingPhrases) {
                                    if(l.endsWith(")")){
                                        parsingPhrases = false;
                                    }else{
                                        bannedPhrasesStr.append(" ").append(l);
                                    }
                                }else if (arr.length == 2) {
                                    map.put(arr[0], arr[1]);
                                } else {
                                    throw new RuntimeException(new ParseException("Wrong here: %s".formatted(l), lineNum));
                                }

                            }
                    );

            parseName();
            parsePort();
            parseBannedPhrases(bannedPhrasesStr.toString());



    }

    public void parsePort() {
        if(map.containsKey("port")){
            port = Integer.parseInt(map.get("port"));
        }
    }

    public void parseName(){
        if(map.containsKey("name")){
            name = map.get("name");
        }
    }

    public String getValue(String key){
        return map.getOrDefault(key, null);
    }

    public void parseBannedPhrases(String s){
        String[] arr = s.trim().split("\\s+");
        bannedPhrases.addAll(Arrays.asList(arr));
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public Set<String> getBannedPhrases() {
        if (bannedPhrases == null) {
            return new HashSet<>();
        }
        return bannedPhrases;
    }

    @Override
    public String toString() {
        return "Port: " + port + " Name: " + name + " BannedPhrases: " + bannedPhrases;
    }
}
