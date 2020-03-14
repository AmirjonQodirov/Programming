import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;

class CollectionManager {

    private File importFile;
    private CopyOnWriteArraySet<Karlson> Karlsons;
    private DataBaseManager DBman;
    private String initTime;

    CollectionManager(File file, DataBaseManager DBman, Receiver receiver) {
        importFile = file;
        this.DBman = DBman;
        Karlsons = DBman.synchronize(receiver);
        initTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ssX"));
    }

    boolean loadFile(File file, Receiver receiver,String login, String password) {
        try {
            if (file == null)
                throw new NullPointerException("Вместо файла передано ничего. Добавьте элементы вручную или импортируйте из другого файла");
            if (!(file.isFile()))
                throw new FileNotFoundException("Это не файл. Добавьте элементы вручную или импортируйте из другого файла");
            if (!(file.exists()))
                throw new FileNotFoundException("Файл не найден. Добавьте элементы вручную или импортируйте из другого файла");
            if (!file.canRead())
                throw new SecurityException("НЕТ доступа. Добавьте элементы вручную или импортируйте из другого файла");
            String JsonString = readFromFile(file, receiver);
            return load(JsonString, receiver,login, password);
        } catch (NullPointerException | FileNotFoundException | SecurityException ex) {
            receiver.add(ex.getMessage());
            return false;
        } catch (IOException ex) {
            receiver.add("Произошла ошибка при чтении с файла.");
            return false;
        }
    }

    boolean load(String JsonString, Receiver receiver, String login, String password) {
        try {
            parser(JsonString.split("},\\{"), receiver, login, password);
            return true;
        } catch (JsonSyntaxException ex) {
            receiver.add("JSON строки исписаны неразборчивым подчерком");
            return false;
        }
    }

    private String readFromFile(File file, Receiver receiver) throws IOException {
        String jsonStr = "";
        BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
        String line;
        while ((line = r.readLine()) != null) jsonStr += line;
        jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
        receiver.add("\nФайл сервера успешно считан");
        return jsonStr;
    }

    private void parser(String[] line, Receiver receiver, String login, String password) throws JsonSyntaxException {
        boolean oneParse = false;
        if (line.length == 1) oneParse = true;
        int noInit = 0;
        int added = 0;
        int count = -1;
        Gson gson = new Gson();
        for (int i = 0; i < line.length; i++) {
            if (i == 0 && !oneParse) line[i] = line[i] + "}";
            else if (i == line.length - 1 && !oneParse) line[i] = "{" + line[i];
            else if (line.length > 1) line[i] = "{" + line[i] + "}";
            if (line[i].equals("")) {
            } else if ((line[i].contains("\"name\""))) {
                count++;
                Karlson forAction = gson.fromJson(line[i], Karlson.class);
                if (add(forAction, receiver, login, password)) added++;
            } else noInit++;
        }
        int finalCount = ++count;
        receiver.add("\nУдачно инициализированно " + finalCount + " объектов, неудачно " + noInit + ",\nДобавлено " + added + ".\n");
    }

    void remove(Karlson forAction, Receiver receiver, String login, String password) {
        ZonedDateTime zonedDateTime = DBman.getTime(login,password,forAction);
        if (DBman.removeKarlson(forAction, receiver, login, password)) {
            Karlsons.remove(forAction);
            receiver.add(forAction.toString().replace("\n", "")+ "\n" + "time: " + zonedDateTime + "\n" + " удалён.");
        }
    }

    void addIfMax(Karlson forAction, Receiver receiver, String login, String password) {
        ZonedDateTime zonedDateTime = DBman.getTime(login,password,forAction);
        if (DBman.addIfMax(forAction, receiver, login, password)) {
            Karlsons.add(forAction);
            receiver.add((forAction.toString().replace("\n", "")+ "\n" + "time: " + zonedDateTime + "\n" + " добавлен, т.к. является наибольшим"));
        } else
            receiver.add(forAction.toString().replace("\n", "")+ "\n" + "time: " + zonedDateTime + "\n" + " не является наибольшим");
    }

    boolean add(Karlson forAction, Receiver receiver, String login, String password) {
        if (DBman.addKarlson(forAction, receiver, login, password)) {
            Karlsons.add(forAction);
            ZonedDateTime zonedDateTime = DBman.getTime(login,password,forAction);
            receiver.add(forAction.toString() + "\n" + "time: " + zonedDateTime + "\n"  + " добавлен");
            return true;
        }
        return false;
    }

    void info(Receiver receiver) {
        DBman.info(receiver);
        receiver.add("\nКоллекция типа " + Karlsons.getClass().getSimpleName() + " содержит объекты класса Карлсон" +
                "\nДата инициализации:  " + initTime +
                "\nСейчас содержит " + Karlsons.size() + " оьъектов" +
                "\nДля помощи введите команду help.");
    }

    void help(Receiver receiver) {
        receiver.add("add {element}: добавить объект;\n" +
                "remove {element}: удалить объект;\n" +
                "add_if_max {element}: добавить объект, если его возраст больше остальных;\n" +
                "show: показать текущих объектов;\n" +
                "clear: удалить всех объектов(или нет);\n" +
                "info: вывести информацию о базе данных и коллекции;\n" +
                "load путь_к_файлу: загрузить объект из файла сервера;\n" +
                "import путь_к_файлу: загрузить объект из файла клиента;\n" +
                "save: сохранить объект в файл на сервере;\n" +
                "exit: завершить работу;\n" +
                "help: вывести помощь по всем командам.");
    }

    synchronized void save(Receiver receiver) {
        File saveFile = importFile;
        Gson gson = new Gson();
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(saveFile, false))) {

            osw.write(gson.toJson(Karlsons));
            osw.flush();
            receiver.add("объекты сохранены в файл сервера " + saveFile.getAbsolutePath());
        } catch (IOException | NullPointerException e) {
            Date d = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("MM.dd_hh:mm:ss");
            saveFile = new File("saveFile" + formater.format(d) + ".json");
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(saveFile, true))) {
                osw.write(gson.toJson(Karlsons));
                osw.flush();
                receiver.add("объекты сохранены в файл " + saveFile.getAbsolutePath());
            } catch (IOException ex) {
                receiver.add("Сохранение прошло неудачно");
            }
        }
    }

    void show(Receiver receiver) {
        receiver.add(Karlsons.toString());
    }

    void clear(Receiver receiver, String login, String password) {
        if (DBman.clearKarlson(receiver, login, password))
            Karlsons.clear();
        Karlsons = DBman.synchronize(receiver);
    }
}