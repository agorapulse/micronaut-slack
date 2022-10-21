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
package com.agorapulse.slack.event;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RunOnceBoltEventHandler<E extends Event> implements BoltEventHandler<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunOnceBoltEventHandler.class);

    private final DuplicateEventsFilter retryService;
    private final BoltEventHandler<E> delegate;

    public RunOnceBoltEventHandler(DuplicateEventsFilter retryService, BoltEventHandler<E> delegate) {
        this.retryService = retryService;
        this.delegate = delegate;
    }

    @Override
    public Response apply(EventsApiPayload<E> event, EventContext context) throws IOException, SlackApiException {
        if (retryService.isRunning(event.getEventId())) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(
                    "Event {} of type {} has been retried {} time(s) for reason {}. Consider handling the event asynchronously",
                    event.getEventId(),
                    event.getEvent().getType(),
                    context.getRetryNum(),
                    context.getRetryReason()
                );
            }
            return context.ack();
        }

        try {
            retryService.start(event.getEventId());
            return delegate.apply(event, context);
        } finally {
            retryService.finish(event.getEventId());
        }
    }

}
