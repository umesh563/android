package com.github.chagall.notificationlistenerexample;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shadman on 5/8/16.
 */
@TargetApi(11)
public class SerialExecutor implements Executor {
    private final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
    private final Executor executor;
    private Runnable mActive;

    private final String taskName;

    public SerialExecutor(Executor threadPoolExecutor) {
        this.executor = threadPoolExecutor;
        this.taskName = "SerialExecutor";

        if ( threadPoolExecutor == null ) {
            throw new RuntimeException("Thread pool executer cannot be null");
        }
    }

    public SerialExecutor() {
        this("SerialExecutor");
    }

    public SerialExecutor(String name) {
        this.taskName = name;

        ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r,  taskName + "#" + mCount.getAndIncrement());
            }
        };

        BlockingQueue<Runnable> sPoolWorkQueue =
                new LinkedBlockingQueue<Runnable>(10);

        this.executor = new ThreadPoolExecutor(2, 128, 1,
                TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy());;
    }

    public synchronized void execute(@NonNull final Runnable r) {
        mTasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });

        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            executor.execute(mActive);
        }
    }
}