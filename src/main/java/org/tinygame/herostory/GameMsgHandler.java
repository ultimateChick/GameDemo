package org.tinygame.herostory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 重构笔记：
 * 1.抽取广播和用户管理为工具类，使得Handler职责更加明确
 * 2.抽取对msg的处理，使其成为根据不同msg类型形成的不同msgHandler，再对这些handler进行接口规范
 * 3.抽取对msg的类型判断，使其成为cmd的工厂，只需要传入msg就可以在ChannelHandler的业务中得到不同的builder进行
 */

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Broadcaster.addChannel(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        Broadcaster.removeChannel(ctx.channel());

        // 先拿到用户 Id
        Integer userId = (Integer)ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == userId) {
            return;
        }

        UserManager.removeUserById(userId);

        GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuilder.setQuitUserId(userId);

        GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

        System.out.println("收到客户短消息， msgClazz = " + msg.getClass().getName() + ", msg = " + msg);

        if (msg instanceof GameMsgProtocol.UserEntryCmd) {
            GameMsgProtocol.UserEntryCmd cmd = (GameMsgProtocol.UserEntryCmd) msg;
            int userId = cmd.getUserId();
            String heroAvatar = cmd.getHeroAvatar();
            GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
            resultBuilder.setUserId(userId);
            resultBuilder.setHeroAvatar(heroAvatar);

            /**
             * 将用户加入字典
             */
            User newUser = new User();
            newUser.userId = userId;
            newUser.heroAvatar = heroAvatar;
            UserManager.addUser(newUser);

            /**
             * 将用户Id附着到Channel上
             */
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(userId);

            GameMsgProtocol.UserEntryResult newResult = resultBuilder.build();
            Broadcaster.broadcast(newResult);
        }
        else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
            GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
            //todo:用户字典
            for (User existUser : UserManager.listUser()) {
                if (null == existUser) continue;
                GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder =
                        GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                userInfoBuilder.setUserId(existUser.userId);
                userInfoBuilder.setHeroAvatar(existUser.heroAvatar);
                resultBuilder.addUserInfo(userInfoBuilder);
            }
            GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
            channelHandlerContext.writeAndFlush(newResult);
        }
        else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
            GameMsgProtocol.UserMoveToCmd userMoveToCmd = (GameMsgProtocol.UserMoveToCmd) msg;
            float moveToPosX = userMoveToCmd.getMoveToPosX();
            float moveToPosY = userMoveToCmd.getMoveToPosY();
            Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();
            if (null == userId) {
                LOGGER.warn(userId + " 号user是空的，但还是附着了");
                return;
            }
            GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
            resultBuilder.setMoveToPosX(moveToPosX);
            resultBuilder.setMoveToPosY(moveToPosY);
            resultBuilder.setMoveUserId(userId);
            GameMsgProtocol.UserMoveToResult newResult = resultBuilder.build();
            Broadcaster.broadcast(newResult);
        }
    }
}
