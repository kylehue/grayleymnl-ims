package com.ims.utils;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Pane;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LazyLoader {
    public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final MFXScrollPane scrollPane;
    private final Pane contentPane;
    private final ObservableMap<?, ?> model;
    
    public enum LoadType {
        HIT_BOTTOM,
        INITIAL,
        INSUFFICIENT
    }
    
    public interface Loader {
        void call(LoadType requestType);
    }
    
    public LazyLoader(
        MFXScrollPane scrollPane,
        Pane contentPane,
        ObservableMap<?, ?> model
    ) {
        this.scrollPane = scrollPane;
        this.contentPane = contentPane;
        this.model = model;
        
        // Make sure the content pane's height will depend on its children
        scrollPane.setFitToHeight(false);
        contentPane.setMaxHeight(-1);
        contentPane.setPrefHeight(-1);
        contentPane.setMinHeight(-1);
    }
    
    private void loadItemsWhenNoScrollbar(Loader loader) {
        Platform.runLater(() -> {
            double contentHeight = contentPane.getBoundsInLocal().getHeight();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            if (contentHeight < viewportHeight) {
                loader.call(LoadType.INSUFFICIENT);
            }
        });
    }
    
    private ScheduledFuture<?> previousTask = null;
    
    private void scheduleTask(Utils.Callable<Void> task) {
        if (previousTask != null) {
            previousTask.cancel(false);
        }
        
        previousTask = executor.schedule(() -> {
            task.call();
        }, 300, TimeUnit.MILLISECONDS);
    }
    
    public void setLoader(Loader loader) {
        scrollPane.vvalueProperty().addListener(($1, $2, scrollValue) -> {
            if (scrollValue.doubleValue() != 1) return;
            loader.call(LoadType.HIT_BOTTOM);
        });
        
        scrollPane.viewportBoundsProperty().addListener(
            ($1, $2, newValue) -> {
                loadItemsWhenNoScrollbar(loader);
            }
        );
        
        model.addListener(
            (MapChangeListener<Object, Object>) change -> {
                if (!change.wasAdded()) return;
                this.scheduleTask(() -> {
                    loadItemsWhenNoScrollbar(loader);
                    return null;
                });
            }
        );
        
        loader.call(LoadType.INITIAL);
    }
}
