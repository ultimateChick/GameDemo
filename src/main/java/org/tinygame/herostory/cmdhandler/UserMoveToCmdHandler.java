package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {
    /**
     * //移动状态有两个地方要考虑
     * //除了广播给在线的客户端
     * //移动过程中来了新的客户端，怎么办？
     * <p>
     * 我们让每个单独的 User 对象各自维护自己的 MoveState
     * 每次移动除了发送移动消息给客户端，还要更新自己user对象中的移动状态
     * 这样新用户在按照whoelseishere指令获取user时就能够得到移动中的用户~
     *
     * @param channelHandlerContext
     * @param cmd
     */

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocol.UserMoveToCmd cmd) {

        // 接受来自客户端的目的坐标
        float moveToPosX = cmd.getMoveToPosX();
        float moveToPosY = cmd.getMoveToPosY();

        //新的协议下，每次还会附带起点坐标，时间戳由服务器维护
        float moveFromPosX = cmd.getMoveFromPosX();
        float moveFromPosY = cmd.getMoveFromPosY();
        //从当前channel获取到请求来源id
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == userId) return;

        User existUser = UserManager.getUserById(userId);
        if (null == existUser) return;

        long now = System.currentTimeMillis();

        existUser.moveState.fromPosX = moveFromPosX;
        existUser.moveState.fromPosY = moveFromPosY;
        existUser.moveState.toPosX = moveToPosX;
        existUser.moveState.toPosY = moveToPosY;
        existUser.moveState.startTime = now;

        //开始处理请求，封装成为结果，以便广播
        GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        resultBuilder.setMoveStartTime(now);
        resultBuilder.setMoveFromPosX(moveFromPosX);
        resultBuilder.setMoveFromPosY(moveFromPosY);
        resultBuilder.setMoveToPosX(moveToPosX);
        resultBuilder.setMoveToPosY(moveToPosY);
        resultBuilder.setMoveUserId(userId);
        GameMsgProtocol.UserMoveToResult newResult = resultBuilder.build();
        //把结果广播给所有客户端
        Broadcaster.broadcast(newResult);
    }
}
