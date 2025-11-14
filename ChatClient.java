import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageInput;
    private JButton sendBtn;
    private JButton connectBtn;
    private JTextField usernameInput;
    private JLabel statusLabel;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean connected = false;

    public ChatClient() {
        setTitle("Java Chat Client");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Top panel - Connection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.setBackground(new Color(102, 126, 234));
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        
        usernameInput = new JTextField(12);
        usernameInput.setText("User1");
        
        connectBtn = new JButton("Connect");
        connectBtn.setBackground(new Color(118, 75, 162));
        connectBtn.setForeground(Color.WHITE);
        connectBtn.setFocusPainted(false);
        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        topPanel.add(userLabel);
        topPanel.add(usernameInput);
        topPanel.add(connectBtn);
        topPanel.add(statusLabel);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 13));
        chatArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Bottom panel - Message input
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        messageInput = new JTextField();
        messageInput.setFont(new Font("Arial", Font.PLAIN, 13));
        messageInput.setEnabled(false);
        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        sendBtn = new JButton("Send");
        sendBtn.setBackground(new Color(102, 126, 234));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setEnabled(false);
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        bottomPanel.add(messageInput, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void connectToServer() {
        username = usernameInput.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            socket = new Socket("localhost", 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username to server
            out.println(username);
            
            connected = true;
            statusLabel.setText("Connected as: " + username);
            statusLabel.setForeground(new Color(0, 150, 0));
            
            connectBtn.setEnabled(false);
            usernameInput.setEnabled(false);
            messageInput.setEnabled(true);
            sendBtn.setEnabled(true);
            
            chatArea.append("*** Connected to server ***\n");
            
            // Start listening for messages
            new Thread(new MessageListener()).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server. Make sure it's running on localhost:8080", "Connection Error", JOptionPane.ERROR_MESSAGE);
            connected = false;
        }
    }

    private void sendMessage() {
        if (!connected) return;
        
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;
        
        out.println(message);
        chatArea.append("You: " + message + "\n");
        messageInput.setText("");
    }

    private class MessageListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    chatArea.append(message + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                }
            } catch (IOException e) {
                if (connected) {
                    chatArea.append("*** Disconnected from server ***\n");
                    connected = false;
                    connectBtn.setEnabled(true);
                    usernameInput.setEnabled(true);
                    messageInput.setEnabled(false);
                    sendBtn.setEnabled(false);
                    statusLabel.setText("Disconnected");
                    statusLabel.setForeground(Color.RED);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}
