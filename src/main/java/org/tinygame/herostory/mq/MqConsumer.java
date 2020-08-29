package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.model.MqVictoryMsg;
import org.tinygame.herostory.msg.GameMsgProtocol;
import org.tinygame.herostory.rank.GetRankService;

import java.util.List;

public class MqConsumer {

    static private final Logger LOGGER = LoggerFactory.getLogger(MqProducer.class);


    private MqConsumer() {

    }

    static public void init() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("herostory");
        consumer.setNamesrvAddr("10.0.1.10:9876");

        try {
            //订阅主题下的所有内容
            consumer.subscribe("herostory_victory", "*");
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

                    for (MessageExt msg : list) {
                        //producer逻辑中用FastJson处理了Msg对象
                        MqVictoryMsg victoryMsg = JSONObject.parseObject(msg.getBody(), MqVictoryMsg.class);
                        int loserId = victoryMsg.loserId;
                        int winnerId = victoryMsg.winnerId;
                        GetRankService.getInstance().refreshRank(winnerId, loserId);

                        LOGGER.info("从消息队列中收到胜利消息，winnerId={},loserId={}", winnerId, loserId);
                    }


                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

}
