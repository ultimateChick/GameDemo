package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf content = frame.content();

        content.readShort(); //读取消息的长度，头两个字节在协议中定义为消息的长度
        int msgCode = content.readShort();//读取消息的编号，头两个字节在协议中定义为消息的编号

        //拿到消息体
        byte[] msgBody = new byte[content.readableBytes()];
        content.readBytes(msgBody); //具体的消息内容

        GeneratedMessageV3 cmd = null;

        switch (msgCode) {
            case GameMsgProtocol
                    .MsgCode.USER_ENTRY_CMD_VALUE:
                cmd = GameMsgProtocol.UserEntryCmd.parseFrom(msgBody);
                break;
            case GameMsgProtocol.MsgCode
                    .WHO_ELSE_IS_HERE_CMD_VALUE:
                cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(msgBody);
            case GameMsgProtocol.MsgCode
                    .USER_MOVE_TO_CMD_VALUE:
                cmd = GameMsgProtocol.UserMoveToCmd.parseFrom(msgBody);
        }
        /**
         * fireChannelRead，把消息传递到下一个处理器
         * 因为pipeline的原因，我们会有一个链式的处理器队列，队列有头尾之分，消息通常从头部进入
         * 假设有队列ABC，如果A不进行fireChannelRead，那么BC永远接收不到消息
         */
        if (null != cmd){
            ctx.fireChannelRead(cmd);
        }
    }
}
