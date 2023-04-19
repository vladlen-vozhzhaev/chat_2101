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
                 *   "login":"ivan@mail.ru",
                 *   "pass":"123"
                 * }
                 * */
                while (true){
                    System.out.println("Введите логин: ");
                    String login = scanner.nextLine();
                    System.out.println("Введите пароль: ");
                    String pass = scanner.nextLine();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("login", login);
                    jsonObject.put("pass", pass);
                    out.writeUTF(jsonObject.toJSONString());
                    String response = in.readUTF();
                    if(response.equals("success")) break;
                    System.out.println("Неправильный логин или пароль");
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
                    String message = scanner.nextLine();
                    out.writeUTF(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
