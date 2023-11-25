package com.ims.components;

import com.ims.utils.LayoutUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

public class NumberField extends StackPane {
    public final MFXTextField textField = new MFXTextField();
    
    private double step = 1.0;
    private boolean allowDecimal = true;
    private boolean allowNegative = true;
    private final DoubleProperty value = new SimpleDoubleProperty(0);
    private final DoubleProperty maxValue = new SimpleDoubleProperty(Double.MAX_VALUE);
    private final DoubleProperty minValue = new SimpleDoubleProperty(-Double.MAX_VALUE);
    
    public NumberField(boolean showButtons) {
        this.setAlignment(Pos.CENTER_LEFT);
        
        this.getChildren().add(textField);
        
        this.prefWidthProperty().bind(textField.prefWidthProperty());
        this.minWidthProperty().bind(textField.minWidthProperty());
        this.maxWidthProperty().bind(textField.maxWidthProperty());
        
        if (showButtons) {
            textField.setStyle("-fx-padding: 5 50 5 50 !important;");
            MFXButton decrementButton = new MFXButton();
            decrementButton.getStyleClass().addAll("icon-button");
            decrementButton.setText("");
            LayoutUtils.addIconToButton(decrementButton, "/icons/minus.svg");
            StackPane.setAlignment(decrementButton, Pos.CENTER_LEFT);
            this.getChildren().add(decrementButton);
            
            MFXButton incrementButton = new MFXButton();
            incrementButton.getStyleClass().addAll("icon-button");
            incrementButton.setText("");
            LayoutUtils.addIconToButton(incrementButton, "/icons/plus.svg");
            StackPane.setAlignment(incrementButton, Pos.CENTER_RIGHT);
            this.getChildren().add(incrementButton);
            
            decrementButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                this.decrement();
            });
            
            incrementButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                this.increment();
            });
        }
        
        textField.setFloatingText("Number");
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValid(newValue)) {
                this.setValue(!oldValue.isEmpty() ? Double.parseDouble(oldValue) : 0);
            } else {
                this.setValue(!newValue.isEmpty() ? Double.parseDouble(newValue) : 0);
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
            event.consume();
        });
        
        this.minValueProperty().addListener(e -> {
            this.setValue(this.getValue());
        });
        
        this.maxValueProperty().addListener(e -> {
            this.setValue(this.getValue());
        });
        
        textField.delegateFocusedProperty().addListener(e -> {
            this.setValue(this.getValue());
        });
        
        this.setValue(0);
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
    
    public DoubleProperty valueProperty() {
        return value;
    }
    
    public double getValue() {
        return value.get() + 1 - 1;
    }
    
    public DoubleProperty maxValueProperty() {
        return maxValue;
    }
    
    public double getMaxValue() {
        return maxValue.get();
    }
    
    public DoubleProperty minValueProperty() {
        return minValue;
    }
    
    public double getMinValue() {
        return minValue.get();
    }
    
    public void setStep(double step) {
        this.step = step;
    }
    
    private boolean isWholeNumber(String str) {
        return str.matches("-?\\d+(\\.0)?");
    }
    
    public void increment() {
        this.setValue(this.getValue() + this.step);
    }
    
    public void decrement() {
        this.setValue(this.getValue() - this.step);
    }
    
    public void setValue(double value) {
        if (value < 0 && !this.allowNegative) {
            value = 0;
        }
        
        if (value > this.getMaxValue()) {
            value = this.getMaxValue();
        }
        
        if (value < this.getMinValue()) {
            value = this.getMinValue();
        }
        
        this.value.set(value);
        
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
