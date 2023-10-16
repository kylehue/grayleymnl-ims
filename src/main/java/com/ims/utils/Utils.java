package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Pair;

import java.util.List;

import static java.util.Arrays.stream;

public class Utils {
    /**
     * Makes an ImageView fill its parent container.
     *
     * @param node The ImageView
     */
    public static void fitImageViewToParent(ImageView node) {
        Node parent = node.getParent();
        node.fitWidthProperty().bind(((Region) parent).widthProperty());
        node.fitHeightProperty().bind(((Region) parent).heightProperty());
    }
    
    /**
     * Make a particular tab active, making the rest inactive.
     *
     * @param tab         The tab to make active.
     * @param tabs        The list of tabs.
     * @param activeClass The class to use in an active button.
     */
    private static void makeTabActive(
        Pair<MFXButton, Pane> tab,
        List<Pair<MFXButton, Pane>> tabs,
        String activeClass
    ) {
        MFXButton tabButton = tab.getKey();
        Pane tabPane = tab.getValue();
        
        ObservableList<String> styleClass = tabButton.getStyleClass();
        if (!styleClass.contains(activeClass)) {
            styleClass.add(activeClass);
        }
        
        tabPane.setVisible(true);
        
        // Make other tabs inactive
        for (Pair<MFXButton, Pane> otherTab : tabs) {
            if (otherTab == tab) continue;
            MFXButton otherTabButton = otherTab.getKey();
            Pane otherTabPane = otherTab.getValue();
            otherTabButton.getStyleClass().remove(activeClass);
            otherTabPane.setVisible(false);
        }
    }
    
    /**
     * Create a tab group from a list of pairs of buttons and panes.
     * The first pair in the list will be the main tab.
     *
     * @param activeClass The class to use in an active button.
     * @param tabs        A list containing pairs of buttons and panes.
     */
    public static void createTabGroup(
        String activeClass,
        List<Pair<MFXButton, Pane>> tabs
    ) {
        boolean hasActiveTab = false;
        
        for (Pair<MFXButton, Pane> tab : tabs) {
            MFXButton tabButton = tab.getKey();
            Pane tabPane = tab.getValue();
            
            // All tab panes should start invisible
            tabPane.setVisible(false);
            
            boolean isActiveTabButton = tabButton.getStyleClass().contains(activeClass);
            if (isActiveTabButton) {
                makeTabActive(tab, tabs, activeClass);
            }
            
            tabButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                makeTabActive(tab, tabs, activeClass);
            });
            
            if (!hasActiveTab) hasActiveTab = isActiveTabButton;
        }
        
        if (!hasActiveTab) {
            Pair firstTab = tabs.get(0);
            makeTabActive(firstTab, tabs, activeClass);
        }
    }
    
    /**
     * Create a smart FlowPane that is aware of the size of its children.
     *
     * @param pane             The `FlowPane` element.
     * @param childMinWidth    Minimum width of a child in a row.
     * @param maxChildrenInRow Maximum number of children in a row.
     * @apiNote The children of the `FlowPane` must be an instance of the `Pane` class.
     */
    public static void createSmartFlowPane(
        FlowPane pane,
        double childMinWidth,
        int maxChildrenInRow,
        double childrenAspectRatio
    ) {
        if (maxChildrenInRow <= 0) {
            throw new Error("The max number of children must be higher than 0.");
        }
        
        ObservableList<Node> children = pane.getChildren();
        pane.widthProperty().addListener((obs, oldValue, newValue) -> {
            double paneWidth = newValue.doubleValue();
            double childWidth = paneWidth / maxChildrenInRow;

            /*
              Compute the needed number of children in a row based on the
              child min width and max children.

              First, we have to get the variable that answers the question
              "how close are we to the minimum width?"

              Here, we got it by dividing the minimum width to the current width,
              which then gives us the percentage (0-1) of how close we are to the
              minimum width.

              Then we can simply floor that and subtract it to the max children.
             */
            int computedMaxChildrenInRow = (int) (
                maxChildrenInRow - Math.floor(childMinWidth / childWidth)
            );
            
            double computedChildWidth = paneWidth / computedMaxChildrenInRow;
            
            int excessChildrenCount = children.size() % computedMaxChildrenInRow;
            int emptyChildrenCount = computedMaxChildrenInRow - excessChildrenCount;
            
            for (int i = 0; i < children.size(); i++) {
                Pane child = (Pane) children.get(i);
                
                // Get the margins/paddings size for offset
                Insets margin = FlowPane.getMargin(child);
                Insets padding = child.getPadding();
                Insets insets = child.getInsets();
                double marginsOffset = stream(new double[]{
                    margin.getRight(),
                    margin.getLeft(),
                    padding.getRight(),
                    padding.getLeft(),
                    insets.getRight(),
                    insets.getLeft()
                }).sum();
                
                
                double widthMultiplier = 1.0;
                double arbitraryOffset = 5;
                
                // The last children should fill the remaining space if there's any
                int startIndexOfExcessChildren = children.size() - 1 - excessChildrenCount;
                boolean hasExcessChildren = excessChildrenCount != 0;
                boolean currentChildIsAnExcess = i > startIndexOfExcessChildren;
                if (hasExcessChildren && currentChildIsAnExcess) {
                    widthMultiplier +=
                        (double) emptyChildrenCount / (double) excessChildrenCount;
                    
                    // we also need to adjust the margin sum offset of the children
                    // honestly I have no clue how math worked here
                    arbitraryOffset += marginsOffset * (widthMultiplier - 1) / 2;
                }
                
                double finalWidth =
                    computedChildWidth * widthMultiplier - marginsOffset - arbitraryOffset;
                
                child.setPrefWidth(finalWidth);
                child.setMaxWidth(finalWidth);
                child.setPrefHeight(finalWidth / childrenAspectRatio);
                child.setMaxHeight(finalWidth / childrenAspectRatio);
            }
        });
    }
}
