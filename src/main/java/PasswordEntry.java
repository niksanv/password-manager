import java.io.Serializable;
import java.util.Objects;

public class PasswordEntry implements Serializable {
    private final String website;
    private final String login;
    private String password;
    private final String codeWord;

    public PasswordEntry(String website, String login, String password, String codeWord) {
        this.website = website;
        this.login = login;
        this.password = password;
        this.codeWord = codeWord;
    }

    // Геттеры
    public String getWebsite() { return website; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getCodeWord() { return codeWord; }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordEntry that = (PasswordEntry) o;
        return Objects.equals(website, that.website) &&
                Objects.equals(login, that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(website, login);
    }

    @Override
    public String toString() {
        return String.format("Website: %s, Login: %s, Password: %s",
                website, login, maskPassword(password));
    }

    public String toStringWithMask() {
        return String.format("Website: %s, Login: %s, Password: %s",
                website, login, maskPassword(password));
    }

    public String toStringWithoutMask() {
        return String.format("Website: %s, Login: %s, Password: %s",
                website, login, password);
    }

    private String maskPassword(String password) {
        if (password == null) return "";
        return "*".repeat(password.length());
    }
}