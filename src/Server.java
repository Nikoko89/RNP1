
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int MAX_CLIENTS = 3;
    private final String PASSWORD = "abc123";
    private static ServerSocket welcomeSocket = null;
    private static List<ServerHelper> listenerSockets;
    private static Server server;

    public Server(int port) {
    }

    public void shutDown() throws IOException{
        try {
            welcomeSocket.close();
        } catch (IOException e) {
            System.err.println("Could not close Serversocket");
        }
        server.shutDown();
    }

    public void start() {
        try {
            welcomeSocket = new ServerSocket(6432, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean serverAlive = true;
        while (serverAlive) {
            if (listenerSockets.size() < MAX_CLIENTS) {
                try {
                    System.out.println("Waiting for Clients");

                    Socket clientS = welcomeSocket.accept();
                    ServerHelper clientListener;
                    clientListener = new ServerHelper(clientS);
                    clientListener.start();
                    listenerSockets.add(clientListener);

                } catch (IOException e) {
                    System.err.println("Could not create Socket to listen for client");
                }
            }
            System.out.println("Size of current Clients: " + listenerSockets.size());
        }
        System.out.println("Sizecurrent Clients: " + listenerSockets.size());
    }

    public static void main(String[] args) {
        int port = 6432;
        listenerSockets = new ArrayList<ServerHelper>();
        server = new Server(port);
        server.start();
    }

    public void remove() {
        for (int i = 0; i < listenerSockets.size(); i++) {
            if (listenerSockets.get(i).alive == false) {
                listenerSockets.remove(i);
                System.out.println("A client sad BYE and left");
                System.out.println("Clients left: " + listenerSockets.size());
            }
        }
    }

    public boolean checkPassword(String pass){
        if (pass.equals(PASSWORD)){
            return true;
        }
        return false;
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
                DataOutputStream serverOutput = new DataOutputStream(socketForClient.getOutputStream());
                String inputLine;
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
                                    serverOutput.writeUTF("ERROR <LOWERCASE commmand detected, but the given String is too long>");
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <LOWERCASE command detected, but there is a Missing Parameter>");
                                } else {
                                    String parameter = inputLine.replaceAll("LOWERCASE ", "");
                                    serverOutput.writeUTF("OK " + parameter.replaceAll("\\n", "").toLowerCase());
                                }
                                break;
                            case "UPPERCASE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <UPPERCASE command detected, but the given String is too long>");
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <UPPERCASE command detected, but there is a Missing Parameter>");
                                } else {
                                    String parameter = inputLine.replaceAll("UPPERCASE ", "");
                                    serverOutput.writeUTF("OK " + parameter.replaceAll("\\n", "").toUpperCase());
                                }
                                break;
                            case "REVERSE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <REVERSE command detected, but the given String is too long>");
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <REVERSE command detected, but there is a Missing Parameter>");
                                } else {
                                    String parameter = inputLine.replaceAll("\\n", "").replaceAll("REVERSE ", "");
                                    String reverse = new StringBuffer(parameter).reverse().toString();
                                    serverOutput.writeUTF("OK " + reverse);
                                }
                                break;
                            case "BYE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <BYE command detected, but the given String is too long>");
                                } else if (inputArray.length > 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <BYE command detected, but there is a Parameter, which is not necessary for this command>");
                                } else {
                                    serverOutput.writeUTF("OK BYE");
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    remove();
                                    serverOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                }
                                break;
                            case "SHUTDOWN":
                                if (inputArray[1].equals(PASSWORD) && listenerSockets.size() == 0){
                                    serverOutput.writeUTF("OK SHUTDOWN");
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    serverOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                    shutDown();
                                }else if (inputArray[1].equals(PASSWORD) && listenerSockets.size() > 0){
                                    //TODO
                                }else if (!inputArray[1].equals(PASSWORD)){
                                    serverOutput.writeUTF("ERROR <The password you typed in is not correct>");
                                }
                                break;
                            default:
                                serverOutput.writeUTF("ERROR UNKNOWN COMMAND");
                        }
                    } catch (EOFException e) {
                        System.err.println("Client disconnected itself");
                        socketForClient.setKeepAlive(false);
                        alive = false;
                        remove();
                        serverOutput.close();
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
