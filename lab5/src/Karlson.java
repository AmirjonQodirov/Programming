import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;

public class Karlson implements Comparable<Karlson>{
    public String name;
    public Integer age;
    public Place place;
    public Karlson(String name,Integer age, Place place){
        this.name = name;
        this.age = age;
        this.place = place;
    }
    public String getName(){
        return this.name;
    }
    public  Integer getAge(){
        return this.age;
    }
    public Place getPlace() {
        return place;
    }
    @Override
    public int compareTo(Karlson o) {
        return this.getAge().compareTo(o.getAge());
    }
    static HashMap<String,Karlson> jsonToKarlsonHashMap(String jsonKarlsonHashMap) throws JsonSyntaxMistakeException {
        try {
            Gson gson = new Gson();
            HashMap<String,Karlson> karlsonHashMap = new HashMap<>();
            int noInitializedCount = 0;
            if (jsonKarlsonHashMap.length() != 0) {
                Karlson[] thingsArray = gson.fromJson(jsonKarlsonHashMap, Karlson[].class);
                for (Karlson i : thingsArray) {
                    if ((i != null) && (i.getName() != null) && (i.getName() != null)&& (i.getPlace() != null)) {
                        karlsonHashMap.put(i.getName(),i);
                    }else noInitializedCount++;
                }
            }
            if (noInitializedCount > 0) System.out.println("Найдено " + noInitializedCount + " не полностью инициализированных элементов");
            return karlsonHashMap;
        }catch (JsonSyntaxException ex){
            throw new JsonSyntaxMistakeException();
        }
    }
}