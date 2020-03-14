import java.io.Serializable;
import java.util.Objects;

public class Karlson implements Comparable<Karlson> , Serializable {

private String name;
private Integer age;
private Location location;

    public Karlson(String name, Integer age, Location location) {
        this.name = name;
        this.age = age;
        this.location = location;
    }
    public String getName() {
        return name;
    }
    public Integer getAge() {
        return age;
    }
    public Location getLocation() {
        return location;
    }
    public String toString() {
        return '{'+"name: " + name  + ", age: " + age + ", location: " + location.toString() + '}';
    }
    @Override
    public int compareTo(Karlson o) {
        return o.getLocation().distances() - this.getLocation().distances();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Karlson karlson = (Karlson) o;
        return name.equals(karlson.name) &&
                age.equals(karlson.age) &&
                location.equals(karlson.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, location);
    }
}