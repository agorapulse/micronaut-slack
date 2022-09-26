/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
public class MessageActionHandler implements MicronautBlockActionHandler {              // <1>

    @Override
    public Pattern getActionIdPattern() {
        return Pattern.compile("#mn-.*");                                               // <2>
    }

    @Override
    public Response apply(
        BlockActionRequest blockActionRequest,
        ActionContext context
    ) throws IOException {
        Optional<BlockActionPayload.Action> firstAction = blockActionRequest            // <3>
            .getPayload()
            .getActions()
            .stream()
            .findFirst();

        if (firstAction.isPresent()) {
            BlockActionPayload.Action action = firstAction.get();
            if ("#mn-yes".equals(action.getActionId())) {                               // <4>
                context.respond("Same do I!");                                          // <5>
            } else if ("#mn-no".equals(action.getActionId())) {
                context.respond("I'm sorry to hear that!");
            } else {
                context.respond("I don't know what you mean!");
            }
        }
        return context.ack();
    }

}
