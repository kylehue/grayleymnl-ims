package com.ims;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class Utils {
    public static void fitImageViewToParent(ImageView node) {
        Node parent = node.getParent();
        node.fitWidthProperty().bind(((Region) parent).widthProperty());
        node.fitHeightProperty().bind(((Region) parent).heightProperty());
    }
}
