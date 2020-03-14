import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

class RequestsSender extends Thread {

    private boolean isWorking = false;
    private boolean exit = false;
    private final int PORT = 5306;
    private final String HOST = "localhost";
    private final String ADDR = "77.234.214.82";
    private SocketAddress socketAddress;
    private Karlson forAction;
    private File file;
    private CopyOnWriteArraySet<Karlson> karlsons = new CopyOnWriteArraySet<>();
    private int countTryConnect = 0;
    private String[] fullaftercommand = new String[]{};
    private String[] full;

    RequestsSender() {
        this.socketAddress = new InetSocketAddress(HOST, PORT);
    }
    public void run() {
        try {
            System.out.println("Добро пожаловать! ");
            while (!exit) {
                if(countTryConnect==0) System.out.println("Соединение с сервером(IP address: " + ADDR + ", port: " + PORT + ")\n");
                SocketChannel server = connect();
                isWorking = true;
                if (server == null) break;
                try (ObjectOutputStream oos = new ObjectOutputStream(server.socket().getOutputStream());
                     ObjectInputStream ois = new ObjectInputStream(server.socket().getInputStream())) {
                    System.out.println("localport = " + server.socket().getLocalPort());
                    System.out.println("Сервер доступен и готов принимать команды.");
                    oos.writeObject(new Request("info"));
                    System.out.println("Server:\n" + ois.readObject());
                    while (isWorking) {
                        String[] fullCommand;
                        if(fullaftercommand.length == 0){
                            fullCommand = readAndParseCommand();
                        }else{
                            fullCommand = fullaftercommand;
                            fullaftercommand = new String[0]; }
                        if (!parse(fullCommand)) continue;
                        switch (fullCommand[0]) {
                            case "info":
                            case "help":
                            case "show":
                            case "showKarlson":
                            case "clear":
                            case "save":
                                oos.writeObject(new Request(fullCommand[0]));
                                System.out.println("Server:\n" + ois.readObject());
                                break;
                            case "add":
                            case "remove_if_max":
                            case "remove":
                                oos.writeObject(new Request(fullCommand[0], forAction));
                                System.out.println("Server:" + ois.readObject());
                                break;
                            case "load":
                                oos.writeObject(new Request(fullCommand[0], file));
                                System.out.println("Server:\n" + ois.readObject());
                                break;
                            case "import":
                                oos.writeObject(new Request(fullCommand[0], karlsons));
                                System.out.println("Server:\n" + ois.readObject());
                                break;
                            case "exit":
                                isWorking = false;
                                exit = true;
                                oos.writeObject(new Request(fullCommand[0]));
                                System.out.println("Server: \n" + ois.readObject());
                                server.close();
                                break;
                            default:
                                System.out.println("Ошибка, Неизвестная команда\n" +
                                        "Для помощи введите команду help.");
                        }
                    }
                } catch (IOException e) {
                    fullaftercommand = full;
                    if(countTryConnect >4)
                    {
                        System.out.println("Возобновить попытки подключения?(Y/*)");
                        try {Scanner scanner = new Scanner(System.in);
                            String command = scanner.nextLine();
                            if (command.equals("Y")) countTryConnect = 0;
                            else throw new NoSuchElementException();
                        } catch (NoSuchElementException error) {
                            System.out.println("Программа завершена");
                            exit = true;
                            break;
                        }
                    }else {
                        countTryConnect++;
                        if (isWorking) System.out.println("Не удается подключиться к серверу. Ожидайте...");
                        Thread.sleep(2000);
                        isWorking = false;
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
        }catch (Exception e){
            System.out.println(e.getCause().toString());
        }
    }
    private SocketChannel connect() {
        SocketChannel socket;
        try {
            socket = SocketChannel.open(socketAddress);
            isWorking = true;
        } catch (IOException e) {
            if (isWorking) {
                System.out.println("Не удается подключиться к серверу. Ожидайте...");
                isWorking = false;
            }
            while (true) {
                try {
                    countTryConnect++;
                    Thread.sleep(2000);
                    System.out.println("Не удается подключиться к серверу. Ожидайте...");
                    socket = SocketChannel.open(socketAddress);
                    isWorking = true;
                    break;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException ex) {
                    if (countTryConnect > 6) {
                        System.out.println("Возобновить попытки подключения?(Y/*)");
                        try {Scanner scanner = new Scanner(System.in);
                            String command = scanner.nextLine();
                            if (command.equals("Y")) countTryConnect = 0;
                            else throw new NoSuchElementException();
                        } catch (NoSuchElementException error) {
                            System.out.println("Программа завершена");
                            return null;
                        }
                    }
                }
            }
        }
        return socket;
    }
    private boolean parse(String[] fullCommand) {
        try {
            if ((fullCommand[0].equals("remove_if_max") || fullCommand[0].equals("add") || fullCommand[0].equals("remove")
                    || fullCommand[0].equals("load")) || fullCommand[0].equals("import")) {
                if (fullCommand.length == 1) {
                    System.out.println("Ошибка, " + fullCommand[0] + " без аргумента.");
                    return false;
                } else if (fullCommand[0].equals("remove_if_max") || fullCommand[0].equals("add")
                        || fullCommand[0].equals("remove")) {
                    Gson gson = new Gson();
                    String jsonStr = fullCommand[1];
                    jsonStr = jsonStr.replace(" ", "");
                        forAction = gson.fromJson(jsonStr, Karlson.class);
                    if ((forAction == null) || (forAction.getName() == null) || (forAction.getAge() == null) || (forAction.getLocation() == null)) {
                        System.out.println("Ошибка, элемент задан неверно, возможно вы указали не все её значения.");
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
                throw new SecurityException("Нет достуа для чтения. Добавьте элементы вручную или импортируйте из другого файла");
            String jsonStr = "";
            String line ="";
            while ((line = r.readLine()) != null) jsonStr += line;
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
            System.out.println("Файл клиента успешно считан");
            return parser((jsonStr).split("},\\{"));
        } catch (NullPointerException | FileNotFoundException | SecurityException ex) {
            System.out.println(ex.getMessage());
            return false;
        } catch (IOException ex) {
            System.out.println("Не удалось считать файл");
            return false;
        }
    }
    private boolean parser(String[] line) throws JsonSyntaxException{
        CopyOnWriteArrayList<Karlson> BeginKarlson = new CopyOnWriteArrayList<>();
        boolean oneParse = false;
        if (line.length==1) oneParse = true;
        int noInit=0;
        int count =-1;
        Gson gson = new Gson();
        for (int i = 0; i < line.length; i++) {
            if(i==0&&!oneParse) line[i] = line[i] + "}";
            else if(i==line.length-1&&!oneParse) line[i] = "{" + line[i];
            else if(line.length> 1)line[i] = "{" + line[i] + "}";
            if (line[i].equals("")){
                continue;
            }else if(line[i].contains("\"name\":\"")){
                count++;
                BeginKarlson.add(gson.fromJson(line[i], Karlson.class));
            }else {
                noInit++;
                continue;
            }
            if (count >= 0 && BeginKarlson.get(count).getName() != null && !BeginKarlson.get(count).getName().equals("")&& !BeginKarlson.get(count).getName().trim().equals(""))
                karlsons.add(BeginKarlson.get(count));
        }
        karlsons = karlsons.stream().sorted(Karlson::compareTo).collect(Collectors.toCollection(CopyOnWriteArraySet::new));
        int finalCount = ++count;
        System.out.println("Удачно инициализированно " + finalCount + " объектов, неудачно " + noInit  + "\n");
        return true;
    }
    private String[] readAndParseCommand() {
        Scanner consoleScanner = new Scanner(System.in);
        String command;
        String[] fullCommand;
        int count = 0;
        try {
            System.out.print("\nВведите команду:\n>");
            command = consoleScanner.nextLine();
            fullCommand = command.trim().split(" ", 2);
            full = fullCommand;
            if (fullCommand.length == 1) return fullCommand;
            else if ((fullCommand[0].equals("remove_if_max") || fullCommand[0].equals("add")) || fullCommand[0].equals("remove")) {
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

}