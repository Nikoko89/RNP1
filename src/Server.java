
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

            try {
                //Nachricht vom Client an den Server
                DataInputStream clientInput = null;
                clientInput = new DataInputStream(socketForClient.getInputStream());
                // Nachricht vom Server an den Client
                DataOutputStream clientOutput = new DataOutputStream(socketForClient.getOutputStream());
                String inputLine;
                System.out.println("Client connected to Server");
                while (alive) {
                    try {
                        inputLine = clientInput.readUTF();
                        String[] inputArray = inputLine.split(" ");
                        if (inputArray.length == 1) {
                            inputArray[0] = inputArray[0].replaceAll("\\n", "");
                        }

                        switch (inputArray[0]) {
                            case "LOWERCASE":
                                if (inputLine.length() > 256) {
                                    System.out.println("ERROR LOWERCASE commmand detected, but the given String is too long");
                                } else if (inputArray.length == 1) {
                                    System.out.println("ERROR LOWERCASE command detected, but there is a Missing Parameter");
                                } else {
                                    String parameter = inputLine.replaceAll("LOWERCASE ", "");
                                    System.out.println("OK " + parameter.replaceAll("\\n", "").toLowerCase());
                                }
                                break;
                            case "UPPERCASE":
                                if (inputLine.length() > 256) {
                                    System.out.println("ERROR UPPERCASE command detected, but the given String is too long");
                                } else if (inputArray.length == 1) {
                                    System.out.println("ERROR UPPERCASE command detected, but there is a Missing Parameter");
                                } else {
                                    String parameter = inputLine.replaceAll("UPPERCASE ", "");
                                    System.out.println("OK " + parameter.replaceAll("\\n", "").toUpperCase());
                                }
                                break;
                            case "REVERSE":
                                if (inputLine.length() > 256) {
                                    System.out.println("ERROR REVERSE command detected, but the given String is too long");
                                } else if (inputArray.length == 1) {
                                    System.out.println("ERROR REVERSE command detected, but there is a Missing Parameter");
                                } else {
                                    String parameter = inputLine.replaceAll("\\n", "").replaceAll("REVERSE ", "");
                                    String reverse = new StringBuffer(parameter).reverse().toString();
                                    System.out.println("OK " + reverse);
                                }
                                break;
                            case "BYE":
                                if (inputLine.length() > 256) {
                                    System.out.println("ERROR BYE command detected, but the given String is too long");
                                } else if (inputArray.length > 1) {
                                    System.out.println("ERROR BYE command detected, but there is a Parameter, which is not necessary for this command");
                                } else {
                                    System.out.println("OK BYE");
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    remove();
                                    clientOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                }
                                break;
                            default:
                        }
                    } catch (EOFException e) {
                        System.err.println("Client disconnected itself");
                        socketForClient.setKeepAlive(false);
                        alive = false;
                        remove();
                        clientOutput.close();
                        clientInput.close();
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
