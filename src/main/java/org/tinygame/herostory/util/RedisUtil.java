package org.tinygame.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil {

    static private final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    static private JedisPool _jedisPool = null;

    static {
        try {
            _jedisPool = new JedisPool("127.0.0.1", 6379);
        }catch (Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private RedisUtil() {

    }

    static public Jedis getJedis() {
        if (null == _jedisPool) throw new RuntimeException("_jedisPool is empty");

        return _jedisPool.getResource();

    }
}
