package com.ims.utils;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.Callable;

public class TextFieldValidatorConstraint {
    public final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final TextFieldValidatorSeverity severity;
    private final String invalidMessage;
    
    public TextFieldValidatorConstraint(
        TextFieldValidatorSeverity severity,
        String invalidMessage,
        Callable<Boolean> validityChecker,
        Observable... dependencies
    ) {
        this.severity = severity;
        this.invalidMessage = invalidMessage;
        for (Observable dep : dependencies) {
            dep.addListener((e) -> {
                try {
                    this.validProperty.set(validityChecker.call());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
    
    public String getInvalidMessage() {
        return invalidMessage;
    }
    
    public TextFieldValidatorSeverity getSeverity() {
        return severity;
    }
}
