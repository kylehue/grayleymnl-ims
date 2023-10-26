package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

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
     * Resize a FlowPane's children based on its width
     * @param flowPane The FlowPane that contains the children to be resized.
     * @param minWidth Minimum width of a child in a row.
     * @param aspectRatio The aspect ratio of children. Set to 1 to disable.
     * @apiNote The children of the `FlowPane` must be an instance of the `Pane`
     */
    private static void resizeFlowPaneChildren(
        FlowPane flowPane,
        double minWidth,
        double aspectRatio,
        boolean fillEmptySpace
    ) {
        ObservableList<Node> children = flowPane.getChildren();
        int childrenCount = children.size();
        double flowPaneWidth = flowPane.getWidth();
        
        // Get number of children that can fit without reaching minWidth
        int numChildrenInRow = (int) (flowPaneWidth / minWidth);
        
        double computedWidth = flowPaneWidth / numChildrenInRow;
        
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            
            if (node instanceof Pane pane) {
                double arbitraryOffset = 10;
                
                Insets margin = FlowPane.getMargin(node);
                double marginsOffset = margin != null ?
                        margin.getRight() +
                        margin.getLeft()
                    : 0;
                
                double finalWidth;
                int lastRowPanes = childrenCount % numChildrenInRow;
                if (
                    lastRowPanes != 0 &&
                        i > childrenCount - 1 - lastRowPanes &&
                        fillEmptySpace
                ) {
                    double lastRowWidth = flowPaneWidth / lastRowPanes;
                    
                    // TODO: fix computations to properly align the excess child width
                    finalWidth = lastRowWidth - arbitraryOffset - marginsOffset * (numChildrenInRow - lastRowPanes);
                } else {
                    finalWidth = computedWidth - marginsOffset - arbitraryOffset;
                }
                
                pane.setPrefWidth(finalWidth);
                pane.setPrefHeight(finalWidth / aspectRatio);
                pane.setMinWidth(20);
                pane.setMinHeight(20 / aspectRatio);
                pane.setMaxWidth(Double.MAX_VALUE);
                pane.setMaxHeight(Double.MAX_VALUE);
            }
        }
    }
    
    /**
     * Create a responsive FlowPane that is aware of the width of its children.
     *
     * @param flowPane The `FlowPane` element.
     * @param minWidth Minimum width of a child in a row.
     * @param aspectRatio The aspect ratio of children. Set to 1 to disable.
     * @apiNote The children of the `FlowPane` must be an instance of the `Pane` class.
     */
    public static void createResponsiveFlowPane(
        FlowPane flowPane,
        double minWidth,
        double aspectRatio,
        boolean fillEmptySpace
    ) {
        flowPane.setMinWidth(0);
        flowPane.setMaxWidth(Double.MAX_VALUE);
        flowPane.widthProperty().addListener(($1, $2, $3) -> {
            Platform.runLater(() -> {
                resizeFlowPaneChildren(
                    flowPane,
                    minWidth,
                    aspectRatio,
                    fillEmptySpace
                );
            });
        });
    }
    
    /**
     * Extracts the path of an SVG file.
     *
     * @param svgFilePath The URL of the svg file.
     * @return The path of the SVG file.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private static String extractSVGPath(
        String svgFilePath
    ) throws ParserConfigurationException, IOException, SAXException {
        Document doc =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(svgFilePath);
        doc.getDocumentElement().normalize();
        
        // Get all path elements in the SVG
        NodeList pathNodes = doc.getElementsByTagName("path");
        
        if (pathNodes.getLength() > 0) {
            Element pathElement = (Element) pathNodes.item(0);
            return pathElement.getAttribute("d");
        } else {
            return "";
        }
    }
    
    /**
     * Adds an icon to a node.
     *
     * @param button  The node where the icon will be placed.
     * @param iconURL The URL of the icon.
     * @throws URISyntaxException
     */
    public static <T extends MFXButton>void addIconToButton(
        MFXButton button, String iconURL
    ) {
        try {
            String resolvedPath = Utils.class.getResource(iconURL).toURI().toString();
            String path = extractSVGPath(resolvedPath);
            Pane icon = new Pane();
            icon.setStyle("-fx-shape: \"" + path + "\";");
            
            double size = 16;
            icon.setPrefWidth(size);
            icon.setMaxWidth(size);
            icon.setPrefHeight(size);
            icon.setMaxHeight(size);
            icon.setFocusTraversable(false);
            
            button.setGraphicTextGap(8);
            button.setGraphic(icon);
        } catch(Exception e) {
            System.out.println(e);
        }
    }
    
    /**
     * Adds an icon to a node.
     *
     * @param textField  The node where the icon will be placed.
     * @param iconURL The URL of the icon.
     * @throws URISyntaxException
     */
    public static <T extends MFXButton>void addIconToTextField(
        MFXTextField textField, String iconURL
    ) {
        try {
            String resolvedPath = Utils.class.getResource(iconURL).toURI().toString();
            String path = extractSVGPath(resolvedPath);
            Pane icon = new Pane();
            icon.setStyle("-fx-shape: \"" + path + "\";");
            
            double size = 16;
            icon.setPrefWidth(size);
            icon.setMaxWidth(size);
            icon.setPrefHeight(size);
            icon.setMaxHeight(size);
            icon.setFocusTraversable(false);
            
            textField.setGraphicTextGap(8);
            textField.setLeadingIcon(icon);
        } catch(Exception e) {
            System.out.println(e);
        }
    }
    
    public static GridPane createGridPane(int rowCount, int columnCount) {
        GridPane gridPane = new GridPane();
        
        for (int i = 0; i < rowCount; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.SOMETIMES);
            rowConstraints.setValignment(VPos.TOP);
            rowConstraints.setFillHeight(true);
            rowConstraints.setMinHeight(10);
            rowConstraints.setPrefHeight(USE_COMPUTED_SIZE);
            rowConstraints.setMaxHeight(USE_COMPUTED_SIZE);
            rowConstraints.setPrefHeight(-1);
            gridPane.getRowConstraints().add(rowConstraints);
        }
        
        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.SOMETIMES);
            columnConstraints.setHalignment(HPos.LEFT);
            columnConstraints.setFillWidth(true);
            columnConstraints.setMinWidth(10);
            columnConstraints.setPrefWidth(USE_COMPUTED_SIZE);
            columnConstraints.setMaxWidth(USE_COMPUTED_SIZE);
            columnConstraints.setPrefWidth(-1);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        
        return gridPane;
    }
}
