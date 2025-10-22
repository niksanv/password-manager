import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DistributedPasswordManager implements PasswordStorage {
    private final Map<String, List<PasswordEntry>> storage;
    private final Map<String, Set<String>> websiteLogins;

    public DistributedPasswordManager() {
        this.storage = new ConcurrentHashMap<>();
        this.websiteLogins = new ConcurrentHashMap<>();
    }

    @Override
    public void savePassword(PasswordEntry entry) {
        String website = entry.getWebsite();
        String login = entry.getLogin();

        // Проверяем существование пары website + login
        if (exists(website, login)) {
            throw new IllegalArgumentException("Entry already exists for website: " +
                    website + " and login: " + login);
        }

        // Сохраняем в хранилище
        storage.computeIfAbsent(website, k -> new ArrayList<>()).add(entry);

        // Обновляем индекс логинов для сайта
        websiteLogins.computeIfAbsent(website, k -> new HashSet<>()).add(login);
    }

    @Override
    public void updatePassword(PasswordEntry entry) {
        String website = entry.getWebsite();
        String login = entry.getLogin();

        // Удаляем старую запись
        List<PasswordEntry> entries = storage.get(website);
        if (entries != null) {
            entries.removeIf(e -> e.getLogin().equals(login));
            entries.add(entry);
        }
    }

    @Override
    public List<PasswordEntry> getPasswordsByWebsite(String website) {
        List<PasswordEntry> entries = storage.get(website);
        if (entries == null) {
            return new ArrayList<>();
        }

        // Возвращаем копии с маскированными паролями
        return entries.stream()
                .map(entry -> new PasswordEntry(
                        entry.getWebsite(),
                        entry.getLogin(),
                        maskPassword(entry.getPassword()),
                        entry.getCodeWord()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PasswordEntry> getPasswordByWebsiteAndLogin(String website, String login, String codeWord) {
        List<PasswordEntry> entries = storage.get(website);
        if (entries == null) {
            return Optional.empty();
        }

        return entries.stream()
                .filter(entry -> entry.getLogin().equals(login))
                .findFirst()
                .filter(entry -> validateCodeWord(entry, codeWord))
                .map(entry -> new PasswordEntry(
                        entry.getWebsite(),
                        entry.getLogin(),
                        entry.getPassword(), // Пароль без маскировки
                        entry.getCodeWord()
                ));
    }

    @Override
    public boolean exists(String website, String login) {
        Set<String> logins = websiteLogins.get(website);
        return logins != null && logins.contains(login);
    }

    private boolean validateCodeWord(PasswordEntry entry, String inputCodeWord) {
        return entry.getCodeWord().equals(inputCodeWord);
    }

    private String maskPassword(String password) {
        if (password == null) return "";
        return "*".repeat(password.length());
    }

    // Метод для получения всех записей (для отладки)
    public List<PasswordEntry> getAllEntries() {
        return storage.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}