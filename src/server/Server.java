package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
                User user = new User(socket);
                sockets.add(socket); // добавляем подключившегося клиента в список
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true){
                                JSONParser jsonParser = new JSONParser();
                                JSONObject jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                String login = jsonObject.get("login").toString();
                                String pass = jsonObject.get("pass").toString();
                                if(user.login(URL_DB, LOGIN_DB, PASS_DB, login, pass)) break;
                                /*String command = "";
                                if(command.equals("/reg")){
                                    if(user.reg(URL_DB, LOGIN_DB, PASS_DB)) break;
                                }else if (command.equals("/login")){
                                    if(user.login(URL_DB, LOGIN_DB, PASS_DB)) break;
                                }else{
                                    user.getOut().writeUTF("Неверная команда");
                                }*/
                            }
                            while (true){
                                String clientMessage = user.getIn().readUTF();
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
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
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
