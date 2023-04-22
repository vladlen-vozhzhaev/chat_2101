package client;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Socket socket;
        {
            try {
                // Подключаемся к серверу
                socket = new Socket("127.0.0.1", 9178);
                // Поток вывода (для отправки сообщения серверу)
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in);
                /*
                 * {} - пустой JSON
                 * {
                 *   "action": "login"
                 *   "login":"ivan@mail.ru",
                 *   "pass":"123"
                 * }
                 * */
                // Данный цикл для авторизации или регистрации (не для общения)
                while (true){
                    System.out.println("Для регистрации введите /reg, для авторизации введите /login");
                    String command = scanner.nextLine(); // Ждём от пользователя команду
                    JSONObject jsonObject = new JSONObject(); // Создаём пустой JSON
                    if(command.equals("/reg")){ // Если пользователь ввёл /reg
                        System.out.println("Введите имя: ");
                        String name = scanner.nextLine();
                        System.out.println("Введите логин: ");
                        String login = scanner.nextLine();
                        System.out.println("Введите пароль: ");
                        String pass = scanner.nextLine();
                        jsonObject.put("action", "reg");
                        jsonObject.put("name", name);
                        jsonObject.put("login", login);
                        jsonObject.put("pass", pass);
                        out.writeUTF(jsonObject.toJSONString()); // Отправляем на сервер наш JSON
                        String response = in.readUTF(); // Читаем ответ
                        if(response.equals("success")) break; // Если ответ success, то прерываем цикл
                    } else if (command.equals("/login")) { // Если пользователь ввёл /login
                        System.out.println("Введите логин: ");
                        String login = scanner.nextLine();
                        System.out.println("Введите пароль: ");
                        String pass = scanner.nextLine();
                        jsonObject.put("action", "login");
                        jsonObject.put("login", login);
                        jsonObject.put("pass", pass);
                        out.writeUTF(jsonObject.toJSONString());// Отправляем на сервер наш JSON
                        String response = in.readUTF();// Читаем ответ
                        if(response.equals("success")) break;// Если ответ success, то прерываем цикл
                        else System.out.println("Неправильный логин или пароль");
                    }else{
                        System.out.println("Недопустимая команда");
                    }

                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true){
                                System.out.println(in.readUTF()); // Чтение сообщения с сервера
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();

                while (true){
                    JSONObject jsonObject = new JSONObject(); // Создаём пустой JSON
                    String message = scanner.nextLine();
                    if(message.contains("/m")){
                        jsonObject.put("public", false);
                        // /m 2 hello my friend
                        String userId = message.split(" ")[1];
                        String msg = message.substring(3+userId.length());
                        jsonObject.put("msg", msg);
                        jsonObject.put("id", userId);
                    }else{
                        jsonObject.put("public", true);
                        jsonObject.put("msg", message);
                    }
                    out.writeUTF(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
