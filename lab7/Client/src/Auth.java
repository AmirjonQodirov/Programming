import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Auth {

    private SocketChannel server;
    static ObjectOutputStream oos;
    static ObjectInputStream ois;
    private String login;
    private String password;
    private Scanner scanner;
    private Request request;

    Auth() {
        scanner = new Scanner(System.in);
    }

    private void connect() throws IOException {
        server = SocketChannel.open(new InetSocketAddress("localhost", 5001));
        oos = new ObjectOutputStream(server.socket().getOutputStream());
        ois = new ObjectInputStream(server.socket().getInputStream());
    }

    void disconnect() {
        try {
            oos.close();
            ois.close();
            server.close();
        } catch (IOException ignored) {
        }
    }

    private boolean logIn(String login, String password) throws IOException {
        try {
            oos.writeObject(new Request("logIn", login, password));
            request = (Request) ois.readObject();
            String input = request.result;
            if (request.flag) {
                this.login = login;
                this.password = password;
                return true;
            } else {
                System.out.println(input);
                return false;
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean signUp(String login, String password) throws IOException {
        try {
            oos.writeObject(new Request("signUp", login, password));
            request = (Request) ois.readObject();
            String input = request.result;
            if (input.length() > 0 || !request.flag) {
                System.out.println(input);
                return false;
            } else return true;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean checkLogin(int code) throws IOException {
        try {
            connect();
            oos.writeObject(new Request("checkLogin", login, null));
            request = (Request) ois.readObject();
            String input = request.result;
            if (input.length() > 0) {
                System.out.println(input);
                return code != 1;
            } else if (code == 2 && request.flag) {
                System.out.println("Пользователь не найден.");
            } else if (code == 1 && !request.flag)
                System.out.println("Такой пользователь существует.");
            return request.flag;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean sendPassword() throws IOException {
        try {
            oos.writeObject(new Request("sendPassword", login, null));
            request = (Request) ois.readObject();
            String input = request.result;
            if (!request.flag) System.out.println(input);
            return request.flag;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean chekPassword(String login,String password) throws IOException {
        try {
            oos.writeObject(new Request("checkPassword", login, password));
            request = (Request) ois.readObject();
            String input = request.result;
            if (input.length() > 0) {
                System.out.println(input);
                return false;
            }
            if (!request.flag) {
                System.out.println("Пароль не соответствует нашим ожиданиям");
                return false;
            } else return true;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    void logIn() throws IOException {
        System.out.println("Введите почту(логин):");
        login = scanner.nextLine();
        System.out.println("Поиск логина...");
        if (!checkLogin(2)) {
            System.out.println("Логин найден");
            System.out.println("Введите пароль:");
            password = scanner.nextLine();
            System.out.println("Проверка пароля...");
            if (logIn(login, password)) {
                RequestsSender sender = new RequestsSender(login,password);
                System.out.println("Добро пожаловать " + login);
                sender.work();
            }
        }
    }
    void signUp() throws IOException {
        System.out.println("Введите почту(логин):");
        login = scanner.nextLine();
        System.out.println("Ожидайте...");
        if (checkLogin(1)) {
            System.out.println("Подготовка к отправлению");
            if(sendPassword()) {
                System.out.println("Пароль отправлен\nВведите пароль, отправленный на " + login + ":");
                String password = scanner.nextLine();
                if (chekPassword(login,password)) {
                    System.out.println("Ожидайте...");
                    if (signUp(login, password))
                        System.out.println("Теперь у вас есть доступ к базе данных.");
                }
            }
        }
    }

}
