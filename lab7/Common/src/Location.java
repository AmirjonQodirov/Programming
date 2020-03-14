import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {

    Integer x;
    Integer y;
    Integer z;

    public Location(int x, int y, int z) {
        this.z=z;
        this.x=x;
        this.y=y;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return x.equals(location.x) &&
                y.equals(location.y) &&
                z.equals(location.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + '}';
    }
}
