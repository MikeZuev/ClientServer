package server.handlers;

import org.w3c.dom.ls.LSOutput;
import server.myserver.MyServer;
import server.services.AuthenticationService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String ONLINE_USER_PREFIX = "/online"; // + adding username name to ListView
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";




    private MyServer myServer;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    private ChatHistoryHandler chatHistoryHandler;



    public ClientHandler(MyServer myServer, Socket socket) {


        chatHistoryHandler = new ChatHistoryHandler();

        this.myServer = myServer;
        this.clientSocket = socket;
    }


    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() ->{
            try {
                authentication();
                readMessage();
            }catch(IOException e){
                e.printStackTrace();
                myServer.unsubscribe(this);
                try {
                    myServer.broadcastServerMessage(this, "Пользователь вышел из чата");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void authentication() throws IOException {
        while(true) {
            String message = in.readUTF();

            if(message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthentication(message);
                if(isSuccessAuth){
                    break;
                }
            } else{
                out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
                System.out.println("Неверная команда аутентификации");
            }
        }
    }

    private boolean processAuthentication(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if(parts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            System.out.println("Неверная команда аутентификации");

            return false;
        }

        String login = parts[1];
        String password = parts[2];

        AuthenticationService auth = myServer.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(login, password);

        if(username != null) {
            if(myServer.isUsernameBusy(username)){
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }



            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username + " " + chatHistoryHandler.readChatHistory()); // ????????????????????????????????
            myServer.subscribe(this);
            System.out.println("User: " + username + " connected to Chat");
            myServer.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");
            myServer.broadcastOnlineUsers(this);
            return true;


        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");

            return false;
        }

    }

    private void readMessage() throws IOException {
        while(true){
            String message = in.readUTF();

            System.out.println("message | " + username + ": " + message);

            String typeMessage = message.split(("\\s+"))[0];
            if(!typeMessage.startsWith("/")) {
                System.out.println("Неверный запрос");
            }



            switch(typeMessage){
                case STOP_SERVER_CMD_PREFIX -> myServer.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case CLIENT_MSG_CMD_PREFIX -> {
                    String[] messageParts = message.split("\\s+", 2);

                    myServer.broadcastMessage(this, messageParts[1]);}
                case PRIVATE_MSG_CMD_PREFIX -> {
                    String[] messagePartsPrivate = message.split("\\s+", 3);
                    String recipient = messagePartsPrivate[1];
                    String PmMessage = messagePartsPrivate[2];
                    myServer.broadcastPrivateMessage(this, recipient, PmMessage);


                }

                default -> System.out.println("Неверная команда");

            }

        }
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        System.out.println(username + " отключился");
    }

    public void sendServerMessage(String sender, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s", SERVER_MSG_CMD_PREFIX, this.getUsername(), message));

    }

    public void sendMessage(String sender, String message) throws IOException {
        String timeStamp = DateFormat.getInstance().format(new Date());

        chatHistoryHandler.writeChatHistory(String.format("%s \n%s: %s", timeStamp, sender, message));
        out.writeUTF(String.format("%s %s %s", CLIENT_MSG_CMD_PREFIX, sender, message));
    }

    public void sendPrivateMessage(String recipient, String message) throws IOException {
        out.writeUTF(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX,  recipient, message));
    }

    public String getUsername() {
        return username;
    }

    public void sendOnlineUser(StringBuilder usernames) {
        try {
            out.writeUTF(String.format("%s %s", ONLINE_USER_PREFIX, usernames));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
