package org.tinygame.herostory.cmdhandler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

@FunctionalInterface
public interface ICmdHandler<T extends GeneratedMessageV3> {
    void handle(ChannelHandlerContext ctx, T tCmd);
}
