package com.ims.canvas.network;

import com.ims.canvas.utils.Utils;
import com.ims.canvas.utils.Vector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;


class Tuple<X, Y> {
    public final X x;
    public final Y y;
    
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}

public class Node {
    public final String id = Utils.generateId();
    public Vector position;
    public Vector velocity;
    private double radius = Utils.generateRandomNumber(
        Config.MIN_RADIUS,
        Config.MAX_RADIUS
    );
    private double originalRadius = radius;
    private double maxOpacity = Utils.generateRandomNumber(
        Config.MIN_OPACITY,
        Config.MAX_OPACITY
    );
    private double opacity = Utils.generateRandomNumber(0, maxOpacity);
    private double speed = 10;
    private String color = getRandomColor();
    private double zoomOutAccelerator = 1;
    private double zoomInAccelerator = 1;
    private double heartBeatIncrementor = 1;
    
    private HashMap<String, Tuple<Node, Double>> connectedNodes = new HashMap<>();
    
    public Node() {
        position = new Vector(0, 0);
        velocity = new Vector(0, 0);
        velocity.randomizeX(-1, 1);
        velocity.randomizeY(-1, 1);
    }
    
    public void render(GraphicsContext ctx) {
        for (Tuple<Node, Double> connection : this.connectedNodes.values()) {
            ctx.save();
            ctx.beginPath();
            ctx.setStroke(Color.BLACK);
            Node connectedNode = connection.x;
            double distance = connection.y;
            
            ctx.setGlobalAlpha(
                Math.abs(distance / Config.CONNECTION_LENGTH - 1.0) * this.opacity * connectedNode.opacity
            );
            
            ctx.moveTo(this.position.getX(), this.position.getY());
            ctx.lineTo(connectedNode.position.getX(), connectedNode.position.getY());
            ctx.stroke();
            ctx.setLineWidth((this.radius + connectedNode.radius) / 3);
            ctx.closePath();
            ctx.restore();
        }
        
        ctx.save();
        ctx.setFill(Color.web(color));
        ctx.setGlobalAlpha(this.opacity);
        ctx.beginPath();
        ctx.fillOval(
            this.position.getX() - radius,
            this.position.getY() - radius,
            2 * radius,
            2 * radius
        );
        ctx.closePath();
        ctx.restore();
        
        
    }
    
    public void update(double fixedTimestep) {
        if (this.opacity <= maxOpacity) {
            this.opacity += 0.01 * fixedTimestep * this.speed;
        }
        
        this.position.add(
            this.velocity.getX() * fixedTimestep * this.speed,
            this.velocity.getY() * fixedTimestep * this.speed
        );
    }
    
    public void reset() {
        this.opacity = 0;
        this.connectedNodes.clear();
    }
    
    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    public void zoomOnHover(double mouseX, double mouseY) {
        double distance = Utils.getDistance(
            this.position.getX(),
            this.position.getY(),
            mouseX,
            mouseY
        );
        
        if (distance < Config.HOVER_DISTANCE) {
            if (this.radius <= this.originalRadius + Config.RADIUS_GAIN_ON_HOVER) {
                this.radius += zoomInAccelerator;
                zoomInAccelerator *= 1.05;
            }
            heartBeatIncrementor++;
            this.radius += Math.sin(heartBeatIncrementor / 15) / 10;
            zoomOutAccelerator = 1;
        } else {
            if (this.radius >= this.originalRadius) {
                this.radius -= zoomOutAccelerator;
                zoomOutAccelerator *= 1.05;
            }
            zoomInAccelerator = 1;
            heartBeatIncrementor = 0;
        }
    }
    
    public double getRadius() {
        return this.radius;
    }
    
    public void connectToOtherNodes(ArrayList<Node> otherNodes) {
        for (Node otherNode : otherNodes) {
            if (this == otherNode) continue;
            
            double distance = Utils.getDistance(
                this.position.getX(),
                this.position.getY(),
                otherNode.position.getX(),
                otherNode.position.getY()
            );
            
            if (distance > Config.CONNECTION_LENGTH) {
                this.disconnectToNode(otherNode);
            } else {
                this.connectToNode(otherNode, distance);
            }
        }
    }
    
    private void connectToNode(Node node, double distance) {
        this.connectedNodes.put(node.id, new Tuple<>(node, distance));
    }
    
    private void disconnectToNode(Node node) {
        this.connectedNodes.remove(node.id);
        node.connectedNodes.remove(this.id);
    }
    
    private boolean isConnectedToNode(Node node) {
        return this.connectedNodes.containsKey(node.id) ||
            node.connectedNodes.containsKey(this.id);
    }
    
    private String getRandomColor() {
        return Config.colors[
            (int) Math.floor(Utils.generateRandomNumber(0, Config.colors.length))
            ];
    }
}
