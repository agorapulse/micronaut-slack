package com.agorapulse.slack.example.sender.interactive;

import com.agorapulse.slack.handlers.MicronautBlockActionHandler;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

@Singleton
public class MessageActionHandler implements MicronautBlockActionHandler {

    @Override
    public Response apply(BlockActionRequest blockActionRequest, ActionContext context) throws IOException {
        Optional<BlockActionPayload.Action> firstAction = blockActionRequest.getPayload().getActions().stream().findFirst();
        if (firstAction.isPresent()) {
            BlockActionPayload.Action action = firstAction.get();
            if ("#micronaut-yes".equals(action.getActionId())) {
                context.respond("Same do I!");
            } else if ("#micronaut-no".equals(action.getActionId())) {
                context.respond("I'm sorry to hear that!");
            } else {
                context.respond("I don't know what you mean!");
            }
        }
        return context.ack();
    }

    @Override
    public Pattern getActionIdPattern() {
        return Pattern.compile("#micronaut-.*");
    }

}
