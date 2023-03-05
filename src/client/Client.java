package client;

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
                while (true){
                    String message = scanner.nextLine();
                    out.writeUTF(message);
                    System.out.println(in.readUTF());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
