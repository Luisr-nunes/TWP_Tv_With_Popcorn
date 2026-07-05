package com.twp.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncManager {
    // Thread pool otimizado para tarefas de rede limitadas (I/O)
    private static final ExecutorService executor = Executors.newFixedThreadPool(6);
    
    public static <T> CompletableFuture<T> runAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { 
                return task.call(); 
            } catch (Exception e) { 
                throw new CompletionException(e); 
            }
        }, executor);
    }
    
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }
    
    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
