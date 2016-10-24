
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int MAX_CLIENTS = 3;
    private static ServerSocket welcomeSocket = null;
    private static List<ServerHelper> listenerSockets;

    public Server(int port) {
    }

    public void start() {
        try {
            welcomeSocket = new ServerSocket(6432, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean serverAlive = true;
        boolean listenForClients = true;
        while (serverAlive) {
            if (listenerSockets.size() < MAX_CLIENTS) {
                try {
                    Socket clientS = welcomeSocket.accept();
                    ServerHelper clientListener;
                    clientListener = new ServerHelper(clientS);
                    clientListener.start();
                    listenerSockets.add(clientListener);

                } catch (IOException e) {
                    System.err.println("Could not create Socket to listen for client");
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 6432;
        listenerSockets = new ArrayList<ServerHelper>();
        Server server = new Server(port);
        server.start();
    }

    public void remove() {
        System.out.println(listenerSockets.size());
        for (int i = 0; i < listenerSockets.size(); i++) {
            if (listenerSockets.get(i).alive == false) {
                listenerSockets.remove(i);
                System.out.println("I removed one client");
                System.out.println("Clients left: " + listenerSockets.size());
            }
        }
    }

    public class ServerHelper extends Thread {
        private Socket socketForClient = null;
        private boolean alive = true;

        public ServerHelper(Socket socket) {
            super("ServerHelper");
            socketForClient = socket;
        }

        public void run() {
            try {
                socketForClient.setKeepAlive(true);
            } catch (SocketException e) {
                System.err.println("Could not set to keep the socket alive");
            }

            //BufferedReader serverInput = null;
            try {
                //Nachricht vom Client an den Server
                //serverInput = new BufferedReader(new InputStreamReader(socketForClient.getInputStream()));
                DataInputStream dos = null;
                dos = new DataInputStream(socketForClient.getInputStream());
                // Nachricht vom Server an den Client
                DataOutputStream clientOutput = new DataOutputStream(socketForClient.getOutputStream());
                String inputLine;
                System.out.println("Client connected to Server");
                while (alive) {
                    //inputLine = serverInput.readLine();
                    try {
                        inputLine = dos.readUTF();
                        System.out.println(inputLine + inputLine.length());
                        byte[] bytes = "Côte d'Ivoire".getBytes("UTF-8");
                        if (inputLine.equals("bye" + '\n')) {
                            System.out.println("bye OK");
                            socketForClient.setKeepAlive(false);
                            alive = false;
                            remove();
                            clientOutput.close();
                            dos.close();
                            socketForClient.close();
                            break;
                        }
                    } catch (EOFException e) {
                        System.err.println("Client disconnected itself");
                        socketForClient.setKeepAlive(false);
                        alive = false;
                        remove();
                        clientOutput.close();
                        dos.close();
                        socketForClient.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
