package com.yuqiliu.event;

import com.alibaba.fastjson.JSONObject;
import com.yuqiliu.entity.DiscussPost;
import com.yuqiliu.entity.Event;
import com.yuqiliu.entity.Message;
import com.yuqiliu.service.DiscussPostService;
import com.yuqiliu.service.ElasticsearchService;
import com.yuqiliu.service.MessageService;
import com.yuqiliu.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yuqiliu
 * @create 2020-06-01  20:27
 */

@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null)
        {
            log.error("消息格式错误");
            return;
        }

        // 发送站内消息
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty())
        {
            for (Map.Entry<String,Object> entry : event.getData().entrySet())
            {
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式错误!");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null)
        {
            log.error("消息格式错误!");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }
}
