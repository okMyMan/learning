package com.ooteco;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

/**
 * Created by xule on 2017/4/27.
 */
public class CallableThread2 implements Callable<String> {
    public String call() throws Exception {
        System.out.println("进入CallableThread的call()方法, 开始睡觉, 睡觉时间为" + System.currentTimeMillis());
        Thread.sleep(10000);
        return "123";
    }

    public static void main(String[] args) throws Exception {

        // 求单词长度之和
        Stream<String> stream = Stream.of("I", "love", "you", "too");
        Integer lengthSum = stream.reduce(0, (sum, str) -> sum + str.length(), (a, b) -> a + b); // 部分和拼接器，并行执行时才会用到

        stream = Stream.of("I", "love", "you", "too");
        int lengthSum2 = stream.mapToInt(str -> str.length()).sum();
        System.out.println(lengthSum);

        stream = Stream.of("I", "love", "you", "too");
        stream.sorted((str1, str2) -> str1.length() - str2.length())
                .forEach(str -> System.out.println(str));

        Stream<Integer> stream1 = Stream.of(5, 2, 4, 6, 1);
        stream1.sorted((i2, i1) -> i1.compareTo(i2)).forEach(i3 -> System.out.println(i3));

        ExecutorService es = Executors.newCachedThreadPool();
        CallableThread2 ct = new CallableThread2();
        FutureTask<String> f = new FutureTask<String>(ct);
        es.submit(f);
        es.shutdown();

        Thread.sleep(5000);
        System.out.println("主线程等待5秒, 当前时间为" + System.currentTimeMillis());

        String str = f.get();
        System.out.println("Future已拿到数据, str = " + str + ", 当前时间为" + System.currentTimeMillis());

    }
}