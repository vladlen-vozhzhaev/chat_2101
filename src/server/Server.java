package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Server {
    private static final  String URL_DB = "jdbc:mysql://127.0.0.1:3306/android_chat_2101";
    private static final String LOGIN_DB = "root";
    private static final String PASS_DB = "";
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
                            String name;
                            while (true){
                                out.writeUTF("Для регистрации аккаунта введите /reg, для авторизации /login");
                                String command = in.readUTF();
                                if(command.equals("/reg")){
                                    out.writeUTF("Введите имя: ");
                                    name = in.readUTF();
                                    out.writeUTF("Введите логин: ");
                                    String login = in.readUTF();
                                    out.writeUTF("Введите пароль: ");
                                    String pass = in.readUTF();
                                    Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                                    Statement statement = connection.createStatement();
                                    statement.executeUpdate("INSERT INTO `users`(`name`, `login`, `pass`) " +
                                            "VALUES ('"+name+"','"+login+"','"+pass+"')");
                                    statement.close();
                                    break;
                                }else if (command.equals("/login")){
                                    out.writeUTF("Введите логин: ");
                                    String login = in.readUTF();
                                    out.writeUTF("Введите пароль: ");
                                    String pass = in.readUTF();
                                    Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet =  statement.executeQuery("SELECT * FROM `users` WHERE login='"+login+"' AND pass='"+pass+"'");
                                    if(resultSet.next()){
                                        name = resultSet.getString("name");
                                        out.writeUTF("Успешный вход в систему, ваше имя "+name);
                                        break;
                                    }else{
                                        out.writeUTF("Неправильный логин или пароль");
                                    }
                                }else{
                                    out.writeUTF("Неверная команда");
                                }
                            }


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
                        }catch (SQLException e){
                            e.printStackTrace();
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
