package org.tinygame.herostory.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    /**
     * 用户字典，记录已登入的用户列表
     */
    static private final Map<Integer, User> _userMap = new ConcurrentHashMap<>();

    private UserManager(){}

    static public void addUser(User user) {
        if (null == user) return;
        _userMap.putIfAbsent(user.userId, user); //和redis的setnx很像
    }

    /**
     * 根据用户 Id 移除用户
     * @param userId
     */
    static public void removeUserById(int userId) {
        _userMap.remove(userId);
    }

    /**
     * 返回用户列表
     * @return
     */
    static public Collection<User> listUser() {
        return _userMap.values();
    }

}
