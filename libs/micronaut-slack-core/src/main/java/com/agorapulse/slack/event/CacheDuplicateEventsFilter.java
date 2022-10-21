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

/**
 * Cache-based implementation of the {@link DuplicateEventsFilter}.
 */
public class CacheDuplicateEventsFilter implements DuplicateEventsFilter {

    private final SyncCache<?> eventsCache;

    public CacheDuplicateEventsFilter(SyncCache<?> eventsCache) {
        this.eventsCache = eventsCache;
    }

    @Override
    public boolean isRunning(String eventId) {
        return eventsCache.get(eventId, String.class).isPresent();
    }

    @Override
    public void start(String eventId) {
        eventsCache.put(eventId, eventId);
    }

    @Override
    public void finish(String eventId) {
        eventsCache.invalidate(eventId);
    }

}
