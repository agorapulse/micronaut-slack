/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2023 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.slack.example.commander;

import com.agorapulse.slack.handlers.MicronautSlashCommandHandler;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.micronaut.core.util.StringUtils;

import jakarta.inject.Singleton;

@Singleton
public class CommandHandler implements MicronautSlashCommandHandler {                   // <1>

    @Override
    public String getCommandId() {
        return "/commander";                                                            // <2>
    }

    @Override
    public Response apply(
        SlashCommandRequest slashCommandRequest,
        SlashCommandContext context
    ) {
        String salutation = StringUtils.capitalize(slashCommandRequest.getPayload().getText());
        return context.ack(salutation + " to you, sailor!");                            // <3>
    }


}
