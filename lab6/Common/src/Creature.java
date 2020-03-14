import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;

public class Creature implements Comparable<Creature>, Serializable {
    private static final long serialVersionUID = 4520157701042133812L;
    private String name;
    private int hunger;
    private Location location;
    private Date CurrentTime;
    private String Class;

    class Inventory {
        LinkedList<Creature> inventory = new LinkedList<>();
        protected void add(Creature a) {
            inventory.add(a);
            System.out.println(Creature.this.getName() + " поднимает " + a.getName());
        }
        protected void remove(Creature a) {
            inventory.remove(a);
            System.out.println(Creature.this.getName() + " опускает " + a.getName());
        }
    }

    transient Creature.Inventory inventory = new Creature.Inventory();

    Creature(String n) {
        name = n;
        CurrentTime = new Date();
    }

    Creature(String n, Location l){
        name = n;
        location = l;
        CurrentTime = new Date();
    }

    public String getName() {
        return name;
    }
    public int getHunger() {
        return hunger;
    }
    public Location getLocation() {
        return location;
    }
    public Date getCurrentTime() {
        return CurrentTime;
    }
    public void setLocation(Location l) {
        location = l;
    }
    public void setCurrentTime() {
        CurrentTime = new Date();
    }

    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        Creature other = (Creature) otherObject;
        return getName().equals(other.getName()) ;
                /*&& getHunger() == other.getHunger()
                && getLocation() == other.getLocation();*/
    }

    public int hashCode() {
        return Objects.hash(name, Class);
    }

    @Override
    public int compareTo(Creature compared) {
        return this.getName().length() - compared.getName().length();
    }

    public String toString() {
        return "\n\t" + Class +" "+ getName();
    }
}