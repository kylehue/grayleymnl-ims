package com.ims.utils;


import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

public class AsyncCaller<T> {
    private final Task<T> task;
    private final ExecutorService executor;
    private final HashSet<Callback<WorkerStateEvent>> failedCallbacks = new HashSet<>();
    private final HashSet<Callback<T>> succeededCallbacks = new HashSet<>();
    private final HashSet<CallbackMutator<T>> succeededCallbackMutators = new HashSet<>();
    
    public AsyncCaller(TaskFunction<T> taskFunction, ExecutorService executor) {
        this.task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return taskFunction.call(this);
            }
        };
        
        this.executor = executor;
        
        this.onFailed(System.out::println);
    }
    
    public interface TaskFunction<T> {
        T call(Task<T> task) throws Exception;
    }
    
    public interface Callback<T> {
        void call(T v);
    }
    
    public interface CallbackMutator<T> {
        T call(T v);
    }
    
    public Task<T> getTask() {
        return task;
    }
    
    public AsyncCaller<T> execute() {
        this.task.setOnSucceeded((e) -> {
            T value = this.task.getValue();
            for (CallbackMutator<T> callback : this.succeededCallbackMutators) {
                value = callback.call(value);
            }
            
            for (Callback<T> callback : this.succeededCallbacks) {
                callback.call(value);
            }
        });
        
        this.task.setOnFailed((e) -> {
            for (Callback<WorkerStateEvent> callback : this.failedCallbacks) {
                callback.call(e);
            }
        });
        
        this.executor.submit(this.task);
        
        return this;
    }
    
    public AsyncCaller<T> onFailed(Callback<WorkerStateEvent> callback) {
        this.failedCallbacks.add(callback);
        return this;
    }
    
    public AsyncCaller<T> onSucceeded(Callback<T> callback) {
        this.succeededCallbacks.add(callback);
        return this;
    }
    
    public AsyncCaller<T> onSucceeded(CallbackMutator<T> callback) {
        this.succeededCallbackMutators.add(callback);
        return this;
    }
}
