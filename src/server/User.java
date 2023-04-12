package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class User {
    private int id;
    private String name;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public User(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(this.socket.getInputStream());
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }
    public boolean reg(String URL_DB, String LOGIN_DB, String PASS_DB) throws IOException, SQLException {
        this.getOut().writeUTF("Введите имя: ");
        name = this.getIn().readUTF();
        this.getOut().writeUTF("Введите логин: ");
        String login = this.getIn().readUTF();
        this.getOut().writeUTF("Введите пароль: ");
        String pass = this.getIn().readUTF();
        Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO `users`(`name`, `login`, `pass`) " +
                "VALUES ('"+name+"','"+login+"','"+pass+"')");
        statement.close();
        this.setName(name);
        return true;
    }

    public boolean login(String URL_DB, String LOGIN_DB, String PASS_DB) throws IOException, SQLException {
        this.getOut().writeUTF("Введите логин: ");
        String login = this.getIn().readUTF();
        this.getOut().writeUTF("Введите пароль: ");
        String pass = this.getIn().readUTF();
        Connection connection = DriverManager.getConnection(URL_DB, LOGIN_DB, PASS_DB);
        Statement statement = connection.createStatement();
        ResultSet resultSet =  statement.executeQuery("SELECT * FROM `users` WHERE login='"+login+"' AND pass='"+pass+"'");
        if(resultSet.next()){
            this.name = resultSet.getString("name");
            this.getOut().writeUTF("Успешный вход в систему, ваше имя "+this.name);
            return true;
        }else{
            this.getOut().writeUTF("Неправильный логин или пароль");
            return false;
        }
    }
}
