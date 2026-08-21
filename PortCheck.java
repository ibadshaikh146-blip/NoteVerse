import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PortCheck {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 3307;
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            System.out.println("TCP:OPEN");
        } catch (IOException e) {
            System.out.println("TCP:CLOSED - " + e.getMessage());
            System.exit(2);
        }
    }
}
