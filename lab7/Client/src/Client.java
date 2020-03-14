import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\nВыход")
        ));
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Регистрация(R) | Вход(L) | Выход(exit)");
                String answer = scanner.nextLine();
                Auth auth = new Auth();
                switch (answer.toLowerCase()) {
                    case "r":
                        auth.signUp();
                        continue;
                    case "l":
                        auth.logIn();
                        auth.disconnect();
                        continue;
                    case "exit":
                        System.exit(1);
                        break;
                }
            } catch (IOException e) {
                System.out.println("Сервер недоступен");
            }catch (NoSuchElementException e){
                break;
            }
        }
    }
}