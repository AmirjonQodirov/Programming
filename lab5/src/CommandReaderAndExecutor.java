import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

class CommandReaderAndExecutor {
    private KarlsonCollectionManager collectionManager;
    private boolean needExit;
    CommandReaderAndExecutor(KarlsonCollectionManager collectionManager) {
        this.collectionManager = new KarlsonCollectionManager();
        if (collectionManager != null) this.collectionManager = collectionManager;
        needExit = false;
    }
    public void govern() {
        while (!needExit) {
            String[] fullCommand = readAndParseCommand();
            Karlson forAction = null;
            if ((fullCommand[0].equals("import") || fullCommand[0].equals("insert") || fullCommand[0].equals("remove") || fullCommand[0].equals("remove_lower"))) {
                if (fullCommand.length == 1) {
                    System.out.println("Error, " + fullCommand[0] + " must have argument.");
                    continue;
                }
                if ((fullCommand.length == 2) && !(fullCommand[0].equals("import")) && !(fullCommand[0].equals("remove"))) {
                    try {
                        Gson gson = new Gson();
                        forAction = gson.fromJson(fullCommand[1], Karlson.class);
                        if ((forAction == null) || (forAction.getName() == null) || (forAction.getAge() == null) || (forAction.getPlace() == null)) {
                            System.out.println("Ошибка, элемент задан неверно, возможно вы указали не все значения");
                            continue;
                        }
                    } catch (JsonSyntaxException ex) {
                        System.out.println("Ошибка, элемент задан неверно");
                        continue;
                    }
                }
            }
            switch (fullCommand[0]) {
                case "info":
                    collectionManager.info();
                    break;
                case "insert":
                    collectionManager.put(forAction);
                    break;
                case "remove":
                    collectionManager.remove(fullCommand[1]);
                    break;
                case "help":
                    collectionManager.help();
                    break;
                case "show_karlson":
                    collectionManager.show_karlson();
                    break;
                case "remove_lower":
                    collectionManager.removeLower(forAction);
                    break;
                case "import":
                    collectionManager.doImport(new File(fullCommand[1]));
                    break;
                case "exit":
                    needExit = true;
                    collectionManager.finishWork();
                    break;
                case "show":
                    collectionManager.show();
                    break;
                default:
                    System.out.println("Ошибка, Неизвестная команда");
            }
        }
    }
    private String[] readAndParseCommand() {
            Scanner consoleScanner = new Scanner(System.in);
            String command;
            String[] fullcomand;
            int count = 0;
            try {
                System.out.println("Введите команду");
                command = consoleScanner.nextLine();
                fullcomand = command.trim().split(" ", 2);
                if (fullcomand.length == 1) return fullcomand;
                else if (fullcomand[0].equals("insert") || fullcomand[0].equals("remove_lower")) {
                    fullcomand[1] = fullcomand[1].trim();
                    command = fullcomand[1];
                    fullcomand[1] = "";
                    while (!command.contains("{")) {
                        fullcomand[1] += command;
                        command = consoleScanner.nextLine().trim();
                    }
                    count += command.replace("{", "").length() - command.replace("}", "").length();
                    fullcomand[1] += command;
                    while (count != 0) {
                        command = consoleScanner.nextLine();
                        fullcomand[1] += command;
                        count += command.replace("{", "").length() - command.replace("}", "").length();
                    }
                } else return fullcomand;
            } catch (NoSuchElementException ex) {
                fullcomand = new String[1];
                fullcomand[0] = "exit";
            }
            return fullcomand;
        }
}