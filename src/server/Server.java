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
                            user.getOut().writeUTF(user.getName()+ " добро пожаловать на сервер");
                            Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT * FROM `messages`, `users` WHERE from_id = users.id;");
                            while (resultSet.next()){
                                String msg = resultSet.getString("msg");
                                String fromUser = resultSet.getString("name");
                                int toUser = resultSet.getInt("to_id");
                                if(user.getId() == toUser)
                                    user.getOut().writeUTF("Личное сообщение - "+fromUser+": "+msg);
                                else if (toUser == 0){
                                    user.getOut().writeUTF(fromUser+": "+msg);
                                }
                            }
                            statement.close();
                            // Общение с клиентами
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                boolean publicMsg = (boolean) jsonObject.get("public");
                                String clientMessage = (String) jsonObject.get("msg");
                                System.out.println(clientMessage);
                                if(publicMsg){// Рассылаем всем
                                    connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                                    statement = connection.createStatement();
                                    statement.executeUpdate(
                                            "INSERT INTO `messages`(`msg`, `from_id`) VALUES ('"+clientMessage+"','"+user.getId()+"')"
                                    );
                                    statement.close();
                                    // user - тот кто отправляет, user1 - тот кому отправляем
                                    for (User user1 : users) {
                                        user1.getOut().writeUTF(user.getName()+": "+clientMessage.toUpperCase()); // Сервер отправляет сообщение
                                    }
                                }else{
                                    // было "2" стало 2
                                    int toUser = Integer.parseInt(jsonObject.get("id").toString());
                                    connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                                    statement = connection.createStatement();
                                    statement.executeUpdate(
                                            "INSERT INTO `messages`(`msg`, `from_id`, `to_id`) " +
                                                "VALUES ('"+clientMessage+"','"+user.getId()+"', '"+toUser+"')"
                                    );
                                    statement.close();
                                    for (User user1 : users) {
                                        if(user1.getId() == toUser){
                                            user1.getOut().writeUTF(user.getName()+": "+clientMessage);
                                            break;
                                        }
                                    }
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
