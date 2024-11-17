import java.io.*;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;

import org.jline.reader.*;
import org.jline.terminal.*;
import org.jline.utils.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;

public class CLIChat {
    private static final Queue<String> messages = new LinkedList<>();
    private static volatile boolean isRunning = true;
    private static Terminal terminal;
    private static LineReader lineReader;
    private static final int MESSAGE_AREA_HEIGHT = 20;

    public static void main(String[] args) throws IOException {
        // Initialize JLine terminal
        terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        LineReaderBuilder builder = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M> ")
                .variable(LineReader.AMBIGUOUS_BINDING, LineReader.ACCEPT_LINE);

        lineReader = builder.build();

        new Thread(()->{
            try{
                while(true){
                    Thread.sleep(3000);
                    addMessage("Hello world");
                }
            }catch (InterruptedException e){return;}
        }).start();

        try {
            while (isRunning) {
                String line = readLine();
                if (line == null || line.equalsIgnoreCase("/quit")) {
                    break;
                }
                if (!line.trim().isEmpty()) {
                    addMessage("You: " + line);
                }
                redrawScreen();
            }
        } finally {
            terminal.close();
        }
    }

    private static String readLine() {
        try {
            return lineReader.readLine("You: ");
        } catch (UserInterruptException e) {
            // Handle Ctrl+C
            return "/quit";
        } catch (EndOfFileException e) {
            // Handle Ctrl+D
            return "/quit";
        }
    }

    private static void addMessage(String message) {
        messages.offer(message);
        while (messages.size() > MESSAGE_AREA_HEIGHT) {
            messages.poll();
        }
    }

    private static void redrawScreen() {
        try {
            terminal.flush();
/*
            terminal.writer().write(InfoCmp.Capability.clear_screen.getSequence());
            terminal.writer().write(InfoCmp.Capability.cursor_home.getSequence());
*/

            // Display messages
            terminal.writer().println("=== Chat Messages ===");
            messages.forEach(msg -> terminal.writer().println(msg));

            // Fill remaining space with empty lines
            int emptyLines = MESSAGE_AREA_HEIGHT - messages.size();
            for (int i = 0; i < emptyLines; i++) {
                terminal.writer().println();
            }

            terminal.writer().println("===================");
            terminal.flush();

            // Force redraw of the current line
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}