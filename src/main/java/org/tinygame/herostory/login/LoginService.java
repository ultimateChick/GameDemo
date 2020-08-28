package org.tinygame.herostory.login;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.factory.MySqlSessionFactory;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.function.Function;

/**
 * 由于这里涉及到IO，且Handler默认被MainThread接管
 * 我们希望另起线程完成io，把结果提交给MainThread
 * 主线程只负责内存空间中的计算任务，涉及到io的，可能拖累主线程工作的就分散到其他线程去处理
 */

public class LoginService {

    static private final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    static private final LoginService _instance = new LoginService();

    private LoginService() {
    }

    static public LoginService getInstance() {
        return _instance;
    }

    public void userLogin(String userName, String password, Function<UserEntity, Void> callback) {
        //经过异步的方式获得UserEntity

        AsyncOperationProcessor asyncOperationProcessor = AsyncOperationProcessor.getInstance();


        AsyncLoginOperation aLoginOp = new AsyncLoginOperation(userName, password) {

            //通过apply方法把获得的UserEntity对象交给MainThread处理
            @Override
            public void doFinish() {
                callback.apply(_userEntity);
            }
        };
    }

    private class AsyncLoginOperation implements IAsyncOperation {

        String _username;
        String _password;
        UserEntity _userEntity;

        AsyncLoginOperation(String username, String password) {
            _username = username;
            _password = password;
        }

        @Override
        public int getBindId() {
            return 0;
        }

        @Override
        public void doAsync() {
            try (SqlSession sqlSession = MySqlSessionFactory.openSession()) {
                IUserDao userDaoMapper = sqlSession.getMapper(IUserDao.class);
//                UserEntity loginUser = userDaoMapper.getUserByLogin(_username, _password);
                UserEntity preUser = userDaoMapper.getUserByUserName(_username);
                if (null == preUser) {
                    //默认给创建一个？
                    LOGGER.info("用户名为【" + _username + "】的玩家不存在");
                    //看看cmd约束了什么
                } else {
                    //验证一下密码
                    UserEntity loginUser = userDaoMapper.getUserByLogin(_username, _password);
                    if (null == loginUser) LOGGER.info("用户名为【" + _username + "】的玩家登陆失败，密码错误");
                    else _userEntity = loginUser;
                }
            }
        }
    }

}
