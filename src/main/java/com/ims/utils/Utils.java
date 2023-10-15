package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Pair;

import java.util.List;

public class Utils {
    /**
     * Makes an ImageView fill its parent container.
     * @param node The ImageView
     */
    public static void fitImageViewToParent(ImageView node) {
        Node parent = node.getParent();
        node.fitWidthProperty().bind(((Region) parent).widthProperty());
        node.fitHeightProperty().bind(((Region) parent).heightProperty());
    }

    /**
     * Make a particular tab active, making the rest inactive.
     * @param tab The tab to make active.
     * @param tabs The list of tabs.
     * @param activeClass The class to use in an active button.
     */
    private static void makeTabActive(
            Pair<MFXButton, Pane> tab,
            List<Pair<MFXButton, Pane>> tabs,
            String activeClass
    ) {
        MFXButton tabButton = (MFXButton) tab.getKey();
        Pane tabPane = (Pane) tab.getValue();

        ObservableList<String> styleClass = tabButton.getStyleClass();

        // Make this tab button active
        if (!styleClass.contains(activeClass)) {
            styleClass.add(activeClass);
        }

        tabPane.setVisible(true);

        // Make the other tabs inactive
        for (Pair<MFXButton, Pane> otherTab : tabs) {
            if (otherTab == tab) continue;
            MFXButton otherTabButton = (MFXButton) otherTab.getKey();
            Pane otherTabPane = (Pane) otherTab.getValue();
            otherTabButton.getStyleClass().remove(activeClass);
            otherTabPane.setVisible(false);
        }
    }

    /**
     * Create a tab group from a list of pairs of buttons and panes.
     * The first pair in the list will be the main tab.
     * @param activeClass The class to use in an active button.
     * @param tabs A list containing pairs of buttons and panes.
     */
    public static void createTabGroup(
            String activeClass,
            List<Pair<MFXButton, Pane>> tabs
    ) {
        boolean hasActiveTab = false;

        for (Pair<MFXButton, Pane> tab : tabs) {
            MFXButton tabButton = (MFXButton) tab.getKey();
            Pane tabPane = (Pane) tab.getValue();

            // All tab panes should start invisible
            tabPane.setVisible(false);

            // Then here, if a tab's button is active, we make its tab pane visible
            boolean isActiveTabButton = tabButton.getStyleClass().contains(activeClass);
            if (isActiveTabButton) {
                System.out.println(1);
                makeTabActive(tab, tabs, activeClass);
            }

            // Switch active tab on tab button click
            tabButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                makeTabActive(tab, tabs, activeClass);
            });

            if (!hasActiveTab) hasActiveTab = isActiveTabButton;
        }

        // Make the first tab active if no tabs are active
        if (!hasActiveTab) {
            makeTabActive(tabs.get(0), tabs, activeClass);
        }
    }
}
