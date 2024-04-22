package zad1;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class LanguageServer {
    private ServerSocket serverSocket;
    private String language;
    private Map<String, String> dictionary = new HashMap<>();

    public LanguageServer(int port, String language) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.language = language;
    }

    public void serve() {


        File[] files = new File("Lang").listFiles((dir, name) -> name.startsWith(language));

        if (files == null) {
            System.out.println("No language files found");
            return;
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    dictionary.put(parts[0], parts[1]);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Language Server " + language +"  running on port " + serverSocket.getLocalPort());

        while (true) {
            try
            {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        Respond(clientSocket, dictionary);
                    } catch (IOException e) {
                        System.out.println("Error in language server: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                System.out.println("Error in language server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void Respond(Socket clientSocket, Map<String, String> dictionary) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] parts = inputLine.split(",");
            System.out.println();
            String word = parts[0];
            int clientPort = Integer.parseInt(parts[1]);
            String translation = dictionary.getOrDefault(word, "Translation not found");
            sendBackTranslation(InetAddress.getByName("localhost"), clientPort, translation);
        }
    }

    private void sendBackTranslation(InetAddress clientAddress, int clientPort, String translation) {
        try (Socket socket = new Socket(clientAddress, clientPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(translation);
            System.out.println("Sent translation to client: " + translation);
        } catch (IOException e) {
            System.out.println("Failed to send translation: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
