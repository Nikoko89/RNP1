import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;


public class Client {
    private static long lastCommand;
    private static BufferedReader userInput;
    private static DataInputStream serverInput;
    private static DataOutputStream clientOutput;
    private static Socket clientSocket;

    public static void main(String[] args) {

        try {
            clientSocket = new Socket("127.0.0.1", 6432);
            clientSocket.setKeepAlive(true);
            boolean living = true;
            // Nachricht vom Benutzer an den Client
            userInput = new BufferedReader(new InputStreamReader(System.in));

            // Nachricht vom Server an den Client
            serverInput = new DataInputStream(clientSocket.getInputStream());


            // Nachricht vom Client an den Server
            clientOutput = new DataOutputStream(clientSocket.getOutputStream());

            while (living) {
                System.out.println("Please type in a command: ");
                String eingabe = userInput.readLine() + '\n';
                clientOutput.writeUTF(eingabe);
                lastCommand = System.currentTimeMillis();
                String serverOut;
                serverOut = serverInput.readUTF();
                System.out.println(serverOut);
                if (serverOut.equals("OK BYE" + '\n') || serverOut.equals("OK SHUTDOWN" + '\n')) {
                    living = false;
                    remove();
                }
                if (serverOut.equals("SHUTTINGDOWN" + '\n')){
                    timeIsRunning();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void remove(){
        try {
            serverInput.close();
            userInput.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Could not close Client connection");
        }

    }


    public static void timeIsRunning(){
        System.out.println("HIIIII");
        try {
        while(System.currentTimeMillis() - lastCommand < 3000){
            System.out.println("Please type in a command: ");
            String eingabe = userInput.readLine() + '\n';
            clientOutput.writeUTF(eingabe);
            lastCommand = System.currentTimeMillis();
            String serverOut;
            serverOut = serverInput.readUTF();
            System.out.println(serverOut);
        }

            clientOutput.writeUTF("BYE" + '\n');
        } catch (IOException e) {
            System.err.println("Could not write to Server");
        }
        remove();
    }
}
