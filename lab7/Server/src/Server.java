import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Server {

    private static ArrayList<Receiver> clients = new ArrayList<>();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("Выход")
        ));
        int id = 0;
        final int PORT = 5001;
        CollectionManager manager = null;
        DataBaseManager DBman = new DataBaseManager();
        Receiver receiver = new Receiver(id);
        try {
            if (args.length == 0)
                throw new ArrayIndexOutOfBoundsException("Имя файла должно передаваться программе с помощью аргумента командной строки.");
            manager = new CollectionManager(new File(args[0]), DBman, receiver);
            if (manager.loadFile(new File(args[0]), receiver, null,null)) System.out.println(receiver.get());
            else {
                System.out.println(receiver.get());
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }


        try {
            try (ServerSocket server = new ServerSocket(PORT)) {
                System.out.println("Сервер запущен");
                System.out.println("Сервер работает клиентов(port = " + PORT + ")");
                while (true) {
                    Socket client = server.accept();
                    id++;
                    RequestsHandler kek = new RequestsHandler(client, manager, DBman, id);
                    new Thread(kek).start();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static void add(Receiver client) {
        clients.add(client);
    }

    static void remove(Receiver client) {
        clients.remove(client);
    }

    static void sendToAll(String message, Receiver sender) {
        try {
            for (Receiver receiver : clients) {
                if (!sender.equals(receiver))
                    receiver.addFromServer(message);
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }
}