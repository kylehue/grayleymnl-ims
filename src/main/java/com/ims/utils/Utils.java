package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.StringProperty;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Utils {
    public static String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        if (date.equals(today)) {
            return "Today";
        } else if (date.equals(yesterday)) {
            return "Yesterday";
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
}
