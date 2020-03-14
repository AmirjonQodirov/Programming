import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CollectionManager {

    private File fileimoport;
    private CopyOnWriteArraySet<Karlson> Karlsons;
    private Date date;
    private boolean exit;
    private String receiver = "" ;

    CollectionManager(File file) {
        fileimoport = file;
        exit = true;
        Karlsons = new CopyOnWriteArraySet<>();
        date = new Date();
    }
    boolean loadFile(File file){
        try {
            if (file == null)
                throw new NullPointerException("Вместо файла передано ничего. Добавьте элементы вручную или импортируйте из другого файла");
            if (!(file.isFile()))
                throw new FileNotFoundException("Это не файл. Добавьте элементы вручную или импортируйте из другого файла");
            if (!(file.exists()))
                throw new FileNotFoundException("Файл не найден. Добавьте элементы вручную или импортируйте из другого файла");
            if (!file.canRead())
                throw new SecurityException("Нет досткпа для чтения. Добавьте элементы вручную или импортируйте из другого файла");
            String JsonString = readFromFile(file);
            receiver = "Файл сервера считан\n";
            return load(JsonString);
        }catch(NullPointerException | FileNotFoundException | SecurityException ex){
            receiver = ex.getMessage();
            return false;
        } catch (IOException ex){
            receiver = "Произошла ошибка при чтении с файла.";
            return false;
        }
    }
    String load(File JsonString){
        try {
            Karlsons = parser(JsonString.split("},\\{"));
            File file = null;
            if(!file.canWrite()) {
                System.out.println();
            FileInputStream
            }
            return
        }catch (JsonSyntaxException ex) {
            receiver = "JSON строки исписаны неправильно!";
            return false;
        }
    }
    private String readFromFile(File file) throws  IOException{
        String jsonStr = "";
        BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
        String line;
        while ((line = r.readLine()) != null) jsonStr += line;
        jsonStr = jsonStr.substring(1, jsonStr.length()-1);
        receiver += "\nФайл успешно считан";
        return jsonStr;
    }
    private CopyOnWriteArraySet<Karlson> parser(String[] line) throws JsonSyntaxException{
        ArrayList<Karlson> BeginKarlsons = new ArrayList<>();
        CopyOnWriteArrayList<Karlson> FinalKarlsons = new CopyOnWriteArrayList<>();
        CopyOnWriteArraySet<Karlson> FinalKArlsons2 =new CopyOnWriteArraySet<>();
        boolean oneParse = false;
        if (line.length==1) oneParse = true;
        int beginCountKarlson = FinalKarlsons.size();
        int noInit=0;
        int count =-1;
        Gson gson = new Gson();
        for (int i = 0; i < line.length; i++) {
            if(i==0&&!oneParse) line[i] = line[i] + "}";
            else if(i==line.length-1&&!oneParse) line[i] = "{" + line[i];
            else if(line.length> 1)line[i] = "{" + line[i] + "}";
            if (line[i].equals("")){
                continue;
            }else if (line[i].contains("\"name\":")) {
                count++;
                BeginKarlsons.add(gson.fromJson(line[i], Karlson.class));
            }else {
                noInit++;
                continue;
            }
            if (count >= 0 && BeginKarlsons.get(count).getName() != null && BeginKarlsons.get(count).getAge() != null && BeginKarlsons.get(count).getLocation() != null && !BeginKarlsons.get(count).getName().equals("")&& !BeginKarlsons.get(count).getName().trim().equals(""))
                FinalKarlsons.add(BeginKarlsons.get(count));
        }
        int finalCount = ++count;
        int addCountKarlsons = FinalKarlsons.size() - beginCountKarlson;
        receiver += ("Удачно инициализированно " + finalCount + " объектов, неудачно " + noInit +
                "\nВ коллекцию добавлено " + addCountKarlsons + " из них.\n");
        FinalKArlsons2.addAll(FinalKarlsons);
        return FinalKArlsons2;
    }
    String add(Karlson forAction){
        if(Karlsons.add(forAction)) {
            return forAction.toString() + " добавлен в коллекцию";
        }else
            return forAction.toString() + " не добавлен в коллекцию";
        }
    String add(CopyOnWriteArraySet<Karlson> Karlsons){
        this.Karlsons.addAll(Karlsons);
        return "В коллекцию добалено " + Karlsons.size() + " объектов" ;
    }
    String info() {
        return("Коллекция типа " + Karlsons.getClass().getSimpleName() + " содержит объекты класса Karlson" +
                "\nДата инициализации:  " + date +
                "\nСейчас содержит " + Karlsons.size() + " объектов" +
                "\n\nДля помощи введите команду help.");
    }
    String help(){
        return("add {element}: добавить новый объкт в коллекцию;\n" +
                "remove {element}: удалить объкт из коллекции;\n" +
                "remove_if_max {element}: удалить объкт из коллекции;\n" +
                "show: вывести в стандарный поток вывода все элементы коллекции в строковом представлении;\n" +
                "*(new) showKarlson: вывести в стандарный поток вывода Карлсона."+
                "clear: очистить коллекцию;\n" +
                "info: вывести информацию о коллекции (тип, дата инициализации, количество элементов);\n" +
                "load путь_к_файлу: считать коллекцию из файла сервера;\n" +
                "import путь_к_файлу: считать коллекцию из файла клиента;\n" +
                "save: сохранить коллекцию в файл на сервере;\n" +
                "exit: завершает работу клиента;\n" +
                "help: вывести помощь по всем командам.");
    }
    synchronized String save() {
        File saveFile = fileimoport;
        Gson gson = new Gson();
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(saveFile, false))) {
            osw.write(gson.toJson(Karlsons));
            osw.flush();
            return ("Коллекция сохранена в файл сервера " + saveFile.getAbsolutePath());
        } catch (IOException | NullPointerException e) {
            Date d = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("MM.dd_hh:mm:ss");
            saveFile = new File("saveFile" + formater.format(d) + ".json");
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(saveFile, true))) {
                osw.write(gson.toJson(Karlsons));
                osw.flush();
                return ("Коллекция сохранена в файл " + saveFile.getAbsolutePath());
            } catch (IOException ex) {
                return ("Сохранение коллекции не удалось");
            }
        }
    }
    String getReceiver() {
        String str = receiver;
        receiver = "";
        return str;
    }
    String show() {
        return Karlsons.toString();
    }
    String showKarlson(){
        return "________¶¶¶¶¶¶ \n" +
                "______¶¶¶¶¶1¶¶¶¶¶ \n" +
                "___¶¶¶1111111111¶¶¶ \n" +
                "_¶¶¶11111111111111¶¶¶ \n" +
                "__¶11111111111111111¶¶ \n" +
                "__¶1111111111111111111¶________¶¶¶¶ \n" +
                "__¶11111¶11111111111111¶______¶____¶ \n" +
                "__¶111111¶¶1111111111111¶_____¶_____¶ \n" +
                "__¶¶¶¶11¶__¶¶¶¶1111111111¶____¶______¶ \n" +
                "____¶_¶1¶______¶¶¶¶¶¶1111¶¶___¶______¶¶ \n" +
                "_______¶____¶¶_______¶¶¶¶__¶¶__¶______¶¶¶¶ \n" +
                "_______¶___¶_¶___________¶¶1¶¶¶¶_______¶_____¶¶¶¶ \n" +
                "_______¶___¶¶¶___________¶11¶___¶¶¶¶____¶___¶____¶ \n" +
                "______¶_________¶¶¶_____¶11¶_____¶__¶___¶_¶¶_____¶ \n" +
                "______¶______¶¶¶_¶¶_____¶1¶______¶__¶¶__¶________¶ \n" +
                "_______¶___¶¶¶__¶¶¶____¶¶1¶_____¶_____¶_¶¶_______¶$\n" +
                "________¶¶____¶¶____¶_¶11¶_____¶____¶___¶¶¶¶__¶¶¶ \n" +
                "__________¶_______¶¶__¶11¶_¶__¶____¶____¶___¶¶ \n" +
                "_________¶¶¶¶¶¶¶¶¶___¶111¶__¶¶____¶_____¶_¶¶ \n" +
                "_¶¶_____¶¶¶¶_________¶111¶___¶¶__¶______¶¶¶¶¶¶ \n" +
                "_¶_¶¶¶¶¶¶_¶__________¶111¶_____¶¶____¶___¶__¶_¶ \n" +
                "_¶________¶_________¶1111¶_______¶____¶¶_¶___¶_¶ \n" +
                "__¶¶__¶¶_¶_________¶_¶¶11¶__¶¶¶¶¶¶_______¶¶_____¶ \n" +
                "_¶_¶_¶___¶_______¶¶¶¶__¶¶¶¶¶111111¶¶¶¶_¶¶¶¶______¶ \n" +
                "¶__¶¶____¶______¶11¶¶__¶1111111111111¶¶111¶¶_____¶ \n" +
                "_¶¶__¶¶¶¶_¶____¶1111¶¶¶_¶11111111111111111¶¶_____¶ \n" +
                "__________¶¶¶¶¶111111¶¶¶111111111111111111¶_¶___¶ \n" +
                "___________¶111111111111111111111111111111¶___¶¶ \n" +
                "___¶¶¶¶¶____¶11111111111111111111111111111¶ \n" +
                "__¶____¶¶____¶¶111111111111111111111111111¶ \n" +
                "__¶____¶_¶_¶¶¶_¶1111111111111111111111111¶ \n" +
                "__¶_______¶_____¶11111111111111111111111¶ \n" +
                "__¶¶__________¶¶¶¶11111111111111111111¶¶¶\n" +
                "___¶¶¶_____¶¶¶¶__¶¶¶111111111111¶¶¶¶¶¶¶ \n" +
                "_____¶¶____¶¶¶_____¶¶¶¶¶¶¶¶¶¶¶¶¶¶_____¶\n" +
                "______¶¶¶¶___¶__________________¶_____¶\n" +
                "_________¶¶_¶¶__________________¶_____¶\n" +
                "__________¶¶¶________________¶¶¶¶______¶¶ \n" +
                "_________________________¶¶¶¶¶___________¶\n" +
                "_________________________¶______________¶¶\n" +
                "_________________________¶____________¶¶¶ \n" +
                "__________________________¶¶¶¶¶¶¶¶¶¶¶¶ ";
    }
    boolean isExit() {
        return exit;
    }
    void trueExit(){
        exit = true;
    }
    String clear() {
        Karlsons.clear();
        return "Коллекция очищена";
    }
    String remove(Karlson karl) {
        if(Karlsons.remove(karl))
            return karl.toString() + " удалён из коллекции.";
        else return karl.toString() + " в коллекции не было.";
    }
    String removeIfMax(Karlson forAction) {
        if (Karlsons.contains(forAction)){
            ArrayList<Karlson> list = new ArrayList<Karlson>();
            list.addAll(Karlsons);
            Collections.sort(list);
            if(list.get(0).equals(forAction)){
                Karlsons.remove(forAction);
                return forAction.toString() + " удалён т.к. является наибольшим";
            }else{
                return forAction.toString() + " не является наибольшим элементом коллекции";
            }
        }else {
            return forAction.toString() + " вне колекции";
        }
    }

}