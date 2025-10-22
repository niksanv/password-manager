import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

public class PasswordManagerTest {

    private DistributedPasswordManager passwordManager;

    @BeforeEach
    void setUp() {
        passwordManager = new DistributedPasswordManager();
    }

    @Test
    void testSaveAndRetrievePassword() {
        // Подготовка тестовых данных
        PasswordEntry entry = new PasswordEntry(
                "google.com",
                "testuser",
                "password123",
                "secret"
        );

        // Сохраняем пароль
        passwordManager.savePassword(entry);

        // Проверяем, что запись существует
        assertTrue(passwordManager.exists("google.com", "testuser"));

        // Получаем пароли по сайту (с маскировкой)
        List<PasswordEntry> entries = passwordManager.getPasswordsByWebsite("google.com");
        assertEquals(1, entries.size());
        assertEquals("google.com", entries.get(0).getWebsite());
        assertEquals("testuser", entries.get(0).getLogin());
        assertTrue(entries.get(0).getPassword().contains("*")); // Пароль замаскирован
    }

    @Test
    void testRetrievePasswordWithCodeWord() {
        // Подготовка тестовых данных
        PasswordEntry entry = new PasswordEntry(
                "github.com",
                "developer",
                "devpass456",
                "code123"
        );

        passwordManager.savePassword(entry);

        // Получаем пароль с правильным кодовым словом
        Optional<PasswordEntry> result = passwordManager.getPasswordByWebsiteAndLogin(
                "github.com", "developer", "code123"
        );

        assertTrue(result.isPresent());
        assertEquals("devpass456", result.get().getPassword()); // Пароль без маскировки

        // Пытаемся получить с неправильным кодовым словом
        Optional<PasswordEntry> wrongCodeResult = passwordManager.getPasswordByWebsiteAndLogin(
                "github.com", "developer", "wrongcode"
        );

        assertFalse(wrongCodeResult.isPresent());
    }

    @Test
    void testDuplicateEntryThrowsException() {
        PasswordEntry entry1 = new PasswordEntry(
                "amazon.com",
                "user1",
                "pass1",
                "code1"
        );

        PasswordEntry entry2 = new PasswordEntry(
                "amazon.com",
                "user1", // Тот же логин
                "pass2",
                "code2"
        );

        passwordManager.savePassword(entry1);

        // Попытка сохранить дубликат должна вызвать исключение
        assertThrows(IllegalArgumentException.class, () -> {
            passwordManager.savePassword(entry2);
        });
    }

    @Test
    void testUpdatePassword() {
        PasswordEntry original = new PasswordEntry(
                "facebook.com",
                "user",
                "oldpass",
                "code"
        );

        passwordManager.savePassword(original);

        // Обновляем пароль
        PasswordEntry updated = new PasswordEntry(
                "facebook.com",
                "user",
                "newpass",
                "code"
        );

        passwordManager.updatePassword(updated);

        // Проверяем, что пароль обновился
        Optional<PasswordEntry> result = passwordManager.getPasswordByWebsiteAndLogin(
                "facebook.com", "user", "code"
        );

        assertTrue(result.isPresent());
        assertEquals("newpass", result.get().getPassword());
    }

    @Test
    void testMultipleEntriesForSameWebsite() {
        PasswordEntry entry1 = new PasswordEntry(
                "twitter.com",
                "user1",
                "pass1",
                "code1"
        );

        PasswordEntry entry2 = new PasswordEntry(
                "twitter.com",
                "user2",
                "pass2",
                "code2"
        );

        passwordManager.savePassword(entry1);
        passwordManager.savePassword(entry2);

        // Получаем все записи для сайта
        List<PasswordEntry> entries = passwordManager.getPasswordsByWebsite("twitter.com");
        assertEquals(2, entries.size());

        // Проверяем, что оба логина присутствуют
        boolean hasUser1 = entries.stream().anyMatch(e -> e.getLogin().equals("user1"));
        boolean hasUser2 = entries.stream().anyMatch(e -> e.getLogin().equals("user2"));

        assertTrue(hasUser1);
        assertTrue(hasUser2);
    }

    @Test
    void testNonExistentWebsite() {
        List<PasswordEntry> entries = passwordManager.getPasswordsByWebsite("nonexistent.com");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testPasswordMasking() {
        PasswordEntry entry = new PasswordEntry(
                "test.com",
                "user",
                "123456789",
                "code"
        );

        passwordManager.savePassword(entry);

        List<PasswordEntry> entries = passwordManager.getPasswordsByWebsite("test.com");
        String maskedPassword = entries.get(0).getPassword();

        // Проверяем, что пароль замаскирован звездочками
        assertEquals("*********", maskedPassword);
        assertEquals(9, maskedPassword.length()); // Такая же длина как оригинальный пароль
    }
}