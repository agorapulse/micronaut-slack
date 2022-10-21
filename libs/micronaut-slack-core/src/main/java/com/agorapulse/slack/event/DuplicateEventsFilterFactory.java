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

import io.micronaut.cache.SyncCache;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

@Factory
public class DuplicateEventsFilterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateEventsFilterFactory.class);

    @Bean
    @Singleton
    @Requires(classes = SyncCache.class)
    DuplicateEventsFilter cacheDuplicateEventsFilter(
        @Nullable @Named("slack-events") SyncCache<?> eventsCache
    ) {
        if (eventsCache != null) {
            LOGGER.info("Micronaut Cache is available but cache for the Slack events is not configured");
            return new CacheDuplicateEventsFilter(eventsCache);
        }

        return new DefaultDuplicateEventsFilter();
    }

    @Bean
    @Singleton
    @Secondary
    DuplicateEventsFilter duplicateEventsFilter() {
        return new DefaultDuplicateEventsFilter();
    }

}
