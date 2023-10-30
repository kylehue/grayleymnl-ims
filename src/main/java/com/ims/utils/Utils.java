package com.ims.utils;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
    
    public static String hashPassword(String plainTextPassword){
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }
    
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
    
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }
}
