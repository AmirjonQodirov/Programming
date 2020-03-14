import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.util.Properties;

public class EmailSender {

    String email_password = "Amir-2000";
    String email = "amirqodirov8383@gmail.com";

    public String send(String login,String password) {
        try (FileInputStream inputStream = new FileInputStream("mail.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            Session mailSession = Session.getDefaultInstance(properties);
            Transport tr = mailSession.getTransport();
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(email));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(login));
            message.setSubject("Регистрация");
            message.setText("Ваш пароль : " + password);

            tr.connect(null, email_password);
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
            return "Пароль отправлен.";
        } catch (Exception e) {
            return "Ощибка!";
        }
    }

}