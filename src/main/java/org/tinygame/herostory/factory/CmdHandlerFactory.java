package org.tinygame.herostory.factory;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandler.ICmdHandler;
import org.tinygame.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CmdHandlerFactory {

    static private final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);

    /**
     * 命令处理器的字典
     */
    static private Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    private CmdHandlerFactory() {
    }

    /**
     * 可以用泛型的反射来自动生成handlerMap，需要先扫描接口的实现类，
     * 通过handle方法中第二个传参建立class和handler的关系
     */
    static {
        // 写死的蠢方法
//        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
//        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
//        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());

        LOGGER.info("==== 完成 Cmd 和 Handler 的关联 ====");

        // 获取包名称
        final String packageName = ICmdHandler.class.getPackage().getName();
        // 获取所有的 ICmdHandler 子类
        Set<Class<?>> clazzSet = PackageUtil.listSubClazz(
                packageName,
                true,
                ICmdHandler.class
        );

        System.out.println(clazzSet);

        for (Class<?> clazz : clazzSet) {
            if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
                // 如果是抽象类,
                continue;
            }

            // 获取方法数组
            Method[] methodArray = clazz.getDeclaredMethods();
            // 消息类型
            Class<?> msgType = null;

            for (Method currMethod : methodArray) {
                if (!currMethod.getName().equals("handle")) {
                    // 如果不是 handle 方法,
                    continue;
                }

                // 获取函数参数类型
                Class<?>[] paramTypeArray = currMethod.getParameterTypes();

                if (paramTypeArray.length < 2 ||
                        paramTypeArray[1] == GeneratedMessageV3.class || // 这里最好加上这个判断
                        !GeneratedMessageV3.class.isAssignableFrom(paramTypeArray[1])) {
                    continue;
                }

                msgType = paramTypeArray[1];
                break;
            }

            if (null == msgType) {
                continue;
            }

            try {
                // 创建指令处理器
                ICmdHandler<?> newHandler = (ICmdHandler<?>) clazz.newInstance();

                LOGGER.info(
                        "关联 {} <==> {}",
                        msgType.getName(),
                        clazz.getName()
                );

                _handlerMap.put(msgType, newHandler);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

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

        //  使用JavaAssist创建Handler  //
        //  这种方式甚至不需要我们去创建具体的实现类！ //
        // 可惜这里Handle的方法具体逻辑相差过大 ，我们还是用一般的方式来 //
    }
}
