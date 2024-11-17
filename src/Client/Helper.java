package Client;

public class Helper {

    public static int getFirstWordEndIndex(String s){
        int space = s.indexOf(" ");
        if (space != -1){
            return space;
        }
        return s.length();
    }

}
