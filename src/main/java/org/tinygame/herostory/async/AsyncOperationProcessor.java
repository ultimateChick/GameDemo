package org.tinygame.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//        int bindId = Math.abs(asyncOp.getBindId());
//        int esIndex = bindId % _esArray.length;
//
//        ess
    }
}
