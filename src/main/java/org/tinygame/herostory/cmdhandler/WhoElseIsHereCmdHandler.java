package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd cmd) {
        GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
        for (User existUser : UserManager.listUser()) {
            if (null == existUser) continue;
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder =
                    GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();

            // 设置用户的id和头像给WhoElseIsHere命令
            userInfoBuilder.setUserId(existUser.userId);
            userInfoBuilder.setHeroAvatar(existUser.heroAvatar);

            //这里是UserInfo的MoveState
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder moveStateBuilder
                    = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            moveStateBuilder.setFromPosX(existUser.moveState.fromPosX);
            moveStateBuilder.setFromPosY(existUser.moveState.fromPosY);
            moveStateBuilder.setToPosX(existUser.moveState.toPosX);
            moveStateBuilder.setToPosY(existUser.moveState.toPosY);
            moveStateBuilder.setStartTime(existUser.moveState.startTime);
            // 把当前用户的移动信息给到
            userInfoBuilder.setMoveState(moveStateBuilder);
            // 把本次循环中的用户信息给到总的resultBuilder
            resultBuilder.addUserInfo(userInfoBuilder);
        }
        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
        ctx.writeAndFlush(newResult);
    }
}
