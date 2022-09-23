package com.agorapulse.slack.example.sender.interactive;

import com.agorapulse.slack.install.enumerate.InstallationEnumerationService;
import com.agorapulse.slack.oauth.DistributedAppMethodsClientFactory;
import com.slack.api.bolt.model.Bot;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class MessageSender {

    private final DistributedAppMethodsClientFactory factory;
    private final InstallationEnumerationService enumerationService;

    public MessageSender(DistributedAppMethodsClientFactory factory, InstallationEnumerationService enumerationService) {
        this.factory = factory;
        this.enumerationService = enumerationService;
    }

    @EventListener
    public void sendMessage(ApplicationStartupEvent event) throws SlackApiException, IOException {
        for (Bot bot : enumerationService.findAllBots().collect(Collectors.toList())) {
            MethodsClient methods = factory.createClient(bot).orElseThrow(() -> new IllegalStateException("App not installed"));
            ConversationsListResponse conversationsList = methods.conversationsList(b -> b);
            Optional<Conversation> generalChannel = conversationsList
                .getChannels()
                .stream()
                .filter(Conversation::isGeneral)
                .findAny();
            if (generalChannel.isPresent()) {
                methods.chatPostMessage(m -> m
                    .channel(generalChannel.get().getId())
                    .blocks(Arrays.asList(
                        Blocks.header(b -> b.text(BlockCompositions.plainText("Micronaut is awesome, WDYT?"))),
                        Blocks.actions(Arrays.asList(
                            BlockElements.button(b -> b.actionId("#micronaut-yes").text(BlockCompositions.plainText("Yes"))),
                            BlockElements.button(b -> b.actionId("#micronaut-no").text(BlockCompositions.plainText("No")))
                        ))
                    ))
                );
            }
        }
    }

}
