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
        ArrayList<User> users = new ArrayList<>();
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
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonObject;
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                String action = jsonObject.get("action").toString();
                                String login = jsonObject.get("login").toString();
                                String pass = jsonObject.get("pass").toString();
                                if(action.equals("reg")){
                                    String name = jsonObject.get("name").toString();
                                    if(user.reg(URL_DB, LOGIN_DB, PASS_DB, name, login, pass))  break;
                                } else if (action.equals("login")) {
                                    if(user.login(URL_DB, LOGIN_DB, PASS_DB, login, pass)) break;
                                }
                            }
                            users.add(user);
                            // Общение с клиентами
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                boolean publicMsg = (boolean) jsonObject.get("public");
                                if(publicMsg){// Рассылаем всем
                                    String clientMessage = (String) jsonObject.get("msg");
                                    System.out.println(clientMessage);
                                    for (int i = 0; i < users.size(); i++) {
                                        users.get(i).getOut().writeUTF(clientMessage.toUpperCase()); // Сервер отправляет сообщение
                                    }
                                }else{
                                    // Отправляем личное сообщение
                                }


                            }
                        }catch (IOException e){
                            System.out.println("Потеряно соединение с клиентом");
                            users.remove(user);
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
