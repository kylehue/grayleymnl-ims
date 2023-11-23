package com.ims.components;

import com.ims.utils.LayoutUtils;
import com.ims.utils.SceneManager;
import com.ims.utils.Transition;
import com.ims.utils.Utils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ComboBox<K, V> extends StackPane {
    public final MFXTextField textField = new MFXTextField();
    private final MFXButton toggleDropDownButton = new MFXButton("");
    private final HashMap<K, V> items = new HashMap<>();
    protected final Dropdown<K> dropdown = new Dropdown<>();
    private Stringifier<V> stringifier = Object::toString;
    private final ObjectProperty<V> value = new SimpleObjectProperty<>();
    private final ArrayList<SelectEvent<V>> selectListeners = new ArrayList<>();
    private final ArrayList<Select2Event<V>> select2Listeners = new ArrayList<>();
    public final StringProperty searchText = new SimpleStringProperty();
    
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
        
        // clear selected value when text changed manually
        textField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            this.value.set(null);
            searchText.set(textField.getText());
        });
        
        this.searchTextProperty().addListener(e -> {
            
            this.search(this.getSearchText());
        });
    }
    
    public String getSearchText() {
        return searchText.get();
    }
    
    public StringProperty searchTextProperty() {
        return searchText;
    }
    
    public void search(String searchText) {
        String searchPattern = Utils.textToSearchPattern(searchText);
        Pattern pattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);
        HashMap<K, MFXButton> itemMap = dropdown.getItemMap();
        for (K id : itemMap.keySet()) {
            MFXButton item = itemMap.get(id);
            boolean doesMatch = pattern.matcher(item.getText()).find();
            item.setVisible(doesMatch);
            item.setManaged(doesMatch);
        }
    }
    
    public void clearValue() {
        this.textField.clear();
        this.value.set(null);
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
    
    public interface Select2Event<T> {
        void call(T newItem, T oldItem);
    }
    
    public void addSelectionListener(SelectEvent<V> selectEvent) {
        this.selectListeners.add(selectEvent);
    }
    
    public void addSelectionListener(Select2Event<V> selectEvent) {
        this.select2Listeners.add(selectEvent);
    }
    
    public void setValue(V value) {
        if (value == null) {
            this.clearValue();
            return;
        }
        if (value == this.value.get()) return;
        V oldValue = this.value.get();
        this.value.set(value);
        this.textField.setText(this.stringifier.call(this.value.get()));
        textField.positionCaret(textField.getText().length());
        this.triggerSelectListeners(value, oldValue);
    }
    
    public V getValue() {
        return value.get();
    }
    
    public ObjectProperty<V> valueProperty() {
        return value;
    }
    
    private MFXButton _addItem(K id, V item) {
        items.put(id, item);
        MFXButton button = dropdown.addItem(id, this.stringifier.call(item));
        button.setOnMouseClicked(e -> {
            V oldItem = this.getValue();
            this.setValue(item);
            this.triggerSelectListeners(item, oldItem);
            dropdown.hide();
        });
        return button;
    }
    
    public void addItem(K id, V item) {
        Platform.runLater(() -> {
            if (items.containsKey(id)) return;
            this._addItem(id, item);
        });
    }
    
    public void addItem(K id, V item, Property<String> textProperty) {
        Platform.runLater(() -> {
            if (items.containsKey(id)) return;
            MFXButton button = this._addItem(id, item);
            if (textProperty != null) {
                button.textProperty().bind(textProperty);
            }
        });
    }
    
    private void triggerSelectListeners(V item, V oldItem) {
        for (SelectEvent<V> listener : this.selectListeners) {
            listener.call(item);
        }
        for (Select2Event<V> listener : this.select2Listeners) {
            listener.call(item, oldItem);
        }
    }
    
    public void removeItem(K id) {
        Platform.runLater(() -> {
            dropdown.removeItemByID(id);
            items.remove(id);
        });
    }
    
    public void clear() {
        Platform.runLater(() -> {
            dropdown.clear();
            items.clear();
        });
    }
    
    public void updateItem(K id, V item) {
        Platform.runLater(() -> {
            items.put(id, item);
            dropdown.updateItemByID(id, this.stringifier.call(item));
        });
    }
    
    public void _setItems(
        ObservableMap<K, V> map,
        TextPropertyGetter<V> textPropertyGetter
    ) {
        map.addListener(
            (MapChangeListener<K, V>) change -> {
                K id = change.getKey();
                if (change.wasAdded()) {
                    V obj = change.getValueAdded();
                    if (textPropertyGetter == null) {
                        this.addItem(id, obj);
                    } else {
                        this.addItem(id, obj, textPropertyGetter.call(obj));
                    }
                } else if (change.wasRemoved()) {
                    this.removeItem(id);
                }
            }
        );
        
        for (K key : map.keySet()) {
            if (this.items.get(key) != null) continue;
            V obj = map.get(key);
            if (textPropertyGetter == null) {
                this.addItem(key, obj);
            } else {
                this.addItem(key, obj, textPropertyGetter.call(obj));
            }
        }
    }
    
    public void setItems(ObservableMap<K, V> map) {
        this._setItems(map, null);
    }
    
    public void setItems(
        ObservableMap<K, V> map,
        TextPropertyGetter<V> textPropertyGetter
    ) {
        this._setItems(map, textPropertyGetter);
    }
    
    public interface TextPropertyGetter<V> {
        Property<String> call(V obj);
    }
    
    public MFXScrollPane getDropDownScrollPane() {
        return this.dropdown.getScrollPane();
    }
    
    public VBox getDropdownContainer() {
        return this.dropdown.getContainer();
    }
    
    protected static class Dropdown<K> extends Popup {
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
        
        public HashMap<K, MFXButton> getItemMap() {
            return itemMap;
        }
        
        public void removeItemByID(K id) {
            MFXButton button = this.itemMap.get(id);
            if (button == null) return;
            this.itemMap.remove(id);
            container.getChildren().remove(button);
        }
        
        public void clear() {
            for (K id : itemMap.keySet()) {
                container.getChildren().remove(
                    itemMap.get(id)
                );
            }
        }
        
        public void updateItemByID(K id, String text) {
            this.itemMap.get(id).setText(text);
        }
        
        public MFXButton getItemByID(K id) {
            return this.itemMap.get(id);
        }
        
        public MFXButton addItem(K id, String text) {
            MFXButton button = new MFXButton(text);
            button.getStyleClass().add("context-menu-item-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setTextAlignment(TextAlignment.LEFT);
            button.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(button);
            this.itemMap.put(id, button);
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
