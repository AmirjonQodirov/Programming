import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Karlson implements Serializable,Comparable<Karlson> {

    private String name;
    private Integer age;
    private ZonedDateTime time;
    private Location location;


    public Karlson(String name, Integer age, ZonedDateTime time) {
        this.time = time;
        this.name = name;
        this.age = age;
    }
    public Karlson(String name,Integer age){
        this.name=name;
        this.age=age;
        this.time= ZonedDateTime.now();
    }

    @Override
    public String toString() {
        return "{ name: " + name + ", age: "+age+ ", location: "+location +" }";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Karlson karlson = (Karlson) o;
        return name.equals(karlson.name) &&
                age.equals(karlson.age) &&
                Objects.equals(time, karlson.time) &&
                location.equals(karlson.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, time, location);
    }

    @Override
    public int compareTo(Karlson o) {
        return this.getAge()-o.getAge();
    }
}
