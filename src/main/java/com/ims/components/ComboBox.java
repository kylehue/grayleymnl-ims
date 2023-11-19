package com.ims.components;

import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.HashMap;

public class ComboBox<K, V> extends StackPane {
    public final MFXTextField textField = new MFXTextField();
    private final MFXButton toggleDropDownButton = new MFXButton("");
    private final HashMap<K, V> items = new HashMap<>();
    private final Dropdown<K> dropdown = new Dropdown<>();
    private Stringifier<V> stringifier = Object::toString;
    private V value = null;
    private SelectEvent<V> selectEvent = null;
    
    public ComboBox() {
        // Set up the TextField
        textField.setPrefWidth(300);
        textField.setMinWidth(100);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setPadding(new Insets(0, 50, 0, 0));
        this.getChildren().add(textField);
        
        // Set up dropdown button
        toggleDropDownButton.getStyleClass().add("icon-button");
        LayoutUtils.addIconToButton(toggleDropDownButton, "/icons/menu-down.svg");
        StackPane.setAlignment(toggleDropDownButton, Pos.TOP_RIGHT);
        toggleDropDownButton.setTranslateY(5);
        this.getChildren().add(toggleDropDownButton);
        
        // Set up dropdown
        dropdown.bindToTextField(textField);
        
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setDuration(Duration.millis(250));
        rotateTransition.setNode(toggleDropDownButton);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);
        
        toggleDropDownButton.setOnMouseClicked(e -> {
            if (dropdown.isShowing()) {
                dropdown.hide();
            } else {
                dropdown.showDropdown(textField);
            }
            textField.requestFocus();
        });
        
        dropdown.setOnShown(e -> {
            rotateTransition.setFromAngle(0);
            rotateTransition.setToAngle(180);
            rotateTransition.play();
        });
        
        dropdown.setOnHidden(e -> {
            rotateTransition.setFromAngle(180);
            rotateTransition.setToAngle(0);
            rotateTransition.play();
            if (textField.delegateIsFocused()) {
                toggleDropDownButton.requestFocus();
            }
        });
        
        textField.delegateFocusedProperty().addListener(e -> {
            if (textField.delegateIsFocused()) {
                toggleDropDownButton.getStyleClass().add("icon-button-active");
            } else {
                toggleDropDownButton.getStyleClass().remove("icon-button-active");
            }
        });
        
