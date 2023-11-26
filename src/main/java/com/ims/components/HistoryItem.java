package com.ims.components;

import com.ims.database.DBHistory;
import com.ims.model.UserManagerModel;
import com.ims.model.objects.UserObject;
import com.ims.utils.LayoutUtils;
import com.ims.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;

import java.sql.Timestamp;

public class HistoryItem extends GridPane {
    private final ObjectProperty<DBHistory.Action> action = new SimpleObjectProperty<>();
    private final StringProperty subject = new SimpleStringProperty("");
    private final ObjectProperty<Integer> userID = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> lastModified = new SimpleObjectProperty<>();
    private DBHistory.HistoryData historyData = null;
    private final Label userLabel = new Label();
    private final Label actionLabel = new Label();
    private final Label subjectLabel = new Label();
    private final Label lastModifedLabel = new Label();
    
    
    public HistoryItem() {
        LayoutUtils.setupGridPane(this, 2, 1);
        TextFlow textFlow = new TextFlow(
            userLabel,
            actionLabel,
            subjectLabel
        );
        this.add(textFlow, 0, 0);
        this.add(lastModifedLabel, 0, 1);
        
        this.getStyleClass().add("history-item");
        textFlow.getStyleClass().add("history-item-header");
        userLabel.getStyleClass().add("history-item-user-label");
        actionLabel.getStyleClass().add("history-item-action-label");
        subjectLabel.getStyleClass().add("history-item-subject-label");
        lastModifedLabel.getStyleClass().add("history-item-timestamp-label");
        
        userIDProperty().addListener(e -> {
            Platform.runLater(() -> {
                Integer userID = userIDProperty().get();
                if (userID != null) {
                    UserObject user = UserManagerModel.loadAndGetUser(userID);
                    userLabel.setText(user.getEmail().split("@")[0]);
                } else {
                    userLabel.setText("Unknown user");
                }
            });
        });
        
        actionProperty().addListener(e -> {
            Platform.runLater(() -> {
                DBHistory.Action action = actionProperty().get();
                if (action == DBHistory.Action.ADD_PRODUCT) {
                    actionLabel.setText(" added a new product ");
                } else if (action == DBHistory.Action.REMOVE_PRODUCT) {
                    actionLabel.setText(" removed the product ");
                } else if (action == DBHistory.Action.EDIT_PRODUCT) {
                    actionLabel.setText(" edited the product ");
                } else if (action == DBHistory.Action.ADD_CATEGORY) {
                    actionLabel.setText(" added a new category ");
                } else if (action == DBHistory.Action.REMOVE_CATEGORY) {
                    actionLabel.setText(" removed the category ");
                } else if (action == DBHistory.Action.EDIT_CATEGORY) {
                    actionLabel.setText(" edited the category ");
                }
            });
        });
        
        subjectProperty().addListener(e -> {
            Platform.runLater(() -> {
                String subject = subjectProperty().get();
                subjectLabel.setText(subject);
            });
        });
        
        lastModifiedProperty().addListener(e -> {
            Platform.runLater(() -> {
                lastModifedLabel.setText(Utils.formatDate(
                    lastModifiedProperty().get().toLocalDateTime()
                ));
            });
        });
    }
    
    public ObjectProperty<DBHistory.Action> actionProperty() {
        return action;
    }
    
    public StringProperty subjectProperty() {
        return subject;
    }
    
    public ObjectProperty<Integer> userIDProperty() {
        return userID;
    }
    
    public ObjectProperty<Timestamp> lastModifiedProperty() {
        return lastModified;
    }
    
    public void setHistoryData(DBHistory.HistoryData historyData) {
        this.historyData = historyData;
        
        this.actionProperty().set(historyData.getAction());
        this.subjectProperty().set(historyData.getSubject());
        this.userIDProperty().set(historyData.getUserID());
        this.lastModifiedProperty().set(historyData.getLastModified());
    }
    
    public DBHistory.HistoryData getHistoryData() {
        return historyData;
    }
}
