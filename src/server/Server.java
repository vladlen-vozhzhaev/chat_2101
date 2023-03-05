package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9178);
            System.out.println("Сервер запущен");
            // Ждём клиента и сохраняем его в socket
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключился");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String clientMessage = in.readUTF();
            System.out.println(clientMessage);
            out.writeUTF(clientMessage.toUpperCase()); // Сервер отправляет сообщение
            System.out.println("Сервер отключен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
