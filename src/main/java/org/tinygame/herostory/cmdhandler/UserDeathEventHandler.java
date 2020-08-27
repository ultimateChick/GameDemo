package org.tinygame.herostory.cmdhandler;

import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 此Handler比较特殊，是在其他伤害场景下才可能触发的
 * 把死亡事件包装成独立的类后可以在多样的伤害Cmd下调用
 */
public class UserDeathEventHandler {
    public static void dying(int targetUserId) {
        User dyingUser = UserManager.getUserById(targetUserId);
        if (null == dyingUser) return;

        GameMsgProtocol.UserDieResult.Builder builder = GameMsgProtocol.UserDieResult.newBuilder();
        builder.setTargetUserId(targetUserId);

        //玩家死亡后应该要把角色删掉？重置？看设计要求，这里先进行一次重置

        GameMsgProtocol.UserDieResult deathResult = builder.build();
        Broadcaster.broadcast(deathResult);
    }
}
