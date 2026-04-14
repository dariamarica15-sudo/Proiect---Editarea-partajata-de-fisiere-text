package client;

import java.io.BufferedReader;
import java.util.List;

public class ServerListener implements Runnable {

    private BufferedReader in;
    private List<String> buffer;
    private boolean[] editAllowed;
    
    public ServerListener(BufferedReader in, List<String> buffer, boolean[] editAllowed) {
        this.in = in;
        this.buffer = buffer;
        this.editAllowed = editAllowed;
    }

    @Override
    public void run() {
        try {
            String line;
            boolean readingContent = false;
            boolean editContentMode = false;

            while ((line = in.readLine()) != null) {

                if (line.equals("CONTENT_BEGIN")) {
                    readingContent = true;
                    buffer.clear();
                    continue;
                }

                if (line.equals("CONTENT_END")) {
                    readingContent = false;
                    editContentMode = false;
                    continue;
                }

                if (readingContent) {
                    buffer.add(line);

                    if (!editContentMode) {
                        System.out.println(line);
                    }

                    continue;
                }

                if (line.startsWith("ERROR: fisierul este deja editat")) {
                    editAllowed[0] = false;
                    System.out.println("[ERROR] " + line.substring(6).trim());
                    continue;
                }
                else if (line.startsWith("SUCCESS: editare inceputa")) {
                    editAllowed[0] = true;
                    editContentMode = true;
                    System.out.println("[SERVER] editare permisa");
                    continue;
                } 
                	
                	
                else if (line.startsWith("ERROR:")) {
                    System.out.println("[ERROR] " + line.substring(6).trim());
                    continue;
                }
                else if (line.startsWith("SUCCESS:")) {
                    System.out.println("[SERVER] " + line.substring(8).trim());
                    continue;
                }
                else if (line.startsWith("INFO:")) {
                    System.out.println("[SERVER] " + line.substring(5).trim());
                    continue;
                } 
                else {
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}