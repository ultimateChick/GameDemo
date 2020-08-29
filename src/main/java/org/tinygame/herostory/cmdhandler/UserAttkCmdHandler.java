package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.MqVictoryMsg;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.mq.MqProducer;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 由于攻击时会产生攻击事件，扣血事件
 * 而扣血又会产生死亡事件
 * 所以这里环节比较多
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd tCmd) {
        Integer attkingUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == attkingUserId) {
            return;
        }

        //被攻击的用户id
        int targetUserId = tCmd.getTargetUserId();

        User targetUser = UserManager.getUserById(targetUserId);
        if (null == targetUser) {
            attkEventBroadCast(attkingUserId, -1);
            return;
        }

        //需要游戏策划提供伤害公式
        int damage = 10;
        targetUser.hp -= damage;
        if (targetUser.hp <= 0) {
            UserDeathEventHandler.dying(targetUserId);

            MqVictoryMsg newMsg = new MqVictoryMsg();
            newMsg.winnerId = attkingUserId;
            newMsg.loserId = targetUserId;

            //发送一个
            MqProducer.sendMsg("herostory_victory", newMsg);
        }

        attkEventBroadCast(attkingUserId, targetUserId);
        userHpSubtractResult(targetUserId, damage);

    }

    /**
     * 把被攻击的事件通知给所有客户端
     * 把血量扣除后的事件通知给所有客户端
     */
    //广播攻击事件
    static private void attkEventBroadCast(int attkUserId, int targetUserId) {
        GameMsgProtocol.UserAttkResult.Builder builder = GameMsgProtocol.UserAttkResult.newBuilder();
        builder.setAttkUserId(attkUserId);
        builder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserAttkResult attkResult = builder.build();
        Broadcaster.broadcast(attkResult);
    }

    //广播血量扣除
    static private void userHpSubtractResult(int targetUserId, int hpSubtract) {
        GameMsgProtocol.UserSubtractHpResult.Builder builder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        builder.setTargetUserId(targetUserId);
        builder.setSubtractHp(hpSubtract);

        GameMsgProtocol.UserSubtractHpResult hpSubResult = builder.build();
        Broadcaster.broadcast(hpSubResult);
    }
}
