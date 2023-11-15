import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

//listen the information that client send to server
public class listenerUtil extends Thread {
    public BufferedReader reader;
    //client input stream
    public Server.serverStart server;
    //the target server
    public Socket socket;
    //client socket

    private DesSystem d = new DesSystem();

    private String key = "12345678";
    public listenerUtil(BufferedReader r, Server.serverStart s, Socket self) {
        reader = r;
        server = s;
        this.socket = self;
    }
 
    @Override
    public void run() {
        while (true) {
            try {
                String message = reader.readLine();
                //send message
                server.commandIdentifier(message, socket);
            }
            catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}