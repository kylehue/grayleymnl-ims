package com.ims.utils;


import com.ims.database.DBUsers;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import java.util.concurrent.ExecutorService;

public class AsyncCaller<T> {
    private final Task<T> task;
    private Callback<WorkerStateEvent> onFailedCallback;
    private Callback<T> onSucceededCallback;
    
    public AsyncCaller(TaskFunction<T> taskFunction) {
        this.task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return taskFunction.call(this);
            }
        };
    }
    
    public interface TaskFunction<T> {
        T call(Task<T> task);
    }
    
    public interface Callback<T> {
        void call(T v);
    }
    
    public AsyncCaller<T> execute(ExecutorService executor) {
        this.task.setOnSucceeded((e) -> {
            onSucceededCallback.call(this.task.getValue());
        });
        
        this.task.setOnFailed((e) -> {
            onFailedCallback.call(e);
        });
        
        executor.submit(this.task);
        
        return this;
    }
    
    public AsyncCaller<T> onFailed(Callback<WorkerStateEvent> callback) {
        this.onFailedCallback = callback;
        return this;
    }
    
    public AsyncCaller<T> onSucceeded(Callback<T> callback) {
        this.onSucceededCallback = callback;
        return this;
    }
}
