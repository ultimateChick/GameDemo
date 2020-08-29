package org.tinygame.herostory.factory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 此工厂服务于Decoder和Encoder中
 * Decoder：根据msgCode，获取对应msg的解析器从而得到具体的cmd对象
 * 经过Handler的处理，我们得到了各种各样的result
 * Encoder：根据result类型，得到相应的msg码，拼接成byte[]以便发出
 */
public final class GameMsgRecognizer {

    // 切记，这里不应该把value设置成builder，共用builder会产生并发问题，应当每次都给客户端返回一个新的builder使用
    static private final Map<Integer, Message> _msgCodeToCmdMessage = new HashMap<>();
    static private final Map<Class<?>, Integer> _msgResultToMsgCode = new HashMap<>();

    private GameMsgRecognizer() {

    }

    /**
     * 这里的Map是写死的，更优化的方式是用反射来动态创建map
     */
//    static {
//        _msgCodeToCmdMessage.put(GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE, GameMsgProtocol.UserEntryCmd.getDefaultInstance());
//        _msgCodeToCmdMessage.put(GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE, GameMsgProtocol.WhoElseIsHereCmd.getDefaultInstance());
//        _msgCodeToCmdMessage.put(GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE, GameMsgProtocol.UserMoveToCmd.getDefaultInstance());
//
//        _msgResultToMsgCode.put(GameMsgProtocol.UserEntryResult.class, GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE);
//        _msgResultToMsgCode.put(GameMsgProtocol.WhoElseIsHereResult.class, GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE);
//        _msgResultToMsgCode.put(GameMsgProtocol.UserMoveToResult.class, GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE);
//        _msgResultToMsgCode.put(GameMsgProtocol.UserQuitResult.class, GameMsgProtocol.MsgCode.USER_QUIT_RESULT_VALUE);
//
//    }

    /**
     * 反射方式动态创建map
     */
    static{
        Class<?>[] innerClasses = GameMsgProtocol.class.getDeclaredClasses();
        for (Class<?> innerClass : innerClasses) {
            //确保当前的类是一个消息类
            if (null == innerClass || !GeneratedMessageV3.class.isAssignableFrom(innerClass)) continue;

            String lowerClassName = innerClass.getName().toLowerCase();
            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {

                String msgCodeName = msgCode.name();
                msgCodeName = msgCodeName.replaceAll("_",""); //让下划线命名贴贴
                msgCodeName = msgCodeName.toLowerCase();

                //匹配上了
                if (!msgCodeName.startsWith(lowerClassName)) {
                    continue;
                }

                try {
                    Object messageObj = innerClass.getDeclaredMethod("getDefaultInstance").invoke(innerClass);

                    _msgCodeToCmdMessage.put(msgCode.getNumber(), (Message)messageObj);

                    _msgResultToMsgCode.put(innerClass, msgCode.getNumber());

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据消息号获取消息建造者（CMD），解码cmd
     * @param msgCode
     * @return
     */
    static public Message.Builder getMsgBuilderByMsgCode(Integer msgCode) {
        if (msgCode < 0) return null;
        Message message = _msgCodeToCmdMessage.get(msgCode);
        if (null == message) return null;
        return message.newBuilderForType();
    }

    /**
     * 根据消息类型得到消息号（Result），编码result
     * @param msg
     * @return
     */
    static public Integer getMsgResultCodeByMsgClazz(Object msg) {
        if (null == msg) return -1;
        return null == _msgResultToMsgCode.get(msg.getClass()) ? -1 : _msgResultToMsgCode.get(msg.getClass());
    }

}
