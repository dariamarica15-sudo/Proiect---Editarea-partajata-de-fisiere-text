package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClientMain {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.print("Username: ");
            String name = console.readLine();

            out.println("CONNECT " + name);

            List<String> buffer = new ArrayList<>();

            boolean[] editAllowed = new boolean[1];
            editAllowed[0] = false;

            new Thread(new ServerListener(in, buffer, editAllowed)).start();

            boolean isEditing = false;
            String editingFile = null;

            while (true) {

                if (isEditing) {
                    System.out.print("(edit)> ");
                    String line = console.readLine();

                    if (line == null) break;

                    if (line.equalsIgnoreCase("SAVE")) {
                        out.println("SAVE " + editingFile);

                        for (String l : buffer) {
                            out.println(l);
                        }

                        out.println("CONTENT_END");

                        buffer.clear();
                        isEditing = false;
                        editingFile = null;
                        continue;
                    }

                    if (line.equalsIgnoreCase("CANCEL")) {
                        out.println("RENUNTA " + editingFile);

                        buffer.clear();
                        isEditing = false;
                        editingFile = null;
                        continue;
                    }

                    if (line.startsWith("APPENDLINE ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Folosește: APPENDLINE numar text");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Linie inexistentă");
                                continue;
                            }

                            buffer.set(index, buffer.get(index) + " " + parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr invalid");
                        }

                        continue;
                    }

                    if (line.startsWith("APPEND ")) {
                        String text = line.substring(7);
                        buffer.add(text);
                        showBuffer(buffer);
                        continue;
                    }

                    if (line.startsWith("REPLACEWORD ")) {
                        String[] parts = line.split(" ", 4);

                        if (parts.length < 4) {
                            System.out.println("[ERROR] Folosește: REPLACEWORD numar cuvant_vechi cuvant_nou");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Linie inexistentă");
                                continue;
                            }

                            String currentLine = buffer.get(index);
                            String updatedLine = currentLine.replaceFirst(
                                    "\\b" + Pattern.quote(parts[2]) + "\\b",
                                    parts[3]
                            );

                            buffer.set(index, updatedLine);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr de linie invalid");
                        }

                        continue;
                    }

                    if (line.startsWith("REPLACE ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Folosește: REPLACE numar text");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Linie inexistentă");
                                continue;
                            }

                            buffer.set(index, parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr de linie invalid");
                        }

                        continue;
                    }

                    if (line.startsWith("DELETEWORD ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Folosește: DELETEWORD numar cuvant");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Linie inexistentă");
                                continue;
                            }

                            String currentLine = buffer.get(index);
                            String updatedLine = currentLine.replaceFirst(
                                    "\\b" + Pattern.quote(parts[2]) + "\\b", "");

                            updatedLine = updatedLine.replaceAll("\\s+", " ").trim();

                            buffer.set(index, updatedLine);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr de linie invalid");
                        }

                        continue;
                    }

                    if (line.startsWith("DELETE ")) {
                        String[] parts = line.split(" ", 2);

                        if (parts.length < 2) {
                            System.out.println("[ERROR] Folosește: DELETE numar");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index >= buffer.size()) {
                                System.out.println("[ERROR] Linie inexistentă");
                                continue;
                            }

                            buffer.remove(index);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr de linie invalid");
                        }

                        continue;
                    }

                    if (line.startsWith("INSERT ")) {
                        String[] parts = line.split(" ", 3);

                        if (parts.length < 3) {
                            System.out.println("[ERROR] Folosește: INSERT numar text");
                            continue;
                        }

                        try {
                            int index = Integer.parseInt(parts[1]) - 1;

                            if (index < 0 || index > buffer.size()) {
                                System.out.println("[ERROR] Poziție invalidă");
                                continue;
                            }

                            buffer.add(index, parts[2]);
                            showBuffer(buffer);

                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Număr de linie invalid");
                        }

                        continue;
                    }

                    System.out.println("[ERROR] Comandă invalidă în edit mode");
                    System.out.println("Comenzi: APPEND text, APPENDLINE n text, REPLACE n text, REPLACEWORD n vechi nou, DELETE n, DELETEWORD n cuvant, INSERT n text, SAVE, CANCEL");
                    continue;
                }

                System.out.print("> ");
                String cmd = console.readLine();

                if (cmd == null) break;

                if (cmd.startsWith("EDIT ")) {

                    editAllowed[0] = false;

                    out.println(cmd);

                    Thread.sleep(200);

                    if (!editAllowed[0]) {
                        continue;
                    }

                    editingFile = cmd.split(" ", 2)[1];
                    isEditing = true;

                    System.out.println("[SERVER] Editing mode pentru " + editingFile);
                    System.out.println("Comenzi: APPEND text, APPENDLINE n text, REPLACE n text, REPLACEWORD n vechi nou, DELETE n, DELETEWORD n cuvant, INSERT n text, SAVE, CANCEL");
                    showBuffer(buffer);

                    continue;
                }

                out.println(cmd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showBuffer(List<String> buffer) {
        if (buffer.isEmpty()) {
            System.out.println("[SERVER] Fișier gol");
            return;
        }

        for (int i = 0; i < buffer.size(); i++) {
            System.out.println((i + 1) + ": " + buffer.get(i));
        }
    }
}