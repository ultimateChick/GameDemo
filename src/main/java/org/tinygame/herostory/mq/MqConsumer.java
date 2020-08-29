package org.tinygame.herostory.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqConsumer {

    static private final Logger LOGGER = LoggerFactory.getLogger(MqProducer.class);


    private MqConsumer() {

    }

    static {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("herostory");
        consumer.setNamesrvAddr("10.0.1.10:9876");

        try {
            //订阅主题下的所有内容
            consumer.subscribe("herostory_victory", "*");

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

}
