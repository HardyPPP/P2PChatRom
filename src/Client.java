import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    public static void main(String[] args) {
        new ClienStart();
    }

    public static class ClienStart extends JFrame {
        public String userName = "";
        //user name that will be announced to all users
        //private Map<String, Socket> clientSockets = new HashMap<>();
        private Socket socket;
        //socket to connect the server
        Socket clientSocket;
        //socket of P2P connection
        private ServerSocket serverSocket;
        //sever socket that used to create p2p connection with other client
        private PrintWriter outputStream;
        //client output stream
        private BufferedReader inputStream;
        //client input stream
        private JTextArea textArea = new JTextArea();
        private JScrollPane panel = new JScrollPane(textArea);
        private JTextField inputField = new JTextField();
        private JLabel nameLabel = new JLabel("ID:", SwingConstants.CENTER);
        private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
        private JTextField nameTextField = new JTextField();
        private JLabel ipLabel = new JLabel("Host:", SwingConstants.CENTER);
        private JTextField ipTextfield = new JTextField("127.0.0.1");
        private JLabel portLabel = new JLabel("Port:", SwingConstants.CENTER);
        private JTextField portTextField = new JTextField();
        private JButton Connect = new JButton("Connect");

        public ClienStart() {
            setTitle("socketClient");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setBounds(300, 50, 1000, 700);
            //initialize the window and listener thread
            init();
            start();
            setVisible(true);
        }

        private void start() {
            //start the two main thread
            //first check the ip, port number and name
            Connect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nameTextField.getText().length() > 10) {
                        JOptionPane.showMessageDialog(ClienStart.this, "name too long");
                        return;
                    }
                    if (ipTextfield.getText().length() > 15) {
                        JOptionPane.showMessageDialog(ClienStart.this, "illegal IP");
                        return;
                    }
                    if (portTextField.getText().length() > 5) {
                        JOptionPane.showMessageDialog(ClienStart.this, "illegal port number");
                        return;
                    }
                    // start the connection thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectToServer(nameTextField.getText(), ipTextfield.getText(), portTextField.getText());
                        }
                    }).start();
                    // start the thread that wait for p2p connection of another client
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getP2PConnection(portTextField.getText());
                        }
                    }).start();
                    Connect.setEnabled(false);
                }
            });
        }

        private void init() {
            //initialize the window
            // ID
            nameLabel.setBounds(600, 50, 100, 40);
            nameLabel.setFont(new Font(null, Font.BOLD, 30));
            add(nameLabel);
            nameTextField.setBounds(710, 50, 130, 50);
            nameTextField.setFont(new Font(null, Font.BOLD, 27));
            add(nameTextField);
            //IP
            ipLabel.setBounds(600, 150, 100, 40);
            ipLabel.setFont(new Font(null, Font.BOLD, 28));
            add(ipLabel);
            ipTextfield.setBounds(710, 150, 230, 50);
            ipTextfield.setFont(new Font(null, Font.BOLD, 28));
            add(ipTextfield);
            //port
            portLabel.setBounds(600, 250, 100, 40);
            portLabel.setFont(new Font(null, Font.BOLD, 28));
            add(portLabel);
            portTextField.setBounds(710, 250, 230, 50);
            portTextField.setFont(new Font(null, Font.BOLD, 28));
            add(portTextField);
            Connect.setBounds(675, 350, 200, 40);
            Connect.setFont(new Font(null, Font.BOLD, 30));
            add(Connect);
            statusLabel.setBounds(600, 450, 350, 40);
            statusLabel.setFont(new Font(null, Font.BOLD, 28));
            add(statusLabel);
            add(new JLabel("                                                                            "));
            textArea.setEditable(false);
            textArea.setFont(new Font(null, Font.BOLD, 28));
            panel.setBounds(30, 30, 550, 550);
            getContentPane().add(panel, "South");
            inputField.setFont(new Font(null, Font.BOLD, 28));
            //press enter tho send message
            inputField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (socket == null) {
                        //if connection failed
                        Connect.setEnabled(true);
                    } else {
                        // send the message content and sender's name to server
                        outputStream.println(inputField.getText() + "_" + userName);
                        inputField.setText("");
                    }

                }
            });
            getContentPane().add(inputField, "South");
        }

        @Override
        public void dispose() {
            //Releases all of the native screen resources used by this Window and close all the connection of the clients
            if (socket != null) {
                try {
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            super.dispose();
        }

        public void getP2PConnection(String port) {
            // thread that listen to p2p connection of another client
            try {
                serverSocket = new ServerSocket(Integer.parseInt(port));
                while (true) {
                    System.out.println("waiting connect");
                    // get the socket of the connected client
                    clientSocket = serverSocket.accept();
                    System.out.println("connected");
                    try {
                        BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        new Thread() {
                            @Override
                            public void run() {
                                //listen to the message sent by the client
                                while (true) {
                                    try {
                                        String message = inputStream.readLine();
                                        if (message!=null) {
                                            textArea.append(message + "\n");
                                        }
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(ClienStart.this, "error occur" + ex.getMessage());
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "error occur" + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //connect to the server
        public void connectToServer(String name, String ip, String port) {
            this.userName = name;
            try {
                socket = new Socket(ip, 7777);
                outputStream = new PrintWriter(socket.getOutputStream(), true);
                // send your basic information to the server
                outputStream.println("{new_" + name + "_" + ip + "_" + port);
                statusLabel.setText("Connected to the server");
            } catch (UnknownHostException ex1) {
                Connect.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Connection Error" + ex1.getMessage());
                statusLabel.setText("Connection Error");
                ex1.printStackTrace();
            } catch (IOException ex) {
                Connect.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Connection Error" + ex.getMessage());
                statusLabel.setText("Connection Error");
                ex.printStackTrace();
            }

            if (socket != null) {
                try {
                    inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    new Thread() {
                        @Override
                        public void run() {
                            //listen to the message sent by server
                            while (true) {
                                try {
                                    String message = inputStream.readLine();
                                    if (message != null && !message.startsWith("USER")) {
                                        textArea.append(message + "\n");
                                    }
                                    if (message != null) {
                                        if (message.equals("{STOP1}")) {
                                            // you close the connection between you and server
                                            textArea.append("You've stopped the connection\n" + "Client will be close in 5s");
                                            try {
                                                Thread.sleep(5000); //Wait for 5 seconds
                                            } catch (InterruptedException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                            // close the window
                                            System.exit(EXIT_ON_CLOSE);
                                        } else if (message.startsWith("USER")) {
                                            // you get the target user's ip and port that you want to send message to
                                            String[] s = message.split("_");

                                            Socket clientSocket = new Socket(s[1], Integer.parseInt(s[2]));
                                            // send the P2P message
                                            PrintWriter OutputStream = new PrintWriter(clientSocket.getOutputStream(), true);
                                            OutputStream.println(userName + "-> YOU: " + s[3]);
                                            textArea.append("ME -> "+ s[4] + ": " + s[3] + "\n");
                                           // clientSocket.close();
                                        } else if (message.equals("{STOP2}")) {
                                            // you've been kicked
                                            textArea.append("You've kicked\n" + "Client will be close in 5s");
                                            try {
                                                Thread.sleep(5000); //Wait for 5 seconds
                                            } catch (InterruptedException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                            // close the window
                                            System.exit(EXIT_ON_CLOSE);
                                        } else if (message.equals("{STOP3}")) {
                                            // the server is down
                                            textArea.append("Server Closed\n" + "Client will be close in 5s");
                                            try {
                                                Thread.sleep(5000); //Wait for 5 seconds
                                            } catch (InterruptedException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                            // close the window
                                            System.exit(EXIT_ON_CLOSE);
                                        } else if (message.equals("Unknown Command")) {
                                            // you enter an unknown command
                                            textArea.append("please check");
                                        }
                                    }
                                } catch (IOException ex) {
                                    PrintWriter OutputStream = null;
                                    try {
                                        OutputStream = new PrintWriter(socket.getOutputStream(), true);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    OutputStream.println("{STOP}");
                                    // tell the server that error occur and you close the connection
                                    JOptionPane.showMessageDialog(ClienStart.this, "Client Closed" + ex.getMessage());
                                    try {
                                        Thread.sleep(500); //Wait for 0.5 seconds
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    System.exit(EXIT_ON_CLOSE);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "InputStream Error" + ex.getMessage());

                    ex.printStackTrace();
                }
            }
        }
    }
}



