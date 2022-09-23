package com.agorapulse.slack.example.sender;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) throws Exception {
        Micronaut.run(Application.class).getBean(MessageSender.class).sendMessage("Hello");
    }

}
