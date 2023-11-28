package com.ims.utils;

import com.ims.Config;
import com.ims.components.Category;
import com.ims.model.BaseModel;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Pane;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Utils {
    public static final ExecutorService executor = Executors.newFixedThreadPool(8);
    
    public static String formatDate(LocalDateTime date) {
        LocalDate today = LocalDateTime.now().toLocalDate();
        LocalDate yesterday = today.minusDays(1);
        
        if (date.toLocalDate().equals(today)) {
            return "Today at " + date.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (date.toLocalDate().equals(yesterday)) {
            return "Yesterday at " + date.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else {
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        }
    }
    
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }
    
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (
            plainTextPassword == null ||
                hashedPassword == null ||
                plainTextPassword.isEmpty() ||
                hashedPassword.isEmpty()
        ) {
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
    
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }
    
    public static void bindModelToTextField(StringProperty model, MFXTextField textField) {
        AtomicReference<String> last = new AtomicReference<>("");
        model.addListener(($1, $2, email) -> {
            if (Objects.equals(last.get(), email)) return;
            last.set(email);
            textField.setText(email);
        });
        
        textField.textProperty().addListener(($1, $2, email) -> {
            if (Objects.equals(last.get(), email)) return;
            last.set(email);
            model.set(email);
        });
    }
    
    public static String generateRandomCode(int codeLength) {
        final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomCode = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC_CHARS.length());
            char randomChar = ALPHANUMERIC_CHARS.charAt(randomIndex);
            randomCode.append(randomChar);
        }
        
        return randomCode.toString();
    }
    
    public static String textToSearchPattern(String text) {
        if (text.isEmpty()) return "";
        String searchPattern = text;
        searchPattern = text.replaceAll("\\s", "");
        searchPattern = searchPattern.replaceAll("(.)", "$1.*");
        searchPattern = searchPattern.substring(0, searchPattern.length() - 2);
        return searchPattern;
    }
    
    public interface Callable<T> {
        T call();
    }
}
