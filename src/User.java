import java.io.Serializable;
import java.net.Socket;

public class User implements Serializable {
    // store the name, ip , port number of a user
    private String name;
    private Socket socket;

   public String port;
    public String ip;
    public User(String name,String port, String ip){
        this.name = name;
        this.port = port;
        this.ip = ip;
    }
}
