package com.agorapulse.slack.example.sender;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class MessageSender {

    private final MethodsClient methodsClient;

    public MessageSender(MethodsClient methodsClient) {
        this.methodsClient = methodsClient;
    }

    public void sendMessage(String message) throws SlackApiException, IOException {
        ConversationsListResponse conversationsList = methodsClient.conversationsList(b -> b);
        Optional<Conversation> generalChannel = conversationsList
            .getChannels()
            .stream()
            .filter(Conversation::isGeneral)
            .findAny();
        if (generalChannel.isPresent()) {
            methodsClient.chatPostMessage(m -> m.text(message).channel(generalChannel.get().getId()));
        }
    }

}
