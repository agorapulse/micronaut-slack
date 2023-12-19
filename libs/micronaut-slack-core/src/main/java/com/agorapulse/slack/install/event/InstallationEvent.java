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
package com.agorapulse.slack.install.event;

public abstract class InstallationEvent {

    private final String enterpriseId;
    private final String teamId;

    private final String userId;

    protected InstallationEvent(String enterpriseId, String teamId, String userId) {
        this.enterpriseId = enterpriseId;
        this.teamId = teamId;
        this.userId = userId;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }
}
