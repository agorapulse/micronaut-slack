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
package com.agorapulse.slack.event

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.app_backend.events.payload.ReactionAddedPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.response.Response
import com.slack.api.methods.SlackApiException
import com.slack.api.model.event.ReactionAddedEvent
import groovy.transform.CompileStatic
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class RunOnceBoltEventHandlerSpec extends Specification {

    ExecutorService executorService = Executors.newFixedThreadPool(3)
    BlockingVariable<String> input = new BlockingVariable<>()
    BlockingVariable<Integer> output = new BlockingVariable<>()
    BlockingReactionAddedHandler blockingReactionAddedHandler = new BlockingReactionAddedHandler(input, output)
    RunOnceBoltEventHandler<ReactionAddedEvent> handler = new RunOnceBoltEventHandler<>(
        new DefaultDuplicateEventsFilter(),
        blockingReactionAddedHandler
    )

    void 'handler only executed once'() {
        given:
            ReactionAddedEvent event = new ReactionAddedEvent()
            EventsApiPayload<ReactionAddedEvent> payload = new ReactionAddedPayload(
                eventId: 'event-id',
                event: event
            )

        when:
            executorService.submit {
                handler.apply(payload, new EventContext('C123456', null, null))
            }

            executorService.submit {
                handler.apply(payload, new EventContext('C123456', 1, 'retry'))
            }

            executorService.submit {
                handler.apply(payload, new EventContext('C123456', 2, 'retry'))
            }

            input.set('ack')

        then:
            output.get() == 1
    }

}

@CompileStatic
class BlockingReactionAddedHandler implements BoltEventHandler<ReactionAddedEvent> {

    private final AtomicInteger calls = new AtomicInteger()
    private final BlockingVariable<String> input
    private final BlockingVariable<Integer> output

    BlockingReactionAddedHandler(BlockingVariable<String> variable, BlockingVariable<Integer> output) {
        this.input = variable
        this.output = output
    }

    @Override
    Response apply(
        EventsApiPayload<ReactionAddedEvent> event,
        EventContext context
    ) throws IOException, SlackApiException {
        input.get()
        output.set(calls.incrementAndGet())
        return context.ack()
    }

}
