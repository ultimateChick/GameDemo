package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqProducer {
    static private final Logger LOGGER = LoggerFactory.getLogger(MqProducer.class);

    static private DefaultMQProducer _producer = null;

    private MqProducer() {
    }

    static {
        try {
            DefaultMQProducer producer = new DefaultMQProducer("herostory_victory");
            //Namesrv做集群发现的
            producer.setNamesrvAddr("10.0.1.10:9876");
            producer.start();
            producer.setRetryTimesWhenSendAsyncFailed(3);

            _producer = producer;

            LOGGER.info("消息队列(生产者)连接成功！");

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 发送消息
     * @param topic 主题
     * @param msg   消息对象
     */
    static public void sendMsg(String topic, Object msg) {
        if (null == topic || null == msg) {
            return;
        }

        Message newMsg = new Message();
        newMsg.setTopic(topic);
        newMsg.setBody(JSONObject.toJSONBytes(msg));//消息体

        try {
            //发送消息
            _producer.send(newMsg);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
