package server.myserver;

import server.handlers.ClientHandler;
import server.services.AuthenticationService;
import server.services.impl.SimpleAuthenticationServiceImpl;
import server.services.impl.SqlAuthenticationServiceImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MyServer {


    private final ServerSocket serverSocket;
    private final AuthenticationService authenticationService;
    private final ArrayList<ClientHandler> clients;
    private ArrayList<String> usersOnline = new ArrayList<>();

    public MyServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    //    authenticationService = new SimpleAuthenticationServiceImpl();
        authenticationService = new SqlAuthenticationServiceImpl();
        clients = new ArrayList<>();

    }

    public void start() {
        System.out.println("СЕРВЕР ЗАПУЩЕН!");
        System.out.println("---------------");
        try {

            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void waitAndProcessNewClientConnection() throws IOException {
        System.out.println("Ожидание клиента...");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился!");


        processClientConnection(socket);

    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler handler = new ClientHandler(this, socket);
        handler.handle();
    }

    public synchronized void subscribe(ClientHandler handler) {
        clients.add(handler);
    //    usersOnline.add(handler.getUsername());
    //    broadcastOnlineUsers(handler, usersOnline);


    }

    public synchronized void unsubscribe(ClientHandler handler) {
        clients.remove(handler);
    //    usersOnline.remove(handler.getUsername());
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;

            }

        }


        return false;
    }

    public void stop() {
        System.out.println("------------------");
        System.out.println("------------------");
        System.out.println("ЗАВЕРШЕНИЕ РАБОТЫ ");
        System.exit(0);
    }

    public synchronized void broadcastMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {

            client.sendMessage(sender.getUsername(), message);

        }

    }

    public synchronized void broadcastPrivateMessage(ClientHandler sender, String recipient, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendPrivateMessage(sender.getUsername(), message);
                sender.sendPrivateMessage(recipient, message);
                break;

            }

        }


    }

    public void broadcastServerMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(sender.getUsername())) {
                continue;
            } else {
                client.sendServerMessage(sender.getUsername(), message);
            }


        }
    }

    public void broadcastOnlineUsers(ClientHandler user) {
        StringBuilder usersList = new StringBuilder();




        for (ClientHandler client : clients) {
            usersList.append(client.getUsername() + " ");

            }
        for (ClientHandler client : clients) {
            client.sendOnlineUser(usersList);

        }

        }
    }




