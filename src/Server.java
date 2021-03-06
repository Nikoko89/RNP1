
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
    private boolean serverAlive;

    public Server(int port) {
    }

    public void shutDown() throws IOException{
        try {
            welcomeSocket.close();
        } catch (IOException e) {
            System.err.println("Could not close Serversocket");
        }
        serverAlive = false;
    }

    public void start() {
        try {
            welcomeSocket = new ServerSocket(6432, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverAlive = true;
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 6432;
        listenerSockets = new ArrayList<ServerHelper>();
        server = new Server(port);
        server.start();
    }

    public synchronized void remove() {
        for (int i = 0; i < listenerSockets.size(); i++) {
            if (listenerSockets.get(i).alive == false) {
                listenerSockets.remove(i);
                System.out.println("A client sad BYE and left");
                System.out.println("Clients left: " + listenerSockets.size());
            }
        }
    }

    public class ServerHelper extends Thread {
        private Socket socketForClient = null;
        private boolean alive = true;
        private DataOutputStream serverOutput;
        private DataInputStream clientInput;

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
                clientInput = new DataInputStream(socketForClient.getInputStream());
                // Nachricht vom Server an den Client
                serverOutput = new DataOutputStream(socketForClient.getOutputStream());
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
                                    serverOutput.writeUTF("ERROR <LOWERCASE commmand detected, but the given String is too long>" + '\n');
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <LOWERCASE command detected, but there is a Missing Parameter>" + '\n');
                                } else {
                                    String parameter = inputLine.replaceAll("LOWERCASE ", "");
                                    serverOutput.writeUTF("OK " + parameter.replaceAll("\\n", "").toLowerCase() + '\n');
                                }
                                break;
                            case "UPPERCASE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <UPPERCASE command detected, but the given String is too long>" + '\n');
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <UPPERCASE command detected, but there is a Missing Parameter>" + '\n');
                                } else {
                                    String parameter = inputLine.replaceAll("UPPERCASE ", "");
                                    serverOutput.writeUTF("OK " + parameter.replaceAll("\\n", "").toUpperCase() + '\n');
                                }
                                break;
                            case "REVERSE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <REVERSE command detected, but the given String is too long>" + '\n');
                                } else if (inputArray.length == 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <REVERSE command detected, but there is a Missing Parameter>" + '\n');
                                } else {
                                    String parameter = inputLine.replaceAll("\\n", "").replaceAll("REVERSE ", "");
                                    String reverse = new StringBuffer(parameter).reverse().toString();
                                    serverOutput.writeUTF("OK " + reverse + '\n');
                                }
                                break;
                            case "BYE":
                                if (inputLine.length() > 256) {
                                    serverOutput.writeUTF("ERROR <BYE command detected, but the given String is too long>" + '\n');
                                } else if (inputArray.length > 1) {
                                    serverOutput.writeUTF("ERROR SYNTAX ERROR <BYE command detected, but there is a Parameter, which is not necessary for this command>" + '\n');
                                } else {
                                    serverOutput.writeUTF("OK BYE" + '\n');
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    remove();
                                    serverOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                }
                                break;
                            case "SHUTDOWN":
                                String givenPass = inputArray[1].replaceAll("\\n", "");
                                if (givenPass.equals(PASSWORD) && listenerSockets.size() == 1){
                                    serverOutput.writeUTF("OK SHUTDOWN" + '\n');
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    remove();
                                    serverOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                    shutDown();
                                }else if (givenPass.equals(PASSWORD) && listenerSockets.size() > 1){
                                    serverAlive = false;
                                    serverOutput.writeUTF("OK SHUTDOWN" + '\n');
                                    socketForClient.setKeepAlive(false);
                                    alive = false;
                                    remove();
                                    for (ServerHelper sfcl: listenerSockets){
                                        sfcl.serverOutput.writeUTF("SHUTTINGDOWN" + '\n');
                                    }
                                    serverOutput.close();
                                    clientInput.close();
                                    socketForClient.close();
                                    while(listenerSockets.size() > 0){

                                    }
                                    shutDown();
                                }else if (!givenPass.equals(PASSWORD)){
                                    serverOutput.writeUTF("ERROR <The password you typed in is not correct>" + '\n');
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
