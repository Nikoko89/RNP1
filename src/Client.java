import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;


public class Client {

    public static void main(String[] args) {

        try {
            Socket clientSocket = new Socket("127.0.0.1", 6432);
            clientSocket.setKeepAlive(true);
            boolean living = true;
            while (living) {
                // Nachricht vom Benutzer an den Client
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

                // Nachricht vom Server an den Client
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Nachricht vom Client an den Server
                DataOutputStream clientOutput = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("Bitte Eingabe tätigen: ");
                String eingabe = userInput.readLine()  + '\n';

                clientOutput.writeUTF(eingabe);
                //clientOutput.writeBytes(eingabe);
                if (eingabe.equals("bye" + '\n')) {

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
}
