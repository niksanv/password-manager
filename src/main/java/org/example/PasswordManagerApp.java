import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class PasswordManagerApp {
    private final PasswordStorage passwordManager;
    private final Scanner scanner;

    public PasswordManagerApp() {
        this.passwordManager = new DistributedPasswordManager();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Password Manager ===");

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    savePassword();
                    break;
                case "2":
                    getPasswordsByWebsite();
                    break;
                case "3":
                    getPasswordByWebsiteAndLogin();
                    break;
                case "4":
                    System.out.println("Выход из приложения...");
                    return;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nВыберите действие:");
        System.out.println("1. Сохранить пароль");
        System.out.println("2. Получить пароли по сайту");
        System.out.println("3. Получить пароль по сайту и логину");
        System.out.println("4. Выход");
        System.out.print("Ваш выбор: ");
    }

    private void savePassword() {
        System.out.println("\n--- Сохранение пароля ---");

        System.out.print("Введите сайт: ");
        String website = scanner.nextLine().trim();

        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        System.out.print("Введите кодовое слово: ");
        String codeWord = scanner.nextLine().trim();

        if (passwordManager.exists(website, login)) {
            System.out.println("Запись для сайта '" + website + "' и логина '" + login + "' уже существует.");
            System.out.print("Хотите обновить пароль? (yes/no): ");
            String response = scanner.nextLine().trim();

            if ("yes".equalsIgnoreCase(response) || "y".equalsIgnoreCase(response)) {
                PasswordEntry updatedEntry = new PasswordEntry(website, login, password, codeWord);
                passwordManager.updatePassword(updatedEntry);
                System.out.println("Пароль успешно обновлен!");
            } else {
                System.out.println("Обновление отменено.");
            }
        } else {
            try {
                PasswordEntry newEntry = new PasswordEntry(website, login, password, codeWord);
                passwordManager.savePassword(newEntry);
                System.out.println("Пароль успешно сохранен!");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void getPasswordsByWebsite() {
        System.out.println("\n--- Получение паролей по сайту ---");

        System.out.print("Введите сайт: ");
        String website = scanner.nextLine().trim();

        List<PasswordEntry> entries = passwordManager.getPasswordsByWebsite(website);

        if (entries.isEmpty()) {
            System.out.println("Пароли для сайта '" + website + "' не найдены.");
        } else {
            System.out.println("Найдены следующие записи (пароли замаскированы):");
            for (int i = 0; i < entries.size(); i++) {
                System.out.println((i + 1) + ". " + entries.get(i).toStringWithMask());
            }
        }
    }

    private void getPasswordByWebsiteAndLogin() {
        System.out.println("\n--- Получение пароля по сайту и логину ---");

        System.out.print("Введите сайт: ");
        String website = scanner.nextLine().trim();

        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Введите кодовое слово: ");
        String codeWord = scanner.nextLine().trim();

        Optional<PasswordEntry> entry = passwordManager.getPasswordByWebsiteAndLogin(website, login, codeWord);

        if (entry.isPresent()) {
            System.out.println("Пароль найден:");
            System.out.println(entry.get().toStringWithoutMask());
        } else {
            System.out.println("Пароль не найден или неверное кодовое слово.");
        }
    }

    public static void main(String[] args) {
        PasswordManagerApp app = new PasswordManagerApp();
        app.start();
    }
}