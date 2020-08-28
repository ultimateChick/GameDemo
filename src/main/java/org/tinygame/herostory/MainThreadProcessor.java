package org.tinygame.herostory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 希望游戏的计算任务由一个线程来完成
 * 因为游戏过程中玩家之间发生交互，本质上是一些数据在不同玩家间的共享
 * 如果提交给netty处理是一个多线程的过程，可能导致数据的不安全
 * netty的worker负责从客户端接收到网路数据，解析后的实际计算由单线程完成。
 */
public class MainThreadProcessor {

    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MainThreadProcessor.class);
    /**
     * 单例
     */
    static private MainThreadProcessor _mainThreadProcessor = new MainThreadProcessor();
    /**
     * 创建一个单线程的线程池，处理各种游戏过程中发生的计算
     */
    private final ExecutorService _es = Executors.newSingleThreadExecutor((r) -> {
        Thread t = new Thread(r);
        t.setName("MainThread");
        return t;
    });

    /**
     * 私有化类构造器
     */
    private MainThreadProcessor() {
    }

    /**
     * 返回主线程单例对象
     */
    public static MainThreadProcessor getInstance() {
        return _mainThreadProcessor;
    }

    /**
     * 泛用的任务处理
     * @param r
     */
    public void process(Runnable r) {
        assert null != r;
        _es.submit(r);
    }

}
