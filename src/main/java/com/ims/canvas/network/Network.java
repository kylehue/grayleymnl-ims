package com.ims.canvas.network;

import com.ims.canvas.utils.FixedTimestepAnimationTimer;
import com.ims.canvas.utils.Utils;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class Network extends FixedTimestepAnimationTimer {
    public Canvas canvas;
    
    public GraphicsContext ctx;
    private final ArrayList<Node> nodes = new ArrayList<>();
    
    private double mouseX = 0;
    private double mouseY = 0;
    
    public Network(Canvas canvas) {
        super(Config.FPS);
        
        this.canvas = canvas;
        this.ctx = canvas.getGraphicsContext2D();
        
        // Fit canvas size to parent
        Parent parent = canvas.getParent();
        if (parent instanceof Pane) {
            this.canvas.widthProperty().bind(((Pane) (parent)).widthProperty());
            this.canvas.heightProperty().bind(((Pane) (parent)).heightProperty());
        }
        
        this.canvas.widthProperty().addListener((o, oldValue, newValue) -> {
            Platform.runLater(() -> {
                for (Node node : this.nodes) {
                    this.restartNode(node);
                }
            });
        });
        
        this.setup();
        this.start();
        
        parent.setOnMouseMoved(event -> {
            this.mouseX = event.getX();
            this.mouseY = event.getY();
        });
    }
    
    private void spawnNode() {
        Node node = new Node();
        
        final double CANVAS_WIDTH = this.canvas.getWidth();
        final double CANVAS_HEIGHT = this.canvas.getHeight();
        
        node.position.randomizeX(0, CANVAS_WIDTH);
        node.position.randomizeY(0, CANVAS_HEIGHT);
        
        this.nodes.add(node);
    }
    
    private void restartNode(Node node) {
        final double CANVAS_WIDTH = this.canvas.getWidth();
        final double CANVAS_HEIGHT = this.canvas.getHeight();
        
        node.position.randomizeX(0, CANVAS_WIDTH);
        node.position.randomizeY(0, CANVAS_HEIGHT);
        
        for (int i = 0; i < this.nodes.size(); i++) {
            Node otherNode = this.nodes.get(i);
            if (node == otherNode) continue;
            double distance = Utils.getDistance(
                node.position.getX(),
                node.position.getY(),
                otherNode.position.getX(),
                otherNode.position.getY()
            );
            
            if (distance <= Config.CONNECTION_LENGTH / 3) {
                node.position.randomizeX(0, CANVAS_WIDTH);
                node.position.randomizeY(0, CANVAS_HEIGHT);
                i = 0;
            }
        }
        
        node.reset();
    }
    
    private void setup() {
        Platform.runLater(() -> {
            for (int i = 0; i < Config.NODE_COUNT; i++) {
                this.spawnNode();
            }
        });
    }
    
    @Override
    public void render(double deltaTime) {
        final double CANVAS_WIDTH = this.canvas.getWidth();
        final double CANVAS_HEIGHT = this.canvas.getHeight();
        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        for (Node node : this.nodes) {
            node.render(this.ctx);
        }
    }
    
    @Override
    public void update(double fixedTimestep) {
        for (Node node : this.nodes) {
            node.update(fixedTimestep);
            node.connectToOtherNodes(this.nodes);
            node.zoomOnHover(mouseX, mouseY);
            this.restartNodeIfOutOfBounds(node);
        }
    }
    
    private void restartNodeIfOutOfBounds(Node node) {
        final double CANVAS_WIDTH = this.canvas.getWidth();
        final double CANVAS_HEIGHT = this.canvas.getHeight();
        double nodeX = node.position.getX();
        double nodeY = node.position.getY();
        double nodeRadius = node.getRadius();
        
        final boolean IS_OUT_OF_BOUNDS =
            nodeX >= CANVAS_WIDTH + nodeRadius ||
                nodeX <= -nodeRadius ||
                nodeY >= CANVAS_HEIGHT + nodeRadius ||
                nodeY <= -nodeRadius;
        
        if (IS_OUT_OF_BOUNDS) {
            this.restartNode(node);
        }
    }
}
