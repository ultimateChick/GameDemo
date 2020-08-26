package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.factory.GameMsgRecognizer;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 把要发送的内容（原先是对象
 * 编码称为字节数组
 * 前四个字节是消息长度和消息号
 * 后面接着对象的信息
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (null == msg || !(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }


        int msgCode = GameMsgRecognizer.getMsgResultCodeByMsgClazz(msg);
        if (msgCode == -1) return;

//        if (msg instanceof GameMsgProtocol.UserEntryResult) {
//            msgCode = GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE;
//        }
//        else if (msg instanceof GameMsgProtocol.WhoElseIsHereResult) {
//            msgCode = GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE;
//        }
//        else if (msg instanceof GameMsgProtocol.UserMoveToResult) {
//            msgCode = GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE;
//        }
//        else if (msg instanceof GameMsgProtocol.UserQuitResult) {
//            msgCode = GameMsgProtocol.MsgCode.USER_QUIT_RESULT_VALUE;
//        }
//        else {
//            LOGGER.error("无法识别的消息类型，msgClazz = " + msg.getClass().getName());
//            return;
//        }


        byte[] byteArray = ((GeneratedMessageV3) msg).toByteArray();
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeShort((short)0);
        byteBuf.writeShort((short)msgCode);
        byteBuf.writeBytes(byteArray);

        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);
        // 编码完毕后，对象变成BinaryWebSocketFrame对象，对着msg参数位写出即可
        super.write(ctx, frame, promise);
    }
}
