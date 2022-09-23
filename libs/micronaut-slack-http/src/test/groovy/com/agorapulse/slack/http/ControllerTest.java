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
package com.agorapulse.slack.http;

import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import io.micronaut.core.convert.DefaultConversionService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.SimpleHttpParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerTest {

    private AuthTestMockServer slackApiServer;

    @BeforeEach
    public void setupServer() throws Exception {
        slackApiServer = new AuthTestMockServer();
        slackApiServer.start();
    }

    @AfterEach
    public void stopServer() throws Exception {
        if (slackApiServer != null) {
            slackApiServer.stop();
        }
    }

    @Test
    public void test() throws Exception {
        SlackConfig slackConfig = new SlackConfig();
        slackConfig.setMethodsEndpointUrlPrefix(slackApiServer.getMethodsEndpointPrefix());
        Slack slack = Slack.getInstance(slackConfig);
        AppConfig config = AppConfig.builder().slack(slack)
                .singleTeamBotToken(AuthTestMockServer.VALID_TOKEN)
                .signingSecret("secret")
                .build();

        App app = new App(config);
        SlackAppController controller = new SlackAppController(app, new SlackAppMicronautAdapter(config));

        assertNotNull(controller);

        HttpRequest<String> req = mock(HttpRequest.class);
        SimpleHttpHeaders headers = new SimpleHttpHeaders(new HashMap<>(), new DefaultConversionService());
        when(req.getHeaders()).thenReturn(headers);
        SimpleHttpParameters parameters = new SimpleHttpParameters(new HashMap<>(), new DefaultConversionService());
        when(req.getParameters()).thenReturn(parameters);

        HttpResponse<String> response = controller.events(req, "token=random&ssl_check=1");
        assertEquals(200, response.getStatus().getCode());
    }

    @Test
    public void oauthNotAvailableForStandardApp() throws Exception {
        SlackConfig slackConfig = new SlackConfig();
        slackConfig.setMethodsEndpointUrlPrefix(slackApiServer.getMethodsEndpointPrefix());
        Slack slack = Slack.getInstance(slackConfig);
        AppConfig config = AppConfig.builder().slack(slack)
                .singleTeamBotToken(AuthTestMockServer.VALID_TOKEN)
                .signingSecret("secret")
                .build();

        App app = new App(config);
        SlackAppController controller = new SlackAppController(app, new SlackAppMicronautAdapter(config));

        assertNotNull(controller);

        HttpRequest<String> req = HttpRequest.GET("/slack/install");

        HttpResponse<String> notFound = controller.install(req);
        assertThat(notFound.getStatus().getCode(), equalTo(404));
    }

    @Test
    public void oauth() throws Exception {
        SlackConfig slackConfig = new SlackConfig();
        slackConfig.setMethodsEndpointUrlPrefix(slackApiServer.getMethodsEndpointPrefix());
        Slack slack = Slack.getInstance(slackConfig);

        AppConfig config = AppConfig.builder()
                .slack(slack)
                .signingSecret("secret")
                .clientId("111.222")
                .clientSecret("cs")
                .scope("commands,chat:write")
                .oauthInstallPath("/slack/install")
                .oauthRedirectUriPath("/slack/oauth_redirect")
                .oauthCompletionUrl("https://www.example.com/success")
                .oauthCancellationUrl("https://www.example.com/failure")
                .oAuthInstallPageRenderingEnabled(false)
                .build();

        App oauthSlackApp = new App(config).asOAuthApp(true);
        SlackAppController controller = new SlackAppController(oauthSlackApp, new SlackAppMicronautAdapter(config));

        assertNotNull(controller);

        HttpRequest<String> req = HttpRequest.GET("/slack/install");

        HttpResponse<String> installResponse = controller.install(req);

        assertThat(installResponse.getStatus().getCode(), equalTo(302));

        String location = installResponse.header("Location");
        assertNotNull(location);
        assertTrue(location.startsWith(
                "https://slack.com/oauth/v2/authorize?client_id=111.222&scope=commands%2Cchat%3Awrite&user_scope=&state="));

        assertThat(installResponse.getStatus().getCode(), equalTo(302));

        String state = extractStateValue(location);

        MutableHttpRequest<String> redirectRequest = HttpRequest.GET("/slack/oauth_redirect");
        MutableHttpParameters parameters = redirectRequest.getParameters();
        parameters
                .add("code", "111.111.111")
                .add("state", state);
        redirectRequest.cookie(Cookie.of("slack-app-oauth-state", state));

        HttpResponse<String> redirectResponse = controller.oauthRedirect(redirectRequest);
        assertThat(redirectResponse.header("Location"), equalTo("https://www.example.com/success"));
    }

    private static String extractStateValue(String location) {
        for (String element : location.split("&")) {
            if (element.trim().startsWith("state=")) {
                return element.trim().replaceFirst("state=", "");
            }
        }
        return null;
    }

}
