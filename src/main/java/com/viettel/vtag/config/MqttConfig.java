package com.viettel.vtag.config;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Value("${vtag.mqtt.url}")
    private String url;

    @Value("${vtag.mqtt.client-id}")
    private String clientId;

    @Value("${vtag.mqtt.topic}")
    private String topic;

    @Value("${vtag.mqtt.username}")
    private String username;

    @Value("${vtag.mqtt.password}")
    private String password;

    @Value("${vtag.mqtt.qos}")
    private int qos;

    @Value("${vtag.mqtt.timeout}")
    private int timeout;

    public MqttConnectOptions connectOptions() {
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);
        return options;
    }

    @Bean
    public IMqttAsyncClient mqttClient(MqttCallback callback) throws MqttException {
        var client = new MqttAsyncClient(url, clientId, new MemoryPersistence());
        client.setCallback(callback);
        client.connect(connectOptions());
        client.subscribe(this.topic, qos);
        return client;
    }

    // @Bean
    // public DefaultMqttPahoClientFactory clientFactory() {
    //     var factory = new DefaultMqttPahoClientFactory();
    //     factory.setConnectionOptions(mqttConnectOptions());
    //     return factory;
    // }
    //
    // @Bean
    // public MqttConnectOptions mqttConnectOptions() {
    //     var options = new MqttConnectOptions();
    //     options.setUserName(username);
    //     options.setPassword(password.toCharArray());
    //     return options;
    // }
    //
    // @Bean
    // public MessageProducer inbound() {
    //     var adapter = new MqttPahoMessageDrivenChannelAdapter(url, clientId, topic);
    //     adapter.setCompletionTimeout(timeout);
    //     adapter.setConverter(new DefaultPahoMessageConverter());
    //     adapter.setQos(qos);
    //     adapter.setOutputChannel(mqttInputChannel());
    //     return adapter;
    // }
    //
    // @Bean
    // public MessageChannel mqttInputChannel() {
    //     return new DirectChannel();
    // }
    //
    // @Bean
    // @ServiceActivator(inputChannel = "mqttInputChannel")
    // public MessageHandler handler() {
    //     return message -> System.out.println(message.getPayload());
    // }
}
