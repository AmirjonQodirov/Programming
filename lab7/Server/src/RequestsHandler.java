import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RequestsHandler extends Thread {
    private Socket client;
    private CollectionManager manager;
    private DataBaseManager DBman;
    private boolean exit = false;
    private Receiver receiver;

    RequestsHandler(Socket socket, CollectionManager manager, DataBaseManager DBman, int id) {
        this.client = socket;
        this.manager = manager;
        this.DBman = DBman;
        receiver = new Receiver(id);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            manager.save(receiver);
            exit();
        }));
    }

    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())) {
            Request request;
            while (!exit) {
                try {
                    request = (Request) ois.readObject();
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                    request = new Request("", "","");
                }

                String command = request.command;
                Karlson karlson = request.karlson;
                File file = request.file;
                String login = request.login;
                String password = request.password;
                String str = request.result;

                new Thread(() -> {
                    try {
                        if (password != null) {
                            Request rq ;
                                switch (command) {
                                    case "signUp":
                                        if (DBman.signUp(login, password, receiver)) {
                                            rq =  new Request(receiver.get(), true);
                                        } else {
                                            receiver.add("Не удалось зарегестрироваться");
                                            rq = new Request(receiver.get(), false);
                                        }
                                        oos.writeObject(rq);
                                        break;
                                    case "logIn":
                                        if (DBman.logIn(login, password, receiver)) {
                                            System.out.println(login + " подключился");
                                            Server.sendToAll(login + " подключился", receiver);
                                            Server.add(receiver);
                                            rq = new Request(receiver.get(), true);
                                        } else
                                            rq = new Request(receiver.get(), false);
                                        oos.writeObject(rq);
                                        break;
                                    case "checkPassword":
                                        if (DBman.checkPassword(login, password, receiver)) {
                                            Server.add(receiver);
                                            rq = new Request(receiver.get(), true);
                                        } else
                                            rq = new Request(receiver.get(), false);
                                        oos.writeObject(rq);
                                        break;
                                    case ("info"):
                                        manager.info(receiver);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "help":
                                        manager.help(receiver);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "show":
                                        manager.show(receiver);
                                        oos.writeObject(receiver.get());break;
                                    case "clear":
                                        manager.clear(receiver, login, password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "add":
                                        manager.add(karlson, receiver, login,password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "remove":
                                        manager.remove(karlson, receiver, login,password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "add_if_max":
                                        manager.addIfMax(karlson, receiver, login,password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "import":
                                        manager.load(str, receiver, login,password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "load":
                                        manager.loadFile(file, receiver, login,password);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "save":
                                        manager.save(receiver);
                                        oos.writeObject(receiver.get());
                                        break;
                                    case "exit":
                                        if (!DBman.checkLogin(login,receiver)) {
                                            System.out.println(login + " отключился");
                                            Server.sendToAll(login + " отключился", receiver);
                                        } else {
                                            Server.sendToAll("Кто-то отключился", receiver);
                                            System.out.println("Кто-то отключился");
                                            receiver.add("Кажется вы замешаны в какой-то подозрительной активности");
                                        }
                                        justExit();
                                        oos.writeObject(receiver.get());
                                        break;
                                }
                        } else {
                            Request answer = null;
                            boolean success;
                            switch (command) {
                                case "checkLogin":
                                    if (DBman.checkLogin(login, receiver)) {
                                        answer = new Request(receiver.get(), true);
                                    } else
                                        answer = new Request(receiver.get(), false);
                                    break;
                                case "sendPassword":
                                    success = DBman.sendPassword(login, receiver);
                                    answer = new Request(receiver.get(), success);
                                    break;

                            }
                            if (!exit) oos.writeObject(answer);
                        }
                    } catch (IOException ignored) {
                    }
                }).start();
            }
            client.close();
        } catch (IOException ignored) {
        }
    }

    private void exit() {
        justExit();
    }

    private void justExit() {
        Server.remove(receiver);
        exit = true;
    }
}