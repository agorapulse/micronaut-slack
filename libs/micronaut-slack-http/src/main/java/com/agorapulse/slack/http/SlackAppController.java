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
package com.agorapulse.slack.http;

import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

/**
 * The default Web controller that works in Micronaut apps.
 * This component requires singleton {@link App} instance managed by the Micronaut DI container.
 *
 * @see <a href="https://guides.micronaut.io/creating-your-first-micronaut-app/guide/index.html">The official tutorial</a>
 * @see <a href="https://docs.micronaut.io/latest/api/io/micronaut/http/annotation/Controller.html">@Controller annotation</a>
 *
 * This is a copy of <code>com.slack.api.bolt.micronaut.SlackAppController</code> that guarantees compatibility with
 * older versions of Micronaut.
 */
@Controller("/slack")
public class SlackAppController {

    private final App slackApp;
    private final SlackAppMicronautAdapter adapter;

    public SlackAppController(App slackApp, SlackAppMicronautAdapter adapter) {
        this.slackApp = slackApp;
        this.adapter = adapter;
    }

    /**
     * Handles all Slack events sent to the /events endpoint.
     * <p>
     * This endpoint receives events from Slack including commands, actions, and
     * webhook events. It supports both application/json and form-urlencoded requests.
     *
     * @param request the HTTP request containing a Slack API payload
     * @return HTTP response to send back to Slack
     * @throws Exception if there's an error processing the request
     */
    @Post(value = "/events", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public HttpResponse<String> events(HttpRequest<String> request) throws Exception {
        return adapt(request, request.getBody().orElse(null));
    }

    /**
     * Handles OAuth installation flow initiation.
     * <p>
     * This endpoint starts the OAuth flow that allows users to install
     * this Slack app to their workspace.
     *
     * @param request the HTTP request
     * @return HTTP response with the OAuth authorization page or 404 if OAuth install is disabled
     * @throws Exception if there's an error processing the request
     */
    @Get("/install")
    public HttpResponse<String> install(HttpRequest<String> request) throws Exception {
        if (!slackApp.config().isOAuthInstallPathEnabled()) {
            return HttpResponse.notFound();
        }
        return adapt(request, null);
    }

    /**
     * Handles OAuth redirect after app installation approval.
     * <p>
     * This endpoint receives the OAuth callback from Slack after a user has
     * approved the installation of the app in their workspace.
     *
     * @param request the HTTP request containing OAuth verification code
     * @return HTTP response completing the OAuth flow or 404 if OAuth redirect is disabled
     * @throws Exception if there's an error processing the request
     */
    @Get("/oauth_redirect")
    public HttpResponse<String> oauthRedirect(HttpRequest<String> request) throws Exception {
        if (!slackApp.config().isOAuthRedirectUriPathEnabled()) {
            return HttpResponse.notFound();
        }
        return adapt(request, null);
    }

    /**
     * Converts Micronaut HTTP requests to Slack Bolt requests and processes them.
     *
     * @param request the Micronaut HTTP request
     * @param body the request body content, or null for GET requests
     * @return a Micronaut HTTP response adapted from the Slack Bolt response
     * @throws Exception if there's an error during request processing
     */
    private HttpResponse<String> adapt(HttpRequest<String> request, String body) throws Exception {
        Request<?> slackRequest = adapter.toSlackRequest(request, body);
        return adapter.toMicronautResponse(slackApp.run(slackRequest));
    }

}
