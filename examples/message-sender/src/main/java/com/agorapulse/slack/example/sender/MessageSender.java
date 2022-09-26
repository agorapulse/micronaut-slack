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

    private final MethodsClient methods;

    public MessageSender(MethodsClient methods) {                                       // <1>
        this.methods = methods;
    }

    public void sendMessage(String message) throws SlackApiException, IOException {
        ConversationsListResponse conversations = methods.conversationsList(b -> b);
        Optional<Conversation> generalChannel = conversations                           // <2>
            .getChannels()
            .stream()
            .filter(Conversation::isGeneral)
            .findAny();
        if (generalChannel.isPresent()) {
            methods.chatPostMessage(m -> m.
                text(message).channel(generalChannel.get().getId())                     // <3>
            );
        }
    }

}
