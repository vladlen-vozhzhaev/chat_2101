package server;

import org.json.simple.JSONArray;
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
    private static ArrayList<User> users = new ArrayList<>(); // Список подключенных пользователей
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9178);
            System.out.println("Сервер запущен");
            // Загружаем класс для работы с БД
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            while (true){
                // Ждём клиента и сохраняем его в socket
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                User user = new User(socket); // Создаём объект пользователя
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonObject;
                            // Авторизация/Регистрация
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
                            System.out.println(user.getName()+" подключился");
                            sendOnlineUsers(); // Рассылаем список пользователей которые в сети
                            jsonObject.put("msg", user.getName()+ " добро пожаловать на сервер");
                            user.getOut().writeUTF(jsonObject.toJSONString());
                            // Отправляем сообщения из базы данных (история сообщений)
                            Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT * FROM `messages`, `users` WHERE from_id = users.id;");
                            while (resultSet.next()){
                                String msg = resultSet.getString("msg");
                                String fromUser = resultSet.getString("name");
                                int toUser = resultSet.getInt("to_id");
                                int fromId = resultSet.getInt("from_id");
                                jsonObject.put("msg", fromUser+": "+msg);
                                if(user.getId() == toUser) { // Отправка приватного (лчиного) сообщения
                                    jsonObject.put("fromId", fromId);
                                    user.getOut().writeUTF(jsonObject.toJSONString());
                                }
                                else if (toUser == 0){
                                    user.getOut().writeUTF(jsonObject.toJSONString());
                                }
                            }
                            statement.close();
                            // Общение с клиентами
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                boolean publicMsg = (boolean) jsonObject.get("public");
                                String clientMessage = (String) jsonObject.get("msg");
                                jsonObject.put("msg", user.getName()+": "+clientMessage);
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
                                        user1.getOut().writeUTF(jsonObject.toJSONString()); // Сервер отправляет сообщение
                                    }
                                }else{ // отправка личного сообщения
                                    int toUser = Integer.parseInt(jsonObject.get("id").toString());
                                    connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
                                    statement = connection.createStatement();
                                    statement.executeUpdate(
                                            "INSERT INTO `messages`(`msg`, `from_id`, `to_id`) " +
                                                "VALUES ('"+clientMessage+"','"+user.getId()+"', '"+toUser+"')"
                                    );
                                    statement.close();
                                    jsonObject.put("fromId", user.getId());
                                    for (User user1 : users) {
                                        if(user1.getId() == toUser){
                                            user1.getOut().writeUTF(jsonObject.toJSONString());
                                            break;
                                        }
                                    }
                                }
                            }
                        }catch (IOException e){
                            System.out.println("Потеряно соединение с клиентом");
                            users.remove(user);
                            sendOnlineUsers(); // Рассылаем список пользователей которые в сети
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

    // Метод отправки списка пользователей которые находятся в сети
    public static void sendOnlineUsers(){
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (User user : users) { // Цикл для наполнения jsonArray
                JSONObject userJSONobject = new JSONObject(); // JSON для хранения id и name пользователя
                userJSONobject.put("id", user.getId()); // Добавляем ID пользователя
                userJSONobject.put("name", user.getName()); // Добавляем имя пользователя
                jsonArray.add(userJSONobject); // Добавляем JSON с ID и name в массив
            }
            jsonObject.put("onlineUsers", jsonArray); // Кладём в итоговый JSON который отправится клиенту
            for (User user : users){ // Цикл для отправки списка пользователей
                user.getOut().writeUTF(jsonObject.toJSONString()); // Отправляем JSON клиенту
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
