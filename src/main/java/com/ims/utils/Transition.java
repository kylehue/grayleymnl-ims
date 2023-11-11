package com.ims.utils;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public abstract class Transition {
    public static void fadeUp(Node node, double durationMillis) {
        FadeTransition fadeInTransition = new FadeTransition(
            Duration.millis(durationMillis), node
        );
        fadeInTransition.setFromValue(0.25);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeInTransition.play();
        
        TranslateTransition translateTransition = new TranslateTransition(
            Duration.millis(durationMillis), node
        );
        translateTransition.setFromY(30);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        translateTransition.play();
    }
    
    public static void fadeDown(Node node, double durationMillis) {
        FadeTransition fadeInTransition = new FadeTransition(
            Duration.millis(durationMillis), node
        );
        fadeInTransition.setFromValue(0.25);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeInTransition.play();
        
        TranslateTransition translateTransition = new TranslateTransition(
            Duration.millis(durationMillis), node
        );
        translateTransition.setFromY(-30);
        translateTransition.setToY(0);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        translateTransition.play();
    }
    
    public static void fadeInBetweenNodes(Node nodeA, Node nodeB, double durationMillis) {
        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(durationMillis), nodeA
        );
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.playFromStart();
        FadeTransition fadeIn = new FadeTransition(
            Duration.millis(durationMillis), nodeB
        );
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.playFromStart();
    }
    
    public static void fadeIn(Node node, double durationMillis) {
        FadeTransition fadeInTransition = new FadeTransition(
            Duration.millis(durationMillis), node
        );
        fadeInTransition.setFromValue(0);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeInTransition.play();
    }
}
