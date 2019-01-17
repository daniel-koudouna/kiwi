package com.proxy.kiwi.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class TaskQueue {
    private boolean isActive;
    private Runnable runnable;
    private Thread thread;
    private List<Runnable> tasks;

    public TaskQueue() {
        this.tasks = new LinkedList<>();
        this.runnable = () -> {
            while (isActive) {
                AtomicLong sleepTime = new AtomicLong(10);
                Optional<Runnable> task = Optional.empty();
                synchronized (this.tasks) {
                    if (!tasks.isEmpty()) {
                        task = Optional.of(tasks.remove(0));
                    }
                }
                task.ifPresent(r -> {
                    r.run();
                    sleepTime.set(100);
                });
                try {
                    Thread.sleep(sleepTime.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void start() {
        this.isActive = true;
        this.thread = new Thread(this.runnable);
        this.thread.start();
    }

    public void enqueue(Runnable r) {
        synchronized (this.tasks) {
            tasks.add(r);
        }
    }

    public void join() {
        isActive = false;
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
