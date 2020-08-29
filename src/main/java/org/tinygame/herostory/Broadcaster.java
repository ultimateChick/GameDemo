package org.tinygame.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.tinygame.herostory.msg.GameMsgProtocol;

public final class Broadcaster {
    /**
     * 客户端信道数组，可用于消息广播等
     * 一定使用static，否则无法实现群发
     * static可以保证所有GameMsgHandler共享一个对象
     * final防止被不同的客户端修改造成混乱
     */
    static private final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 私有化类默认构造器
     */
    private Broadcaster() {
    }

    /**
     * 添加信道
     *
     * @param channel 要添加的信道
     */
    static public void addChannel(Channel channel) {
        _channelGroup.add(channel);
    }

    /**
     * 移除信道
     * @param channel 待移除的信道
     */
    static public void removeChannel(Channel channel) {
        _channelGroup.remove(channel);
    }

    /**
     * 广播消息
     * @param msg
     */
    static public void broadcast(Object msg) {
        if (null == msg) return;
        _channelGroup.writeAndFlush(msg);

    }

}
