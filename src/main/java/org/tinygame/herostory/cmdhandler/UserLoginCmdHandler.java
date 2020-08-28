package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MainThreadProcessor;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd> {

    static private final Logger LOGGER = LoggerFactory.getLogger(UserLoginCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserLoginCmd tCmd) {
        // 获取名称和密码
        String userName = tCmd.getUserName();
        String password = tCmd.getPassword();
        // 通过dao去数据库检查，使用LoginService的登录功能
        LoginService loginService = LoginService.getInstance();
        loginService.userLogin(userName, password, userEntity -> {
            LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

            GameMsgProtocol.UserLoginResult.Builder resultBuilder = GameMsgProtocol.UserLoginResult.newBuilder();
            if (null == userEntity) {
                //代表着不存在这个用户

            } else {

                LOGGER.info(
                        "用户登陆, userName = {}, password = {}",
                        userName,
                        password
                );

                User user = new User();
                user.userId = userEntity.userId;
                user.heroAvatar = userEntity.heroAvatar;
                user.userName = userEntity.userName;
                UserManager.addUser(user);

                //标记当前用户id到channel
                ctx.channel().attr(AttributeKey.valueOf("userId")).set(userEntity.userId);

                resultBuilder.setUserId(userEntity.userId);
                resultBuilder.setUserName(userEntity.userName);
                resultBuilder.setHeroAvatar(userEntity.heroAvatar);

                GameMsgProtocol.UserLoginResult loginResult = resultBuilder.build();

                ctx.writeAndFlush(loginResult);
            }
            return null;
        });
    }
}
