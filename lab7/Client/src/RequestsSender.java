import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;

class RequestsSender {
    private final int PORT = 5001;
    private boolean isWorking = false;
    private boolean exit = false;
    private SocketAddress socketAddress;
    private Karlson forAction;
    private File file;
    private int countTryConnect = 0;
    private String jsonStr = "";
    private String login;
    private String password;
    private String HOST = "localhost";

    RequestsSender(String login, String password) {
        this.login = login;
        this.password = password;
        this.socketAddress = new InetSocketAddress(HOST, PORT);
    }

    void work() {
        while (!exit) {
            if (countTryConnect == 0)
                System.out.println("Соединение с сервером(IP address " + HOST + ", port " + PORT + ")");
            SocketChannel server = connect();
            countTryConnect = 0;
            isWorking = true;
            if (server == null) break;
            try (ObjectOutputStream oos = Auth.oos;
                 ObjectInputStream ois = Auth.ois) {
                System.out.println("Сервер доступен и готов принимать команды.\n");
                oos.writeObject(new Request("info", login, password));
                String input = (String) ois.readObject();
                if (isTimeOut(input)) {
                    server.close();
                    continue;
                }
                System.out.println("Server:\n" + input);
                while (isWorking) {
                    String[] fullCommand = readAndParseCommand();
                    String answer = "";
                    if (!parse(fullCommand)) continue;
                    boolean error = false;
                    switch (fullCommand[0]) {
                        case "info":
                        case "help":
                        case "show":
                        case "clear":
                        case "save":
                            oos.writeObject(new Request(fullCommand[0], login, password));
                            answer = (String) ois.readObject();
                            if (isTimeOut(answer))
                                server.close();
                            break;

                        case "add":
                        case "remove":
                        case "add_if_max":
                            oos.writeObject(new Request(fullCommand[0], forAction, login, password));
                            answer = (String) ois.readObject();
                            if (isTimeOut(answer))
                                server.close();
                            break;

                        case "load":
                            oos.writeObject(new Request(fullCommand[0], file, login, password));
                            answer = (String) ois.readObject();
                            if (isTimeOut(answer))
                                server.close();
                            break;

                        case "exit":
                            isWorking = false;
                            exit = true;
                            oos.writeObject(new Request(fullCommand[0], login, password));
                            answer = (String) ois.readObject();
                            if (answer.length() > 0) System.out.println(answer);
                            System.out.println("Выход");
                            server.close();
                            break;

                        default:
                            error = true;
                            System.out.println("ОШИБКА: Неизвестная команда\n" +
                                    "Для помощи введите команду help.");
                    }
                    if (!error && !exit)
                        System.out.println("Server:\n" + answer);
                }
            } catch (IOException e) {
                if (countTryConnect == 0) System.out.println("Сервер временно недоступен(подключаюсь)");
                if (countTryConnect > 4) {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        System.out.println("Хотите возобновить попытки подключения?(Y/N)");
                        String command = scanner.nextLine();
                        if (command.equalsIgnoreCase("Y")) {
                            countTryConnect = 0;
                            break;
                        } else if (command.equalsIgnoreCase("N")) {
                            System.out.println("Выход");
                            exit = true;
                            break;
                        }
                    }
                } else {
                    countTryConnect++;
                    isWorking = false;
                }
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }


    private SocketChannel connect() {
        SocketChannel socket;
        try {
            socket = SocketChannel.open(socketAddress);
            ObjectOutputStream oos = new ObjectOutputStream(socket.socket().getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.socket().getInputStream());
            isWorking = true;
            Auth.ois = ois;
            Auth.oos = oos;
        } catch (IOException e) {
            if (isWorking) {
                System.out.println("Не удается подключиться к серверу. Ожидайте...");
                isWorking = false;
            }
            while (true) {
                try {
                    countTryConnect++;
                    Thread.sleep(1000);
                    System.out.println("Не удается подключиться к серверу. Ожидайте...");
                    socket = SocketChannel.open(socketAddress);
                    Auth.oos = new ObjectOutputStream(socket.socket().getOutputStream());
                    Auth.ois = new ObjectInputStream(socket.socket().getInputStream());
                    isWorking = true;
                    countTryConnect = 0;
                    break;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException ex) {
                    if (countTryConnect > 5) {
                        Scanner scanner = new Scanner(System.in);
                        while (true) {
                            System.out.println("Хотите возобновить попытки подключения?(Y/N)");
                            String command = scanner.nextLine();
                            if (command.equalsIgnoreCase("Y")) {
                                countTryConnect = 1;
                                break;
                            } else if (command.equalsIgnoreCase("N")) {
                                System.out.println("Программа завершена");
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return socket;
    }

    private boolean parse(String[] fullCommand) {
        try {
            if ((fullCommand[0].equals("add_if_max") || fullCommand[0].equals("add") || fullCommand[0].equals("remove")
                    || fullCommand[0].equals("load")) || fullCommand[0].equals("import")) {
                if (fullCommand.length == 1) {
                    System.out.println("  Ошибка, " + fullCommand[0] + " должyа иметь аргумент.");
                    return false;
                } else if (fullCommand[0].equals("add_if_max") || fullCommand[0].equals("add")
                        || fullCommand[0].equals("remove")) {
                    Gson gson = new Gson();
                    String jsonStr = fullCommand[1];
                    jsonStr = jsonStr.replace(" ", "");
                    if (jsonStr.contains("\"name\"")) {
                        forAction = gson.fromJson(jsonStr, Karlson.class);
                    } else forAction = null;
                    if (forAction == null || forAction.getName() == null || forAction.getAge() == null || forAction.getLocation() == null) {
                        System.out.println("  Ошибка, элемент задан неверно, возможно вы указали не все значения.");
                        return false;
                    }
                } else if (fullCommand[0].equals("import"))
                    return importFile(new File(fullCommand[1].replace(" ", "")));
                else
                    file = new File(fullCommand[1].replace(" ", ""));
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка в формате аргумента");
            return false;
        }
        return true;
    }

    private boolean importFile(File file) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))))) {
            if (!(file.isFile()))
                throw new FileNotFoundException("Это не файл. Добавьте элементы вручную или импортируйте из другого файла");
            if (!(file.exists()))
                throw new FileNotFoundException("Файл не найден. Добавьте элементы вручную или импортируйте из другого файла");
            if (!file.canRead())
                throw new SecurityException("Нет доступа. Добавьте элементы вручную или импортируйте из другого файла");
            String line;
            jsonStr = "";
            while ((line = r.readLine()) != null) jsonStr += line;
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
            return true;
        } catch (NullPointerException | FileNotFoundException | SecurityException ex) {
            System.out.println(ex.getMessage());
            return false;
        } catch (IOException ex) {
            System.out.println("Не удалось считать файл");
            return false;
        }
    }

    private String[] readAndParseCommand() {
        String command;
        String[] fullCommand;
        int count = 0;
        try {
            Scanner consoleScanner = new Scanner(System.in);
            System.out.print("\nВведите команду:\n>");
            command = consoleScanner.nextLine();
            fullCommand = command.trim().split(" ", 2);
            if (fullCommand.length == 1) return fullCommand;
            else if ((fullCommand[0].equals("add_if_max") || fullCommand[0].equals("add") || fullCommand[0].equals("remove"))) {
                fullCommand[1] = fullCommand[1].trim();
                command = fullCommand[1];
                fullCommand[1] = "";
                while (!command.contains("{")) {
                    fullCommand[1] += command;
                    command = consoleScanner.nextLine().trim();
                }
                count += command.replace("{", "").length() - command.replace("}", "").length();
                fullCommand[1] += command;
                while (count != 0) {
                    command = consoleScanner.nextLine();
                    fullCommand[1] += command;
                    count += command.replace("{", "").length() - command.replace("}", "").length();
                }
            } else return fullCommand;
        } catch (NoSuchElementException ex) {
            fullCommand = new String[1];
            fullCommand[0] = "exit";
        }
        return fullCommand;
    }

    private boolean isTimeOut(String answer) {
        if (answer.contains("Ваше время истекло")) {
            exit = true;
            isWorking = false;
            System.out.println(answer + "\nВыход");
            return true;
        }
        return false;
    }
}