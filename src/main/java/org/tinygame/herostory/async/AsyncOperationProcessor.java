package org.tinygame.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MainThreadProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncOperationProcessor {
    static private final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    static private final AsyncOperationProcessor _instance = new AsyncOperationProcessor();

    private ExecutorService[] _ess;

    private AsyncOperationProcessor() {
        _ess = new ExecutorService[8];
        for (int i = 0; i < 8; i++) {
            String tName = "Async No【 " + i + " 】";
            _ess[i] = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setName(tName);
                return t;
            });
        }
    }

    static public AsyncOperationProcessor getInstance() {
        return _instance;
    }

    public void process(IAsyncOperation aop) {

        int _bindId = aop.getBindId();
        int index = _bindId % _ess.length;

        //拿到执行此次IO操作的线程号
        ExecutorService es = _ess[index];

        es.submit(() -> {
            //先让LoginService中的异步操作完成，填充UserEntity之后
            aop.doAsync();
            //到主线程中，利用得到的UserEntity，进行剩余的Handler逻辑
            MainThreadProcessor.getInstance().process(aop::doFinish);
        });
    }
}
