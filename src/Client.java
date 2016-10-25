import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;


public class Client {
    private static long lastCommand;

    public static void main(String[] args) {

        try {
            Socket clientSocket = new Socket("127.0.0.1", 6432);
            clientSocket.setKeepAlive(true);
            boolean living = true;
            // Nachricht vom Benutzer an den Client
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Nachricht vom Server an den Client
            DataInputStream serverInput = new DataInputStream(clientSocket.getInputStream());


            // Nachricht vom Client an den Server
            DataOutputStream clientOutput = new DataOutputStream(clientSocket.getOutputStream());

            while (living) {
                System.out.println("Please type in a command: ");
                String eingabe = userInput.readLine() + '\n';
                clientOutput.writeUTF(eingabe);
                lastCommand = System.currentTimeMillis();
                String serverOut;
                serverOut = serverInput.readUTF();
                System.out.println(serverOut);
                if (serverOut.equals("OK BYE")) {
                    living = false;
                    serverInput.close();
                    userInput.close();
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void timeIsRunning(){

    }
}
