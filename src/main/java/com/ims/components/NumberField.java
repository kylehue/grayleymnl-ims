package com.ims.components;

import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

public class NumberField extends HBox {
    public final MFXTextField textField = new MFXTextField();
    
    private double step = 1.0;
    private double value = 0;
    private boolean allowDecimal = true;
    private boolean allowNegative = true;
    
    public NumberField() {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(5);
        
        MFXButton decrementButton = new MFXButton();
        decrementButton.getStyleClass().addAll("icon-button");
        decrementButton.setText("");
        LayoutUtils.addIconToButton(decrementButton, "/icons/minus-circle-outline.svg");
        
        MFXButton incrementButton = new MFXButton();
        incrementButton.getStyleClass().addAll("icon-button");
        incrementButton.setText("");
        LayoutUtils.addIconToButton(incrementButton, "/icons/plus-circle-outline.svg");
        
        textField.setFloatingText("Number");
        this.getChildren().addAll(decrementButton, textField, incrementButton);
        
        decrementButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            this.decrement();
        });
        
        incrementButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            this.increment();
        });
        
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValid(newValue)) {
                this.value = !oldValue.isEmpty() ? Double.parseDouble(oldValue) : 0;
            } else {
                this.value = !newValue.isEmpty() ? Double.parseDouble(newValue) : 0;
            }
        });
        
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String text = textField.getText();
            int caretPosition = textField.delegateGetCaretPosition();
            String beforeCaret = text.substring(0, caretPosition);
            String afterCaret = text.substring(caretPosition);
            String newText = beforeCaret + event.getCharacter() + afterCaret;
            
            if (!isValid(newText)) {
                event.consume();
            }
        });
        
        textField.addEventHandler(ScrollEvent.SCROLL, (event) -> {
            if (!textField.delegateIsFocused()) return;
            if (event.getDeltaY() > 0) {
                this.increment();
            } else if (event.getDeltaY() < 0) {
                this.decrement();
            }
        });
        
        this.setValue(this.value);
    }
    
    private boolean isValid(String str) {
        boolean result = true;
        if (this.allowDecimal) {
            result = !(!str.matches("-?\\d*(\\.\\d*)?") ||
                str.startsWith(".") ||
                str.startsWith("-."));
        } else {
            result = str.matches("-?\\d*(\\d*)?");
        }
        
        if (str.startsWith("-") && !this.allowNegative) {
            result = false;
        }
        
        return result;
    }
    
    public void setStep(double step) {
        this.step = step;
    }
    
    private boolean isWholeNumber(String str) {
        return str.matches("-?\\d+(\\.0)?");
    }
    
    public void increment() {
        this.setValue(this.value += this.step);
    }
    
    public void decrement() {
        if (this.value - this.step < 0 && !this.allowNegative) {
            return;
        }
        
        this.setValue(this.value -= this.step);
    }
    
    public void setValue(double value) {
        this.value = value;
        
        String strValue = Double.toString(value);
        if (isWholeNumber(strValue) || !allowDecimal) {
            this.textField.setText(strValue.split("\\.")[0]);
        } else {
            this.textField.setText(String.format("%.2f", value));
        }
    }
    
    public void setAllowDecimal(boolean v) {
        this.allowDecimal = v;
    }
    
    public void setAllowNegative(boolean v) {
        this.allowNegative = v;
    }
}
