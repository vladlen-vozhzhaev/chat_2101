package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        ArrayList<Socket> sockets = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(9178);
            System.out.println("Сервер запущен");
            // Загружаем класс для работы с БД
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            while (true){
                // Ждём клиента и сохраняем его в socket
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                sockets.add(socket); // добавляем подключившегося клиента в список
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            out.writeUTF("Введите имя: ");
                            String name = in.readUTF();
                            out.writeUTF("Введите логин: ");
                            String login = in.readUTF();
                            out.writeUTF("Введите пароль: ");
                            String pass = in.readUTF();

                            while (true){
                                String clientMessage = in.readUTF();
                                System.out.println(clientMessage);
                                for (int i = 0; i < sockets.size(); i++) {
                                    Socket socket1 = sockets.get(i);
                                    DataOutputStream out1 = new DataOutputStream(socket1.getOutputStream());
                                    out1.writeUTF(clientMessage.toUpperCase()); // Сервер отправляет сообщение
                                }
                            }
                        }catch (IOException e){
                            System.out.println("Потеряно соединение с клиентом");
                            sockets.remove(socket);
                        }
                    }
                });
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
