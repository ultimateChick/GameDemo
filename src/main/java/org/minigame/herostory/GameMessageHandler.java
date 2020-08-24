package org.minigame.herostory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class GameMessageHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.out.println("收到客户端的消息， msg = " + msg);
        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf content = frame.content();

        byte[] byteArray = new byte[content.readableBytes()];
        content.readBytes(byteArray);

        System.out.println("收到的字节：");
        for (byte b: byteArray){
            System.out.print((int) b);
            System.out.print(", ");
        }
        System.out.println();
    }
}
