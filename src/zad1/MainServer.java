package zad1;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MainServer {
    private ServerSocket serverSocket;
    private ExecutorService pool;
    static ServersList serversList = new ServersList();

    public MainServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(10);

    }

    public void serve() {
        System.out.println("Main Server is Running on port " + serverSocket.getLocalPort());
        serversList.getServers().forEach((languageCode, port) -> {
            new Thread(() -> {
                try {
                    new LanguageServer(port, languageCode).serve();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });


        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, this));
            } catch (IOException e) {
                System.out.println("Accept failed on: " + serverSocket.getLocalPort());
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        int port = 2500;
        try {
            MainServer server = new MainServer(port);
            server.serve();
        } catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            e.printStackTrace();
        }

    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private MainServer server;

        public ClientHandler(Socket socket, MainServer server) {
            this.clientSocket = socket;
            this.server = server;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String inputLine = in.readLine();
                System.out.println("Received: " + inputLine);
                if (inputLine != null) {
                    String[] parts = inputLine.split(",");
                    if (parts.length == 3) {
                        String word = parts[0];
                        String languageCode = parts[1].toUpperCase();
                        int clientPort = Integer.parseInt(parts[2]);
                        PrintWriter clientOut = new PrintWriter(new Socket("localhost", clientPort).getOutputStream(), true);
                            if (serversList.getPport(languageCode) == -1) {
                                if (word.equals("newlanguage")) {
                                    serversList.addServer(languageCode);
                                    new Thread(() -> {
                                        try {
                                            new LanguageServer(serversList.getPport(languageCode), languageCode).serve();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                    clientOut.println("New language server added for " + languageCode);

                                }
                                else {
                                    System.out.println("Error: Language not supported");

                                    clientOut.println("Error: Language not supported");
                                }



                            }
                            else {
                                forwardRequest(serversList.getPport(languageCode), word, clientPort);
                                out.println("Request forwarded to " + languageCode + " server");
                            }

                    } else {
                        out.println("Error: Incorrect request format");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client #" + clientSocket.getPort());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Could not close a socket");
                    e.printStackTrace();
                }
            }
        }

        private void forwardRequest(int languageSocket, String word, int clientPort) throws IOException {
            System.out.println("Forwarding request to language server on port " + languageSocket);
            try (Socket socket = new Socket("localhost", languageSocket);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(word + "," + clientPort);
            }
        }
    }

    static class ServersList {
        static int maxport = 2502;
        private Map<String, Integer> servers =  new HashMap<>();
        ServersList() {
            ReadLanguageServers();
        }
        public void addServer(String languageCode ) {
            int port = maxport++;
            servers.put(languageCode, port);
        }
        public Map<String, Integer> getServers() {
            return servers;
        }
        public int getPport(String languageCode) {
            if (!servers.containsKey(languageCode)) {
                return -1;
            }
            return servers.get(languageCode);
        }
        public void ReadLanguageServers(){

            File[] files = new File("Lang").listFiles((dir, name) -> name.endsWith(".txt"));
            if (files == null) {
                System.out.println("No language files found");
                return;
            }
            for (File file : files) {
                String languageCode = file.getName().substring(0, 2);
                addServer(languageCode);
            }



        }


    }
}
