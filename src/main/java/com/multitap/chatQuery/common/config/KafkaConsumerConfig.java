package com.multitap.chatQuery.common.config;

import com.multitap.chatQuery.chatQuery.kafka.messageIn.ChatDto;
import com.multitap.chatQuery.chatQuery.kafka.messageIn.MemberDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.cluster.uri}")
    private String kafkaClusterUri;

    @Bean
    public ConsumerFactory<String, ChatDto> chatConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaClusterUri); // 단일 포트로 수정
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:19092,kafka-2:19092,kafka-3:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ChatDto.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatDto> chatDtoListener() {
        ConcurrentKafkaListenerContainerFactory<String, ChatDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MemberDto> memberConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaClusterUri); // 단일 포트로 수정
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:19092,kafka-2:19092,kafka-3:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(MemberDto.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MemberDto> memberDtoListener() {
        ConcurrentKafkaListenerContainerFactory<String, MemberDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(memberConsumerFactory());
        return factory;
    }


}
