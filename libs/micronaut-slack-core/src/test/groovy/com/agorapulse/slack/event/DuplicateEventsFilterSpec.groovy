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

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class DuplicateEventsFilterSpec extends Specification {

    private static final String EVENT_ID = 'event-id'

    @Inject DuplicateEventsFilter eventsFilter

    @SuppressWarnings('Instanceof')
    void 'basic guard'() {
        expect:
            eventsFilter instanceof DefaultDuplicateEventsFilter
        and:
            !eventsFilter.isRunning(EVENT_ID)

        when:
            eventsFilter.start(EVENT_ID)
        then:
            eventsFilter.isRunning(EVENT_ID)

        when:
            eventsFilter.finish(EVENT_ID)
        then:
            !eventsFilter.isRunning(EVENT_ID)
    }

}
