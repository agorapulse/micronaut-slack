package com.agorapulse.slack.example.commander;

import com.agorapulse.slack.handlers.MicronautSlashCommandHandler;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.micronaut.core.util.StringUtils;

import javax.inject.Singleton;

@Singleton
public class CommandHandler implements MicronautSlashCommandHandler {

    @Override
    public Response apply(SlashCommandRequest slashCommandRequest, SlashCommandContext context) {
        return context.ack(StringUtils.capitalize(slashCommandRequest.getPayload().getText()) + " to you, sailor!");
    }

    @Override
    public String getCommandId() {
        return "/commander";
    }

}
