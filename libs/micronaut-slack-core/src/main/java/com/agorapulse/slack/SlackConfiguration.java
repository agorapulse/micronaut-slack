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
package com.agorapulse.slack;

import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.service.builtin.oauth.view.OAuthInstallPageRenderer;
import com.slack.api.bolt.service.builtin.oauth.view.OAuthRedirectUriPageRenderer;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.StringUtils;

@ConfigurationProperties("slack")
public class SlackConfiguration extends AppConfig {

    private String bucket;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    // more idiomatic setters e.g. oauth-foo instead of o-auth-foo and bot-token shortcut

    public void setOauthRedirectUriPageRenderer(OAuthRedirectUriPageRenderer oAuthRedirectUriPageRenderer) {
        super.setOAuthRedirectUriPageRenderer(oAuthRedirectUriPageRenderer);
    }

    public void setOauthInstallPageRenderer(OAuthInstallPageRenderer oAuthInstallPageRenderer) {
        super.setOAuthInstallPageRenderer(oAuthInstallPageRenderer);
    }

    public void setOauthInstallPathEnabled(boolean oAuthInstallPathEnabled) {
        super.setOAuthInstallPathEnabled(oAuthInstallPathEnabled);
    }

    public void setOauthRedirectUriPathEnabled(boolean oAuthRedirectUriPathEnabled) {
        super.setOAuthRedirectUriPathEnabled(oAuthRedirectUriPathEnabled);
    }

    public void setOauthInstallPageRenderingEnabled(boolean oAuthInstallPageRenderingEnabled) {
        super.setOAuthInstallPageRenderingEnabled(oAuthInstallPageRenderingEnabled);
    }

    @Deprecated
    public void setOauthCallbackEnabled(boolean enabled) {
        super.setOAuthCallbackEnabled(enabled);
    }

    @Deprecated
    public void setOauthStartEnabled(boolean enabled) {
        super.setOAuthStartEnabled(enabled);
    }

    public void setBotToken(String singleTeamBotToken) {
        if (StringUtils.isNotEmpty(singleTeamBotToken)) {
            super.setSingleTeamBotToken(singleTeamBotToken);
        }
    }
}
