import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    public static void main(String[] args) {
        new serverStart();
    }

    public static class serverStart extends JFrame {
        private ServerSocket serverSocket;// server socket
        private Map<Socket, listenerUtil> users = new HashMap<>();
        //match all users' socket and listener thread
        private Map<Socket, PrintWriter> inputStream = new HashMap<>();
        //match all users' socket and input stream
        private Map<String, List<String>> userCommands = new HashMap<>();
        //record all users' command
        private Map<String, Socket> userSockets = new HashMap<>();
        //match all users' name and socket
        private Map<String, User> userP = new HashMap<>();
        //match all users' name and their information like ip, port
        private List<Socket> sockets = new ArrayList<>();
        //all sockets
        private JTextArea textArea = new JTextArea();
        //show the message that client input
        private JScrollPane panel = new JScrollPane(textArea);
        private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
        // record the status of server
        public serverStart() {
            setTitle("HardyPrime Server");
            //my server name
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setBounds(50, 20, 800, 800);
            //initialize window
            init();
            try {
                serverSocket = new ServerSocket(7777);
                //create server socket
                statusLabel.setText("Server is running, "+ userSockets.size() + " client is online" );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
            listenerThread();
            //start listen all client input
            setVisible(true);
        }

        //initialize window
        private void init() {
            statusLabel.setBounds(0, 0, 550, 40);
            statusLabel.setFont(new Font(null, Font.BOLD, 28));
            add(statusLabel);
            add(new JLabel("                                                                            "));
            textArea.setFont(new Font(null, Font.BOLD, 28));
            textArea.setEditable(false);
            panel.setBounds(0, 50, 800, 750);
            getContentPane().add(panel, "South");
            getContentPane().add(new JLabel("                                                                            "), "South");
        }

        //listen client input
        private void listenerThread() {
            new Thread(() -> {
                try {
                    while (true) {
                        Socket socket = serverSocket.accept();
                        //get client socket
                        if (!users.containsKey(socket)) {
                            //max number of clients
                            if (users.size() < 10) {
                                //init the message listening thread
                                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                listenerUtil listener = new listenerUtil(reader, serverStart.this, socket);
                                listener.start();
                                //add user information, like name,socket,inputstream
                                users.put(socket, listener);
                                inputStream.put(socket, new PrintWriter(socket.getOutputStream(), true));
                                sockets.add(socket);
                                textArea.append("Client connected, IP: " + socket.getInetAddress() + "\n");
                                statusLabel.setText("Server is running, "+ users.size() + " online clients " );
                                // broadcast the new client information

                            } else {
                                //room is full
                                System.out.println("room is full, connection rejected");
                                textArea.append("room is full, connection rejected");
                                socket.close();
                            }
                        }
                    }
                } catch (IOException ex) {
                    // error occurs, shut down server
                    JOptionPane.showMessageDialog(serverStart.this, "Server down " + ex.getMessage());
                    try {
                        Thread.sleep(500); //Wait for 0.5 seconds
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //shut down
                    System.exit(EXIT_ON_CLOSE);

                    ex.printStackTrace();
                    // tell all the client that server is down and disconnect them
                    for (Socket s : inputStream.keySet()) {
                        inputStream.get(s).println("{STOP3}");
                        inputStream.remove(s);
                        sockets.remove(s);
                        users.remove(s);
                        try {
                            s.close();
                        } catch (IOException ex2) {
                            ex2.printStackTrace();
                        }
                    }
                    dispose();

                }
            }).start();
        }

        //message listening thread used in listenerThread()
        public void commandIdentifier(String message, Socket s) throws IOException {
            if (message.trim().startsWith("{")) {
                // client input a command
                String m1 = message.trim().replace("{", "").replace("{", "")
                        .replace("{", "").replaceAll("}", "");
                // split the command to get the required information like the command type, the sender, the content, the target receiver
                if (m1.startsWith("BROADCAST")) {
                    // broadcast
                    String[] m2 = m1.split("_");
                    textArea.append("\n" + m2[2] + ": " + m2[1]);
                    System.out.println(m2[2]);
                    // broadcast the content to all client
                    for (Socket s_2 : inputStream.keySet()) {
                        inputStream.get(s_2).println(m2[2] + ": " + m2[1] + "\n");
                    }
                    userCommands.get(m2[2]).add("{BROADCAST"+"{"+m2[1]+"}}");
                } else if (m1.startsWith("MESSAGE")) {
                    String[] m2 = m1.split("_");
                    textArea.append("\n" + m2[3] + " -> " + m2[1] + ": " + m2[2]);
                    // find the target receiver's port and ip
                    if (userP.get(m2[1])!=null) {
                        String ss = "USER:_" + userP.get(m2[1]).port + "_" + userP.get(m2[1]).ip + "_" + m2[2] + "_" + m2[1];
                        inputStream.get(s).println(ss);
                        userCommands.get(m2[3]).add("{MESSAGE" + "_" + m2[1] + "_{" + m2[2] + "}}");
                    }else{
                        inputStream.get(s).println("No such user found");
                    }
                } else if (m1.startsWith("new")) {
                    // add a new client to the maps
                    String[] m2 = m1.split("_");
                    userSockets.put(m2[1], s);
                    User u = new User(m2[1], m2[2], m2[3]);
                    userP.put(m2[1], u);
                    System.out.println(userSockets.get(m2[1]) + " " + m2[1]);
                    for (Socket ss : inputStream.keySet()) {
                        inputStream.get(ss).println("New client connected, ID: " + userSockets.keySet().toArray()[userSockets.size()-1]);
                    }
                    userCommands.put(m2[1],new ArrayList<String>());
                } else if (m1.startsWith("STOP")) {
                    String[] m2 = m1.split("_");
                    // remove the user
                    userCommands.get(m2[1]).add("{STOP}");
                    users.remove(s);
                    sockets.remove(s);
                    // tell the user that remove successfully
                    inputStream.get(s).println("{STOP1}");
                    inputStream.remove(s);
                    userSockets.remove(m2[1]);
                    textArea.append("Client: ID = " + m2[1] + " is offline"+ "\n");
                    if (users.size() != 0) {
                        for (Socket s_2 : inputStream.keySet()) {
                            inputStream.get(s_2).println("Client: ID = " +m2[1] + " is offline");
                        }
                    }
                    statusLabel.setText("Server is running, "+ users.size() + " online clients " );
                } else if (m1.startsWith("LIST")) {
                    String[] m2 = m1.split("_");
                    // give the user list to sender
                    //inputStream.get(userSockets.get(m2[1])).println("Current Users: " + "\n");
                    StringBuilder IDs = new StringBuilder("Current Users: " + "\n");
                    for (String name : userSockets.keySet()) {
                        IDs.append(name).append("\n");
                    }
                    inputStream.get(userSockets.get(m2[1])).println(IDs);
                    userCommands.get(m2[1]).add("{LIST}");
                } else if (m1.startsWith("KICK")) {
                    String[] m2 = m1.split("_");
                    System.out.println(m2[1]);
                    if (userSockets.get(m2[1].trim())!=null) {
                        // remove the user
                        inputStream.get(userSockets.get(m2[1].trim())).println("{STOP2}");
                        users.remove(userSockets.get(m2[1]));
                        sockets.remove(userSockets.get(m2[1]));
                        userSockets.remove(m2[1]);
                        // tell all the users that this guy is kicked
                        textArea.append("Client: ID = " + m2[1] + " is kicked" + "\n");
                        if (users.size() != 0) {
                            for (Socket s_2 : inputStream.keySet()) {
                                inputStream.get(s_2).println("Client: ID = " + m2[1] + " is kicked");
                            }
                        }
                        userCommands.get(m2[2]).add("{KICK" + "_" + m2[1] + "}");
                        statusLabel.setText("Server is running, " + users.size() + " online clients ");
                        inputStream.remove(userSockets.get(m2[1]));
                    }else {
                        inputStream.get(userSockets.get(m2[2].trim())).println("No such user found");
                    }
                }else if (m1.startsWith("STATS")) {
                    String[] m2 = m1.split("_");
                    if (userCommands.get(m2[1])!=null) {
                        StringBuilder commands = new StringBuilder("Commands used by " + m2[1] + ": " + "\n");
                        for (String command : userCommands.get(m2[1])) {
                            commands.append(command).append("\n");
                        }
                        inputStream.get(userSockets.get(m2[2])).println(commands);
                        userCommands.get(m2[2]).add("{STATS" + "_" + m2[1] + "}");
                    }else{
                        inputStream.get(userSockets.get(m2[2].trim())).println("No such user found");
                    }
                }
                else {
                    inputStream.get(s).println("Unknown Command" + "\n");
                }
            }else {
                inputStream.get(s).println("Unknown Command" + "\n");
            }

        }

        @Override
        public void dispose() {
            //Releases all of the native screen resources used by this Window and close all the connection of the clients
            try {
                serverSocket.close();
                for (Socket s : sockets) {
                    users.remove(s);
                    inputStream.remove(s);
                    sockets.remove(s);
                    s.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            super.dispose();
        }
    }


}