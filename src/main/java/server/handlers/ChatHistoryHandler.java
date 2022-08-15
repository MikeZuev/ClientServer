package server.handlers;

import java.io.*;

public class ChatHistoryHandler {

    private BufferedWriter bf;
    private  File file;


    private  BufferedReader reader;


    public ChatHistoryHandler() {

        try {
            file = new File("src/main/resources/ChatHistory/chatHistory.txt");
            reader = new BufferedReader(new FileReader(file));
            bf = new BufferedWriter(new FileWriter(file, true));






        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeChatHistory(String message) {
        try {
            bf.write(message + "\n");
            bf.write("\n");
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public  String readChatHistory() {

        StringBuilder sb = new StringBuilder();
        String a;

        try {

            while((a = reader.readLine()) != null) {
                sb.append(a);
                sb.append("\n");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();



    }



}
