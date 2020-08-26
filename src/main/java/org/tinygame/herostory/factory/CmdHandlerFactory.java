package org.tinygame.herostory.factory;

import com.google.protobuf.GeneratedMessageV3;
import org.tinygame.herostory.cmdhandler.ICmdHandler;
import org.tinygame.herostory.cmdhandler.UserEntryCmdHandler;
import org.tinygame.herostory.cmdhandler.UserMoveToCmdHandler;
import org.tinygame.herostory.cmdhandler.WhoElseIsHereCmdHandler;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

public final class CmdHandlerFactory {
    /**
     * 命令处理器的字典
     */
    static private Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    private CmdHandlerFactory() {
    }

    /**
     * 可以用泛型的反射来自动生成handlerMap，需要先扫描接口的实现类
     */
    static {
        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
    }

    /**
     * 创建并返回消息处理器
     *
     * @param msgClazz
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> getHandler(Class<?> msgClazz) {
        if (null == msgClazz) return null;
        return _handlerMap.get(msgClazz);
    }
}
