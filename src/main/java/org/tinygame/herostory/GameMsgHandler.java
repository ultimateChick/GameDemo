package org.tinygame.herostory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 客户端信道数组，可用于消息广播等
     * 一定使用static，否则无法实现群发
     * static可以保证所有GameMsgHandler共享一个对象
     * final防止被不同的客户端修改造成混乱
     */
    static private final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 用户字典，记录已登入的用户列表
     */
    static private final Map<Integer, User> _userMap = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        _channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        //msg已经被decoder转码成为GameMsgProtocol对象

        System.out.println("收到客户短消息， msgClazz = " + msg.getClass().getName() + ", msg = " + msg);

        if (msg instanceof GameMsgProtocol.UserEntryCmd) {
            GameMsgProtocol.UserEntryCmd cmd = (GameMsgProtocol.UserEntryCmd) msg;
            int userId = cmd.getUserId();
            String heroAvatar = cmd.getHeroAvatar();
            GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
            resultBuilder.setUserId(userId);
            resultBuilder.setHeroAvatar(heroAvatar);

            /**
             * 将用户Id附着到Channel上
             */
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(userId);

            /**
             * 将用户加入字典
             */
            User newUser = new User();
            newUser.userId = userId;
            newUser.heroAvatar = heroAvatar;
            _userMap.put(userId, newUser);

            GameMsgProtocol.UserEntryResult newResult = resultBuilder.build();
            _channelGroup.writeAndFlush(newResult);
        } else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
            GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
            //todo:用户字典
            for (User existUser : _userMap.values()) {
                if (null == existUser) continue;
                GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder =
                        GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                userInfoBuilder.setUserId(existUser.userId);
                userInfoBuilder.setHeroAvatar(existUser.heroAvatar);
                resultBuilder.addUserInfo(userInfoBuilder);
            }
            GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
            channelHandlerContext.writeAndFlush(newResult);
        } else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
            GameMsgProtocol.UserMoveToCmd userMoveToCmd = (GameMsgProtocol.UserMoveToCmd) msg;
            float moveToPosX = userMoveToCmd.getMoveToPosX();
            float moveToPosY = userMoveToCmd.getMoveToPosY();
            Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();
            if (null == userId) return;
            GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
            resultBuilder.setMoveToPosX(moveToPosX);
            resultBuilder.setMoveToPosY(moveToPosY);
            resultBuilder.setMoveUserId(userId);
            GameMsgProtocol.UserMoveToResult result = resultBuilder.build();
            channelHandlerContext.writeAndFlush(result);
        }
//        System.out.println("收到客户端的消息， msg = " + msg);
//        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
//        ByteBuf content = frame.content();
//
//        byte[] byteArray = new byte[content.readableBytes()];
//        content.readBytes(byteArray);
//
//        System.out.println("收到的字节：");
//        for (byte b: byteArray){
//            System.out.print((int) b);
//            System.out.print(", ");
//        }
//        System.out.println();
    }
}
