package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
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
import java.util.concurrent.atomic.AtomicLong;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public abstract class LayoutUtils {
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
     * Resize a FlowPane's children based on its width
     *
     * @param flowPane    The FlowPane that contains the children to be resized.
     * @param minWidth    Minimum width of a child in a row.
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
        int numChildrenInRow = Math.max((int) (flowPaneWidth / minWidth), 1);
        
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
                pane.setMinWidth(20);
                pane.setMaxWidth(Double.MAX_VALUE);
                
                if (aspectRatio != 1) {
                    pane.setPrefHeight(finalWidth / aspectRatio);
                    pane.setMinHeight(20 / aspectRatio);
                    pane.setMaxHeight(Double.MAX_VALUE);
                }
            }
        }
    }
    
    /**
     * Create a responsive FlowPane that is aware of the width of its children.
     *
     * @param flowPane    The `FlowPane` element.
     * @param minWidth    Minimum width of a child in a row.
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
        
        flowPane.getChildren().addListener((ListChangeListener<? super Node>) (e) -> {
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
    
    public static Pane createIcon(String iconURL) {
        Pane icon = null;
        try {
            String resolvedPath = Utils.class.getResource(iconURL).toURI().toString();
            String path = extractSVGPath(resolvedPath);
            icon = new Pane();
            icon.setStyle("-fx-shape: \"" + path + "\";");
            
            // Get bounds
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(path);
            Bounds bounds = svgPath.getBoundsInLocal();
            
            // Set size
            icon.setPrefWidth(bounds.getWidth());
            icon.setMaxWidth(bounds.getWidth());
            icon.setPrefHeight(bounds.getHeight());
            icon.setMaxHeight(bounds.getHeight());
            icon.setFocusTraversable(false);
            icon.getStyleClass().add("icon");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return icon;
    }
    
    /**
     * Adds an icon to a node.
     *
     * @param button  The node where the icon will be placed.
     * @param iconURL The URL of the icon.
     */
    public static void addIconToButton(
        MFXButton button,
        String iconURL
    ) {
        Pane icon = createIcon(iconURL);
        button.setGraphicTextGap(8);
        button.setGraphic(icon);
    }
    
    public static void setupGridPane(
        GridPane gridPane,
        int rowCount,
        int columnCount
    ) {
        gridPane.getRowConstraints().clear();
        gridPane.getColumnConstraints().clear();
        
        for (int i = 0; i < Math.max(rowCount, 1); i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.SOMETIMES);
            rowConstraints.setValignment(VPos.TOP);
            rowConstraints.setFillHeight(true);
            rowConstraints.setMinHeight(USE_COMPUTED_SIZE);
            rowConstraints.setPrefHeight(USE_COMPUTED_SIZE);
            rowConstraints.setMaxHeight(USE_COMPUTED_SIZE);
            gridPane.getRowConstraints().add(rowConstraints);
        }
        
        for (int i = 0; i < Math.max(columnCount, 1); i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.SOMETIMES);
            columnConstraints.setHalignment(HPos.LEFT);
            columnConstraints.setFillWidth(true);
            columnConstraints.setMinWidth(USE_COMPUTED_SIZE);
            columnConstraints.setPrefWidth(USE_COMPUTED_SIZE);
            columnConstraints.setMaxWidth(USE_COMPUTED_SIZE);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
    }
    
    public static GridPane createGridPane(int rowCount, int columnCount) {
        GridPane gridPane = new GridPane();
        setupGridPane(gridPane, rowCount, columnCount);
        return gridPane;
    }
    
    public static void applyVirtualScrolling(
        MFXScrollPane scrollPane,
        Pane container
    ) {
        InvalidationListener listener = (e) -> {
            Platform.runLater(() -> {
                double viewportHeight = scrollPane.getHeight();
                double scrollHeight = container.getHeight() - viewportHeight;
                double scrollValue = scrollHeight * scrollPane.getVvalue();
                
                double visibleMinY = scrollValue;
                double visibleMaxY = scrollValue + viewportHeight;
                
                double bufferOffset = 0;
                
                for (Node child : container.getChildren()) {
                    double childMinY = child.getBoundsInParent().getMinY() - bufferOffset;
                    double childMaxY = child.getBoundsInParent().getMaxY() + bufferOffset;
                    
                    boolean isVisible = childMaxY >= visibleMinY && childMinY <= visibleMaxY;
                    child.setVisible(isVisible);
                }
            });
        };
        
        scrollPane.vvalueProperty().addListener(listener);
        container.heightProperty().addListener(listener);
        container.getChildren().addListener(listener);
    }
}
