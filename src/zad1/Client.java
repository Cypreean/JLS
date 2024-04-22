package zad1;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Client {
    private String language;
    private int port;
    private int ownPort;
    private Socket socket;
    private ServerSocket serverSocket;

    private JFrame frame;
    private JTextField wordField;

    private JTextField newlanguageField;
    private JTextField ownPortField;
    private JTextField languageField;
    private JTextArea responseArea;

    public Client(int port) {
        this.port = port;
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Translation Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel wordLabel = new JLabel("Word:");
        wordField = new JTextField(10);
        JLabel ownPortLabel = new JLabel("Own Port:");
        ownPortField = new JTextField(5);
        JLabel languageLabel = new JLabel("Language:");
        languageField = new JTextField(2);
        JButton sendButton = new JButton("Translate");
        JLabel newlanguageLabel = new JLabel("New Language:");
        newlanguageField = new JTextField(2);
        JButton sendButton2 = new JButton("Create New Language");


        panel.add(wordLabel);
        panel.add(wordField);
        panel.add(ownPortLabel);
        panel.add(ownPortField);
        panel.add(languageLabel);
        panel.add(languageField);
        panel.add(sendButton);
        panel.add(newlanguageLabel);
        panel.add(newlanguageField);
        panel.add(sendButton2);


        responseArea = new JTextArea(10, 30);
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        sendButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ownPort = Integer.parseInt(ownPortField.getText());
                } catch (NumberFormatException ex) {
                    responseArea.append("Invalid port number: " + ownPortField.getText() + "\n");
                    return;
                }
                language = newlanguageField.getText().toUpperCase();
                connect("localhost");
                sendRequest("newlanguage");

                receiveResponse();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ownPort = Integer.parseInt(ownPortField.getText());
                } catch (NumberFormatException ex) {
                    responseArea.append("Invalid port number: " + ownPortField.getText() + "\n");
                    return;
                }
                language = languageField.getText().toUpperCase();
                connect("localhost");
                sendRequest(wordField.getText());

                receiveResponse();
            }
        });

        frame.setVisible(true);
    }

    public void connect(String serverAddress) {
        try {
            socket = new Socket(serverAddress, port);

            responseArea.append("Connected to server at " + serverAddress + " on port " + port + "\n");
        } catch (IOException e) {
            responseArea.append("Failed to connect to server: " + e.getMessage() + "\n");
        }
    }

    public void sendRequest(String polishWord) {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String request = polishWord + "," + language + "," + ownPort;
            out.println(request);
            responseArea.append("Request sent: " + request + "\n");
        } catch (IOException e) {
            responseArea.append("Error sending request: " + e.getMessage() + "\n");
        }
    }

    public void receiveResponse() {
        try {
            serverSocket = new ServerSocket(ownPort);
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = in.readLine();
            responseArea.append("Received response: " + response + "\n");
        } catch (IOException e) {
            responseArea.append("Error receiving response: " + e.getMessage() + "\n");
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                responseArea.append("Error closing server: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        new Client(2500);
    }
}
