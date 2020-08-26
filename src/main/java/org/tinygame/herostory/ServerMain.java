package org.tinygame.herostory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.factory.CmdHandlerFactory;

public class ServerMain {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);
    public static void main(String[] args) {
        try {
//            CmdHandlerFactory.init();

            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup(3);

            ServerBootstrap bs = new ServerBootstrap();
            ChannelFuture future = bs.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    //约定俗成的，H5连到服务器需要这样的配置
                                    new HttpServerCodec(), //Http 服务器编解码器
                                    new HttpObjectAggregator(65535), // 内容长度限制
                                    new WebSocketServerProtocolHandler("/websocket"), // WebSocket协议处理器，在这里处理
                                    new GameMsgDecoder(),
                                    new GameMsgEncoder(),
                                    new GameMsgHandler()
                            );
                        }
                    }).bind(12345).sync();
            if (future.isSuccess()) {
                LOGGER.info("服务器启动成功!");
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
