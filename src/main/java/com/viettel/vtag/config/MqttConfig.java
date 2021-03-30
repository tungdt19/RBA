package com.viettel.vtag.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    private final String url;
    private final String clientId;
    private final String topic;
    private final String username;
    private final String password;
    private final int qos;
    private final int timeout;

    public MqttConfig(
        @Value("${vtag.mqtt.url}") String url,
        @Value("${vtag.mqtt.client-id}") String clientId,
        @Value("${vtag.mqtt.topic}") String topic,
        @Value("${vtag.mqtt.username}") String username,
        @Value("${vtag.mqtt.password}") String password,
        @Value("${vtag.mqtt.qos}") int qos,
        @Value("${vtag.mqtt.timeout}") int timeout
    ) {
        this.url = url;
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
        this.timeout = timeout;
        this.username = username;
        this.password = password;
    }

    @Bean
    public DefaultMqttPahoClientFactory clientFactory() {
        var factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        var options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        return options;
    }

    @Bean
    public MessageProducer inbound() {
        var adapter = new MqttPahoMessageDrivenChannelAdapter(url, clientId, topic);
        adapter.setCompletionTimeout(timeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> System.out.println(message.getPayload());
    }
}