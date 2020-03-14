import java.time.LocalTime;

class Receiver {
    private String message = "";
    private int id;

    Receiver(int id) {
        this.id = id;
    }


    String get() {
        String str = message;
        message = "";
        return str;
    }

    void addFromServer(String additive) {
        String time = LocalTime.now().withNano(0).toString();
        if (message.equals("")) message = time + " " + additive;
        else message = message + "\n" + time + " " + additive;
    }

    void add(String additive) {
        if (message.equals("")) message += additive;
        else message = message + "\n" + additive;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        Receiver other = (Receiver) otherObject;
        return id == other.id;
    }
}