        // clear selected value when text changed
        // use event filter instead of event handler cuz mfx sucks
        textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            this.value = null;
        });
    }
    
    public void clearValue() {
        this.textField.clear();
        this.value = null;
    }
    
    public interface Stringifier<T> {
        String call(T item);
    }
    
    public void setStringifier(Stringifier<V> stringifier) {
        this.stringifier = stringifier;
    }
    
    public interface SelectEvent<T> {
        void call(T item);
    }
    
    public void setOnSelect(SelectEvent<V> selectEvent) {
        this.selectEvent = selectEvent;
    }
    
    public void setValue(V value) {
        if (value == null) {
            this.clearValue();
            return;
        }
        this.value = value;
        this.textField.setText(this.stringifier.call(this.value));
        textField.positionCaret(textField.getText().length());
    }
    
    public V getValue() {
        return value;
    }
    
    public void addItem(K id, V item) {
        Platform.runLater(() -> {
            items.put(id, item);
            MFXButton button = dropdown.addItem(id, this.stringifier.call(item));
            button.setOnMouseClicked(e -> {
                this.setValue(item);
                if (this.selectEvent != null) {
                    this.selectEvent.call(item);
                }
                dropdown.hide();
            });
        });
    }
    
    public void removeItem(K id) {
        Platform.runLater(() -> {
            items.remove(id);
            dropdown.removeItemByID(id);
        });
    }
    
    public void updateItem(K id, V item) {
        Platform.runLater(() -> {
            items.put(id, item);
            dropdown.updateItemByID(id, this.stringifier.call(item));
        });
    }
    
    public void setItems(ObservableMap<K, V> map) {
        map.addListener(
            (MapChangeListener<K, V>) change -> {
                K id = change.getKey();
                boolean isAddedAlready = items.get(id) != null;
                boolean needsToBeAdded = change.wasAdded() && !isAddedAlready;
                boolean needsToBeUpdated = change.wasAdded() && isAddedAlready;
                boolean needsToBeRemoved = change.wasRemoved() && isAddedAlready;
                if (needsToBeAdded) {
                    this.addItem(id, change.getValueAdded());
                } else if (needsToBeUpdated) {
                    this.updateItem(id, change.getValueAdded());
                } else if (needsToBeRemoved) {
                    this.removeItem(id);
                }
            }
        );
        
        for (K key : map.keySet()) {
            if (this.items.get(key) != null) continue;
            this.addItem(key, map.get(key));
        }
    }
    
    public MFXScrollPane getDropDownScrollPane() {
        return this.dropdown.getScrollPane();
    }
    
    public VBox getDropdownContainer() {
        return this.dropdown.getContainer();
    }
    
    private static class Dropdown<K> extends Popup {
        private final VBox container = new VBox();
        private final MFXScrollPane scrollPane = new MFXScrollPane();
        private final HashMap<K, MFXButton> itemMap = new HashMap<>();
        
        public Dropdown() {
            this.setWidth(-1);
            this.setHeight(-1);
            this.setAutoHide(true);
            this.setAutoFix(false);
            
            this.focusedProperty().addListener(($1, $2, isFocused) -> {
                if (!isFocused) this.hide();
            });
            
            container.setFillWidth(true);
            container.setPrefWidth(-1);
            container.setPrefHeight(-1);
            container.getStyleClass().add("context-menu");
            
            scrollPane.setFitToHeight(false);
            scrollPane.setFitToWidth(true);
            scrollPane.setMaxHeight(300);
            scrollPane.setPadding(new Insets(5));
            scrollPane.getStylesheets().add(
                getClass().getResource("/styles/global.css").toExternalForm()
            );
            scrollPane.getStyleClass().add("context-menu");
            scrollPane.setContent(container);
            
            this.getContent().add(scrollPane);
            
            this.showingProperty().addListener(e -> {
                Transition.fadeDown(scrollPane, 200);
            });
        }
        
        private double[] getTextFieldCoordinates(MFXTextField textField) {
            double height = textField.getHeight();
            Point2D coordinates = textField.localToScreen(0, 0);
            
            // Calculate the position of nodeA relative to nodeB
            double nodeAX = coordinates.getX() - 10;
            double nodeAY = coordinates.getY() + height;
            
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            if (nodeAY + scrollPane.getHeight() >= primScreenBounds.getHeight()) {
                nodeAY -= scrollPane.getHeight() + height;
            }
            
            return new double[]{nodeAX, nodeAY};
        }
        
        public void showDropdown(MFXTextField textField) {
            if (this.itemMap.isEmpty()) return;
            double[] coordinates = getTextFieldCoordinates(textField);
            this.show(SceneManager.getStage(), coordinates[0], coordinates[1]);
        }
        
        public void bindToTextField(MFXTextField textField) {
            textField.delegateFocusedProperty().addListener(($1, $2, isFocused) -> {
                scrollPane.setMinWidth(textField.getWidth());
                if (!isFocused) {
                    this.hide();
                } else {
                    showDropdown(textField);
                }
            });
            
            textField.delegateCaretPositionProperty().addListener(e -> {
                if (!this.isShowing() && textField.delegateIsFocused()) {
                    textField.requestFocus();
                }
            });
            
            textField.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                textField.positionCaret(textField.getText().length());
                if (!this.isShowing() && textField.delegateIsFocused()) {
                    textField.requestFocus();
                }
            });
        }
        
        public void removeItemByID(K id) {
            MFXButton button = this.itemMap.get(id);
            if (button == null) return;
            this.itemMap.remove(id);
            Platform.runLater(() -> {
                container.getChildren().remove(button);
            });
        }
        
        public void updateItemByID(K id, String text) {
            Platform.runLater(() -> {
                this.itemMap.get(id).setText(text);
            });
        }
        
        public MFXButton getItemByID(K id) {
            return this.itemMap.get(id);
        }
        
        public MFXButton addItem(K id, String text) {
            MFXButton button = new MFXButton(text);
            Platform.runLater(() -> {
                button.getStyleClass().add("context-menu-item-button");
                button.setMaxWidth(Double.MAX_VALUE);
                button.setTextAlignment(TextAlignment.LEFT);
                button.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().add(button);
                this.itemMap.put(id, button);
            });
            return button;
        }
        
        public MFXScrollPane getScrollPane() {
            return this.scrollPane;
        }
        
        public VBox getContainer() {
            return this.container;
        }
    }
}
