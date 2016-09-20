package com.infinity.thread;

import java.util.concurrent.*;


/**
 * @author meizs
 *         线程池配置类(Singleton)
 */
public class ThreadPools {

    public static final Long TIME_OUT = 45000L;
    private ExecutorService requestPool;
    private ScheduledExecutorService cleanService = Executors.newScheduledThreadPool(1);
    private ConcurrentHashMap<String, Object> results_map = new ConcurrentHashMap<String, Object>();
    private ConcurrentHashMap<String, Future> future_map = new ConcurrentHashMap<>();
    private int mixnum = 128;
    private int maxnum = 1024;
    private Long alive = 1000L;
    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private ThreadFactory factory = new NamedThreadFactory("req_thread_");
    private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

    private ThreadPools() {
        //        this.setRedis(JedisXFactory.getJedisX("192.168.1.133:6379", 500, 100, 10));
        this.setRequestPool(new ThreadPoolExecutor(mixnum, maxnum, alive, TimeUnit.MILLISECONDS, queue, factory, handler));
    }

    public static ThreadPools getInstance() {
        return ThreadPoolsHandler.instance;
    }

    public ScheduledExecutorService getCleanService() {
        return cleanService;
    }

    public void setCleanService(ScheduledExecutorService cleanService) {
        this.cleanService = cleanService;
    }

    public ExecutorService getRequestPool() {
        return requestPool;
    }

    public void setRequestPool(ExecutorService requestPool) {
        this.requestPool = requestPool;
    }

    public ConcurrentHashMap<String, Object> getResults_map() {
        return results_map;
    }

    public void setResults_map(ConcurrentHashMap<String, Object> results_map) {
        this.results_map = results_map;
    }

    public int getMixnum() {
        return mixnum;
    }

    public void setMixnum(int mixnum) {
        this.mixnum = mixnum;
    }

    public int getMaxnum() {
        return maxnum;
    }

    public void setMaxnum(int maxnum) {
        this.maxnum = maxnum;
    }

    public Long getAlive() {
        return alive;
    }

    public void setAlive(Long alive) {
        this.alive = alive;
    }

    public BlockingQueue<Runnable> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    public ThreadFactory getFactory() {
        return factory;
    }

    public void setFactory(ThreadFactory factory) {
        this.factory = factory;
    }

    public RejectedExecutionHandler getHandler() {
        return handler;
    }

    public void setHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
    }

    public ConcurrentHashMap<String, Future> getFuture_map() {
        return future_map;
    }

    public void setFuture_map(ConcurrentHashMap<String, Future> future_map) {
        this.future_map = future_map;
    }

    public void removeFuture(String key) {
        Future future = getFuture_map().remove(key);
        if (null != future) {
            future.cancel(true);
        }
    }


    private static class ThreadPoolsHandler {
        private static ThreadPools instance = new ThreadPools();
    }



}
