package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd> {

    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd cmd) {

        // 加入登录功能后，id和avatar由dao填充的UserEntity来决定

        GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();

        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        User userById = UserManager.getUserById(userId);
        if (null == userById) {
            LOGGER.error("用户不存在, userId = {}", userId);
            return;
        }

        String heroAvatar = userById.heroAvatar;

        resultBuilder.setUserId(userId);
        resultBuilder.setHeroAvatar(heroAvatar);


        GameMsgProtocol.UserEntryResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}
