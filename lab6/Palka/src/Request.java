import java.io.File;
import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Request implements Serializable{

    public String command;
    public Karlson karlson;
    public File file;
    public CopyOnWriteArraySet<Karlson> karlsons;

    public Request(String command) {
        this.command = command;
    }
    public Request(String command, Karlson karlson){
        this.command = command;
        this.karlson = karlson;
    }
    public Request(String command, File file){
        this.command = command;
        this.file = file;
    }
    public Request(String command, CopyOnWriteArraySet<Karlson> karlsons){
        this.command = command;
        this.karlsons = karlsons;
    }

}