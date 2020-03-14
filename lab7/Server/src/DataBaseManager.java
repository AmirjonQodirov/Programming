import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class DataBaseManager {
    private final String URL = "jdbc:postgresql://localhost:5432/laba7";
    private final String LOGIN = "postgres";
    private final String PASSWORD = "postgres";
    private PasswordRandom passwordRandom;

    DataBaseManager() {
        passwordRandom = new PasswordRandom();
    }

    private String sqlException(String exception) {
        String[] message = exception.split("Подробности: ");
        if (message.length > 1) return "ОШИБКА:" + message[1];
        else return message[0];
    }

    private Long getKarlsonId(String name, int age, Long user_id) throws SQLException {
        String query = "SELECT karlson_id from Karlsons where name = ? AND age = ? AND user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, name);
            pst.setInt(2, age);
            pst.setLong(3, user_id);
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private Karlson initFromDataBase(String name, int age, Timestamp time, Long userId) throws SQLException {
        Location location;
        ZonedDateTime ztime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.of("UTC"));
        Karlson karlson = new Karlson(name, age, ztime);
        Long karlson_id = getKarlsonId(name, age, userId);
        String query1 = "SELECT x,y,z from Locations where karlson_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query1)) {
            pst.setLong(1, karlson_id);
            pst.execute();
            try (ResultSet rs1 = pst.getResultSet()) {
                if (rs1.isBeforeFirst()) {
                    while (rs1.next()) {
                        int x = rs1.getInt(1);
                        int y = rs1.getInt(2);
                        int z = rs1.getInt(3);
                        location = new Location(x, y, z);
                        karlson.setLocation(location);
                    }
                }
            }
        }
        return karlson;
    }

    CopyOnWriteArraySet<Karlson> synchronize(Receiver receiver) {
        String query = "SELECT * from Karlsons";
        CopyOnWriteArraySet<Karlson> Karlsons;
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                Karlsons = new CopyOnWriteArraySet<>();
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        String name = rs.getString(1);
                        int age = rs.getInt(2);
                        Timestamp time = rs.getTimestamp(3);
                        Long userId = rs.getLong(4);
                        Karlsons.add(initFromDataBase(name, age, time, userId));
                    }
                }
            }
            return Karlsons;
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return new CopyOnWriteArraySet<>();
        }
    }

    private String generate(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD2");
        } catch (NoSuchAlgorithmException e) {
            return "Ошибка при хеширование";
        }
        byte[] messageDigest = md.digest(password.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    private Long getUserId(String login, String password) throws SQLException {
        if (password == null) return null;
        if (login == null) return null;
        String query = "SELECT user_id from Users where login = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, login);
            pst.setString(2, generate(password));
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    boolean checkLogin(String login, Receiver receiver) {
        String query = "SELECT count(*) From Users where login = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, login);
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                int count = rs.getInt(1);
                return count <= 0;
            }
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    ZonedDateTime getTime(String login,String password,Karlson karlson) {
        String query = "SELECT time from Karlsons where name = ? AND age = ? AND user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, karlson.getName());
            pst.setInt(2, karlson.getAge());
            pst.setLong(3, getUserId(login, password));
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                Timestamp timestamp =  rs.getTimestamp(1);
                return timestamp.toInstant().atZone(ZoneId.of("Europe/Moscow"));
            }
        } catch (SQLException e) {
            return ZonedDateTime.now();
        }
    }

    boolean checkPassword(String login, String password, Receiver receiver) {
        String query = "SELECT count(*) From Users where login = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, login);
            pst.setString(2, generate(password));
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    boolean sendMessage(String login, String password, Receiver receiver) {
        EmailSender emailSender = new EmailSender();
        String k = emailSender.send(login, password);
        if (k.equals("Пароль отправлен."))
            return true;
        else receiver.add("Ощибка при отправке отключён интернет или неверный почтовый адрес");
        return false;
    }

    boolean sendPassword(String login, Receiver receiver) {
        String password = passwordRandom.nextString();
        if (sendMessage(login, password, receiver)) {
            String query = "INSERT INTO Users(login, password) VALUES(?, ?)";
            try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
                 PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setString(1, login);
                pst.setString(2, generate(password));
                int row = pst.executeUpdate();
                return row > 0;
            } catch (SQLException e) {
                receiver.add(sqlException(e.getLocalizedMessage()));
                return false;
            }
        }
        return false;
    }

    boolean logIn(String login, String password, Receiver receiver) {
        String query = "SELECT password from Users where login = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, login);
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                rs.next();
                if (generate(password).equals(rs.getString(1))) {
                    return true;
                } else {
                    receiver.add("Пароль неверный");
                    return false;
                }
            }
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    boolean signUp(String login, String password, Receiver receiver) {
        String query = "UPDATE Users SET password = ? WHERE login = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, generate(password));
            pst.setString(2, login);
            int row = pst.executeUpdate();
            return row > 0;
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    void info(Receiver receiver) {
        String query = "SELECT pg_size_pretty(pg_database_size(current_database()));"
                + "Select current_database();"
                + "SELECT COUNT(*) FROM Karlsons";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.execute();
            boolean isMoreResult = false;
            String size = null;
            String name = null;
            int count;
            do {
                try (ResultSet rs = pst.getResultSet()) {
                    if (!isMoreResult) {
                        rs.next();
                        size = rs.getString(1);
                    } else if (name == null) {
                        rs.next();
                        name = rs.getString(1);
                        receiver.add("Размер базы данных " + name + ": " + size);
                    } else {
                        rs.next();
                        count = rs.getInt(1);
                        receiver.add("Содержит " + count + " объектов");
                    }
                    isMoreResult = pst.getMoreResults();
                }
            } while (isMoreResult);
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
        }
    }

    boolean clearKarlson(Receiver receiver, String login, String password) {
        String query = "DELETE FROM Karlsons where user_id = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setLong(1, getUserId(login, password));
            int rows = pst.executeUpdate();
            receiver.add("Удалено " + rows + " объектов");
            return rows > 0;
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    private Object[] Initialization(Karlson forAction) {
        Object[] karlson = new Object[9];
        karlson[0] = forAction.getName();
        karlson[1] = forAction.getAge();
        karlson[2] = forAction.getTime();
        karlson[3] = forAction.getLocation().getX();
        karlson[4] = forAction.getLocation().getY();
        karlson[5] = forAction.getLocation().getZ();
        return karlson;
    }

    private void addLocation(int x, int y, int z, String name,int age,Long user_id) throws SQLException {
        String query = "INSERT INTO Locations(x,y,z,karlson_id) VALUES(?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, x);
            pst.setInt(2, y);
            pst.setInt(3, z);
            Long karlson_id = getKarlsonId(name,age,user_id);
            pst.setLong(4, karlson_id);
            pst.executeUpdate();
        }
    }



    boolean addKarlson(Karlson forAction, Receiver receiver, String login, String password) {
        String query = "INSERT INTO Karlsons(name, age, time, user_id) VALUES(?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            Object[] cr = Initialization(forAction);
            pst.setString(1, (String) cr[0]);
            pst.setInt(2, (int) cr[1]);
            ZonedDateTime time = ZonedDateTime.now();
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC)));
            Long user_id = getUserId(login, password);
            if (user_id == null) pst.setLong(4, 1);
            else pst.setLong(4, user_id);
            int row = pst.executeUpdate();
            addLocation((int) cr[3], (int) cr[4], (int) cr[5], (String) cr[0],(int) cr[1], user_id);
            return row > 0;
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    boolean addIfMax(Karlson forAction, Receiver receiver, String login, String password) {
        int age = forAction.getAge();
        boolean add = false;
        String query = "Select age from Karlsons";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            boolean isResult;
            pst.execute();
            do {
                try (ResultSet rs = pst.getResultSet()) {
                    if (rs.isBeforeFirst()) {
                        while (rs.next()) {
                            if (rs.getInt(1) < age)
                                add = true;
                        }
                        isResult = pst.getMoreResults();
                    } else {
                        isResult = false;
                    }
                }
            } while (isResult && add);
            if (add) addKarlson(forAction, receiver, login, password);
            return add;
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

    boolean removeKarlson(Karlson forAction, Receiver receiver, String login, String password) {
        String query = "SELECT user_id from Karlsons where name = ? AND age = ?";
        try (Connection connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             PreparedStatement pst = connection.prepareStatement(query)) {
            String name = forAction.getName();
            int age = forAction.getAge();
            pst.setString(1, name);
            pst.setInt(2, age);
            pst.execute();
            try (ResultSet rs = pst.getResultSet()) {
                if (rs.isBeforeFirst()) {
                    rs.next();
                    if (getUserId(login, password) == rs.getLong(1)) {
                        try (PreparedStatement pst1 = connection.prepareStatement("DELETE FROM Karlsons cascade where name = ? AND age = ?")) {
                            pst1.setString(1, name);
                            pst1.setInt(2, age);
                            return pst1.executeUpdate() > 0;
                        }
                    } else {
                        receiver.add("ОШИБКА: Объект не пренадлежит вам!");
                        return false;
                    }
                } else {
                    receiver.add("ОШИБКА: Такого объекта не существует!");
                    return false;
                }
            }
        } catch (SQLException e) {
            receiver.add(sqlException(e.getMessage()));
            return false;
        }
    }

}