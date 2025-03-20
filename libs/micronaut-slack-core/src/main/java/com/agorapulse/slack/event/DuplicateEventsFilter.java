/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2025 Agorapulse.
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

/**
 * Service responsible for tracking and managing duplicate event executions.
 * <p>
 * This interface helps prevent duplicate processing of the same Slack event,
 * which can occur due to Slack's retry mechanisms or other network issues.
 */
public interface DuplicateEventsFilter {

    /**
     * Checks if processing for the given event ID is currently in progress.
     *
     * @param eventId the unique Slack event identifier
     * @return true if the event is currently being processed, false otherwise
     */
    boolean isRunning(String eventId);

    /**
     * Marks the beginning of processing for the given event ID.
     *
     * @param eventId the unique Slack event identifier
     */
    void start(String eventId);

    /**
     * Marks the completion of processing for the given event ID.
     *
     * @param eventId the unique Slack event identifier
     */
    void finish(String eventId);

}
