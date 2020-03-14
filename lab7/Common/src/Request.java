import java.io.File;
import java.io.Serializable;

public class Request implements Serializable {

    String command;
    String login;
    String password;
    String result;
    Karlson karlson;
    File file;
    boolean flag;


    Request(String result,boolean flag){
        this.result=result;
        this.flag=flag;
    }
    Request(String command,String login,String password){
        this.command=command;
        this.login=login;
        this.password=password;
    }
    Request(String command,Karlson karlson,String login,String password){
        this.command=command;
        this.karlson=karlson;
        this.login=login;
        this.password=password;
    }
    Request(String command,File file,String login,String password){
        this.command=command;
        this.file=file;
        this.login=login;
        this.password=password;
    }


    public Request() {

    }
}
