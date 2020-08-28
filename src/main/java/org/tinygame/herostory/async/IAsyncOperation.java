package org.tinygame.herostory.async;

public interface IAsyncOperation {

    /**
     * 我们希望把不同的IO业务分散到不同的线程中处理，但是涉及同一对象的数据库请求要保持在同一线程中处理
     * 否则某些请求就变成了不安全的多线程操作
     * 具体业务经过某种特定算法可以使得业务分散但是安全
     * @return
     */
    default int getBindId(){
        return 0;
    }

    //处理具体的异步业务
    void doAsync();

    //如果业务需要返回值，可以在doFinish方法当中发生回调，继续后续业务
    default void doFinish(){

    }
}
