import java.util.List;
import java.util.Optional;

public interface PasswordStorage {
    void savePassword(PasswordEntry entry);
    void updatePassword(PasswordEntry entry);
    List<PasswordEntry> getPasswordsByWebsite(String website);
    Optional<PasswordEntry> getPasswordByWebsiteAndLogin(String website, String login, String codeWord);
    boolean exists(String website, String login);
}