import java.io.*;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.*;

public class KarlsonCollectionManager {
private HashMap<String,Karlson> karlsonHashMap;
    private Date initDate;
    private File fileForIO;
    public KarlsonCollectionManager(File collectionFile) {
        fileForIO = null;
        this.initDate = new Date();
        karlsonHashMap = new HashMap<String,Karlson>();
        if(collectionFile != null) {
            doImport(collectionFile);
            fileForIO = collectionFile;
        }
    }
    public KarlsonCollectionManager() {
        fileForIO = null;
        this.initDate = new Date();
        karlsonHashMap = new HashMap<String,Karlson>();
    }
    /**
     * Метод выводит на экран информацию о коллекции.
     */
    public void info() {
        System.out.println("Коллекция типа HashMap и содержит из объектов класса Карлсон." + "\n" + "Keys : Karlson's name" + "\n" + "Values : Karlson's Objects");
        System.out.println("Дата иннициализации" + initDate);
        System.out.println("В коллекции " + karlsonHashMap.size() + " элементов.");
    }
    /**
     * Метод удалияет элемент из коллекции по его ключу
     * @param karl : (String) - Name of Karlson Object
     */
    public void remove(String karl) {
        if (karlsonHashMap.remove(karl) != karlsonHashMap.get(karl)) {
            System.out.println("Элемент удалён");
        } else System.out.println("Коллекция не содержит данный элемент");
    }
    /**
     * Метод удалияет из коллекции все элементы, меньшие, чем заданный.
     * @param karlsonForCompare (Karlson) - Объект класса Карлсон.
     */
    public void removeLower(Karlson karlsonForCompare) {
        HashSet<Karlson> hsk = new HashSet<>(karlsonHashMap.values());
        Iterator<Karlson> iterator = hsk.iterator();

        int count = 0;
        while (iterator.hasNext()) {
            Karlson anotherKarlson = iterator.next();
            if (karlsonForCompare.getAge().compareTo(anotherKarlson.getAge()) > 0) {
                karlsonHashMap.remove(anotherKarlson.getName());
                count++;
            }
        }
        System.out.println("Удалено " + count + " элементов");
    }
    /**
     * Метод добавляет объект Карлсона в Коллекцию.
     * @param karlson : (Karlson) - Объект класса Карлсон.
     */
    public void put(Karlson karlson) {
        if (karlsonHashMap.put(karlson.name,karlson) == karlsonHashMap.get(karlson)) {
            System.out.println("Элемент добавлен");
        } else System.out.println("Коллекция уже содержит данный элемент");
    }
    /**
     * Метод добавить в коллекцию все данные из файла.
     * @param importFile:(java.io.File) - файл для чтения.
     */
    public void doImport(File importFile) {
        try{
            if(!(importFile.isFile())) throw new FileNotFoundException("Ошибка. Указаный путь не ведёт к файлу");
            if (!(importFile.exists()))
                throw new FileNotFoundException("Фаил коллекцией не найден. Добавьте элементы вручную или импортируйте из другого файла");
            if (!importFile.canRead()) throw new SecurityException("Доступ запрещён. Файл защищен от чтения");
            String JsonString = readJsonFromFile(importFile);
            if (!(Karlson.jsonToKarlsonHashMap(JsonString).isEmpty())){
                karlsonHashMap.putAll(Karlson.jsonToKarlsonHashMap(JsonString));
                System.out.println("Добавлены только полностью инициализированные элементы");
            }else System.out.println("Ничего не добавлено, возможно импортируемая коллекция пуста, или элементы заданы неверно");
        }catch (FileNotFoundException | SecurityException ex){
            System.out.println(ex.getMessage());
        } catch (IOException ex){
            System.out.println("Непредвиденная ошибка ввода: " + ex);
        }catch (JsonSyntaxMistakeException ex){
            System.out.println("Содержимое фаила имеет неверный формат, проверьте синтаксис он должен удовлетворять формату json, а после используйте команду import {Path} для повторной ошибки");
        }
    }
    /**
     * @param fileForRead:(java.io.File) - файл для чтения.
     * @return string in format json (serialized object).
     * @throws IOException - throws Input-Output Exceptions.
     */
    private String readJsonFromFile(File fileForRead) throws IOException {
        try(
                FileInputStream fileInpStream = new FileInputStream(fileForRead);
                BufferedInputStream buffInpStream = new BufferedInputStream(fileInpStream)) {
            LinkedList<Byte> collectionBytesList = new LinkedList<>();
            while (buffInpStream.available() > 0) {
                collectionBytesList.add((byte) buffInpStream.read());
            }
            char[] collectionChars = new char[collectionBytesList.size()];
            for (int i = 0; i < collectionChars.length; i++) {
                collectionChars[i] = (char) (byte) collectionBytesList.get(i);
            }
            return new String(collectionChars);
        }
    }
    /**
     * Завершает работу с коллекцией элементов, сохраняя ее в фаил из которого она была считана.
     * Если сохранение в исходный фаил не удалось, то сохранение происходит в фаил с уникальным названием.
     */
    public void finishWork() {
        File saveFile = (fileForIO != null) ? fileForIO : new File("");
        Gson gson = new Gson();
        try{
            String jsonstring = readJsonFromFile(saveFile);
            if(!Karlson.jsonToKarlsonHashMap(jsonstring).isEmpty()){
                try {
                    BufferedWriter buffOutStr = new BufferedWriter(new FileWriter(saveFile));
                    buffOutStr.write(gson.toJson(karlsonHashMap));
                    buffOutStr.flush();
                    System.out.println("Коллекция сохранена в файл " + saveFile.getAbsolutePath());
                }catch (IOException e){
                    System.out.println("Сохранение коллекции не удалось");
                }
            } else
                System.out.println("Сохранение коллекции не удалось");

        }catch (IOException | NullPointerException e){
            Date d = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
            saveFile = new File("saveFile" + formater.format(d) + ".txt");
            try(BufferedWriter buffOutStr = new BufferedWriter(new FileWriter(saveFile))) {
                if (saveFile.createNewFile()) throw new IOException();
                buffOutStr.write(gson.toJson(karlsonHashMap));
                buffOutStr.flush();
                System.out.println("Коллекция сохранена в файл " + saveFile.getAbsolutePath());
            }catch (IOException ex){
                System.out.println("Сохранение коллекции не удалось");
            }
        } catch (JsonSyntaxMistakeException e) {
            System.out.println("Сохранение коллекции не удалось");
        }
    }

    /**
     * Выводит на экран количество элементов массива(только ключи).
     */
    public void show(){
        System.out.println("В колекции: " + karlsonHashMap.size() + " элемента");
        System.out.println(karlsonHashMap.keySet());
    }

    /**
     * Выводит на экран все команды.
     */
    public void help(){
        System.out.println("Команды:");
        System.out.println("insert {element} ----- добавить новый элемент");
        System.out.println("show ----- вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        System.out.println("remove_lower {element} ----- удалить из коллекции все элементы, меньшие, чем заданный");
        System.out.println("remove String_key ----- удалить элемент из коллекции по его ключу");
        System.out.println("info ----- вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        System.out.println("import String_path ----- добавить в коллекцию все данные из файла");
    }
            /**
     * Выводит на экран изображение Карлсона.
      */
    public void show_karlson(){
        System.out.println("________¶¶¶¶¶¶ \n" +
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
                "__________________________¶¶¶¶¶¶¶¶¶¶¶¶ ");
    }
}