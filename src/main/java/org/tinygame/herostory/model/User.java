package org.tinygame.herostory.model;

public class User {
    /**
     * 用户 Id
     */
    public int userId;
    /**
     * 英雄形象
     */
    public String heroAvatar;

    /**
     * 血量，默认100
     */
    public volatile int hp = 100;

    /**
     * 移动状态
     */
    public final MoveState moveState = new MoveState();

}
