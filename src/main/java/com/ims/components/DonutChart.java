package com.ims.components;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DonutChart extends PieChart {
    private final Circle innerCircle;
    
    public DonutChart(ObservableList<Data> pieData) {
        super(pieData);
        
        innerCircle = new Circle();
        
        innerCircle.setFill(Color.WHITE);
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(3);
        
        this.setDepthTest(DepthTest.DISABLE);
        this.setEffect(null);
        this.setAnimated(true);
    }
    
    @Override
    protected void layoutChartChildren(
        double top,
        double left,
        double contentWidth,
        double contentHeight
    ) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);
        
        addInnerCircleIfNotPresent();
        updateInnerCircleLayout();
    }
    
    private void addInnerCircleIfNotPresent() {
        if (!getData().isEmpty()) {
            Node pie = getData().get(0).getNode();
            if (pie.getParent() instanceof Pane) {
                Pane parent = (Pane) pie.getParent();
                
                if (!parent.getChildren().contains(innerCircle)) {
                    parent.getChildren().add(innerCircle);
                }
            }
        }
    }
    
    private void updateInnerCircleLayout() {
        Platform.runLater(() -> {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            for (PieChart.Data data: getData()) {
                Node node = data.getNode();
                
                Bounds bounds = node.getBoundsInParent();
                if (bounds.getMinX() < minX) {
                    minX = bounds.getMinX();
                }
                if (bounds.getMinY() < minY) {
                    minY = bounds.getMinY();
                }
                if (bounds.getMaxX() > maxX) {
                    maxX = bounds.getMaxX();
                }
                if (bounds.getMaxY() > maxY) {
                    maxY = bounds.getMaxY();
                }
            }
            
            innerCircle.setCenterX(minX + (maxX - minX) / 2);
            innerCircle.setCenterY(minY + (maxY - minY) / 2);
            
            innerCircle.setRadius((maxX - minX) / 4);
        });
    }
}