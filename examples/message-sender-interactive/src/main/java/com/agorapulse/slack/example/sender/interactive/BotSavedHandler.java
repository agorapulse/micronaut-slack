package com.agorapulse.slack.example.sender.interactive;


import com.agorapulse.slack.install.event.BotSavedEvent;
import io.micronaut.context.event.ApplicationEventListener;

import javax.inject.Singleton;

@Singleton
public class BotSavedHandler implements ApplicationEventListener<BotSavedEvent> {

    @Override
    public void onApplicationEvent(BotSavedEvent event) {
        System.out.println("New bot installed for " + event.getTeamId());
    }

}
