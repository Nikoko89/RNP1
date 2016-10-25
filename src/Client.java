import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;


public class Client {
    private static long lastCommand;
    private static BufferedReader userInput;
    private static DataInputStream serverInput;
    private static DataOutputStream clientOutput;
    private static Socket clientSocket;
    private static boolean living;

    public static void main(String[] args) {

        try {
            clientSocket = new Socket("127.0.0.1", 6432);
            clientSocket.setKeepAlive(true);
            living = true;
            // Nachricht vom Benutzer an den Client
            userInput = new BufferedReader(new InputStreamReader(System.in));

            // Nachricht vom Server an den Client
            serverInput = new DataInputStream(clientSocket.getInputStream());


            // Nachricht vom Client an den Server
            clientOutput = new DataOutputStream(clientSocket.getOutputStream());

            while (living) {

                if (System.in.available() > 0) {

                    String eingabe = userInput.readLine() + '\n';
                    clientOutput.writeUTF(eingabe);

                }
                if (serverInput.available() > 0) {
                    String serverOut;
                    serverOut = serverInput.readUTF();
                    System.out.println(serverOut);
                    if (serverOut.equals("OK BYE" + '\n') || serverOut.equals("OK SHUTDOWN" + '\n')) {
                        remove();
                    }
                    if (serverOut.equals("SHUTTINGDOWN" + '\n')){
                        lastCommand = System.currentTimeMillis();
                        timeIsRunning();
                    }
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
            living = false;
        } catch (IOException e) {
            System.err.println("Could not close Client connection");
        }

    }


    public static void timeIsRunning(){
        System.out.println("Ciaoooo");
        try {
        while(System.currentTimeMillis() - lastCommand < 15000){
            if (System.in.available() > 0) {

                String eingabe = userInput.readLine() + '\n';
                clientOutput.writeUTF(eingabe);
                lastCommand = System.currentTimeMillis();
            }

            if (serverInput.available() > 0) {
                String serverOut;
                serverOut = serverInput.readUTF();
                System.out.println(serverOut);
            }

        }

            clientOutput.writeUTF("BYE" + '\n');
        } catch (IOException e) {
            System.err.println("Could not write to Server");
        }
        remove();
    }
}
