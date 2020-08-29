package org.tinygame.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MainThreadProcessor;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.model.RankEntity;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class GetRankService {
    static private final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    static private final GetRankService _instance = new GetRankService();

    private GetRankService() {
    }

    static public GetRankService getInstance() {
        return _instance;
    }

    public void getRank(Function<List<RankEntity>, Void> callback) {
        AsyncGetRankOperation aop = new AsyncGetRankOperation() {
            @Override
            public void doFinish() {
                callback.apply(_rankEntities);
            }
        };

        AsyncOperationProcessor.getInstance().process(aop);
    }

    public void refreshRank(int winnerId, int loserId) {
        if (winnerId <=0 || loserId <= 0){
            return;
        }

        try (Jedis redis = RedisUtil.getJedis()) {
            //hincrby命令可以直接创造属性，并向上追加数值
            redis.hincrBy("userId_" + winnerId, "win", 1);
            redis.hincrBy("userId_" + loserId, "lose", 1);

            String win = redis.hget("userId_" + winnerId, "win");
            int winNum = Integer.parseInt(win);

            //更新sortedSet中，对应id的胜利次数
            redis.zadd("Rank", winNum, String.valueOf(winnerId));

        } catch (Exception exception){
            LOGGER.error(exception.getMessage(), exception);
        }
    }


    private class AsyncGetRankOperation implements IAsyncOperation {

        List<RankEntity> _rankEntities;

        public List<RankEntity> getRankEntities() {
            return _rankEntities;
        }

        @Override
        public void doAsync() {

            //每次请求时创建一个新的rankEntity集合，免去清理工作
            //不过每次getRank创建了一个新的aop就是了...
            _rankEntities = new ArrayList<>();

            try (Jedis jedis = RedisUtil.getJedis()) {

                //我们jedis中存储了两个集合，一个sortedSet存储了key为userId，score为win次数的信息
                //一个hash中存储了userId->userInfo的键值对
                Set<Tuple> ranks = jedis.zrevrangeWithScores("Rank", 0, -1);//拿到从大到小排好序的集合

                int rankId = 0;

                for (Tuple t : ranks) {
                    //
                    String userId = t.getElement();
                    double winCount = t.getScore();

                    String userInfo = jedis.hget("User_" + userId, "BasicInfo"); //JsonStr，存有必要的用户信息
                    if (null == userInfo) continue;

                    //创建排名条目
                    RankEntity rankEntity = new RankEntity();
                    rankEntity.rankId = ++rankId;
                    rankEntity.userId = Integer.parseInt(userId);
                    rankEntity.win = (int) winCount;

                    JSONObject jsonObj = JSONObject.parseObject(userInfo);

                    rankEntity.userName = jsonObj.getString("userName");
                    rankEntity.heroAvatar = jsonObj.getString("heroAvatar");

                    _rankEntities.add(rankEntity);
                }

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

    }

}
