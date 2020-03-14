import java.io.File;

public class Main {
    public static void main(String[] args) {
        CommandReaderAndExecutor readerAndExecutor = new CommandReaderAndExecutor(new KarlsonCollectionManager(getFileFromEnvironmentVariable()));
        readerAndExecutor.govern();
    }
    private static File getFileFromEnvironmentVariable() {
        String collectionPath = System.getenv("Karlson_PATH");
        if (collectionPath == null) {
            System.out.println("Путь должен передаваться через переменную окружения Karlson_PATH");
            return null;
        }else{
            return new File(collectionPath);
        }
    }
}