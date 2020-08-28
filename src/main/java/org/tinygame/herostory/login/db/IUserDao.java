package org.tinygame.herostory.login.db;

import org.tinygame.herostory.model.User;

public interface IUserDao {
    UserEntity getUserByUserName(String userName);
    UserEntity getUserByLogin(String userName, String password);
}
