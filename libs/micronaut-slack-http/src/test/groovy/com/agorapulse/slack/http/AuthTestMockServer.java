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
package com.agorapulse.slack.http;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.io.IOException;
import java.net.SocketException;

public class AuthTestMockServer {

    public static final String VALID_TOKEN = "xoxb-this-is-valid";

    static String ok = "{\n"
        + "  \"ok\": true,\n"
        + "  \"url\": \"https://java-slack-sdk-test.slack.com/\",\n"
        + "  \"team\": \"java-slack-sdk-test\",\n"
        + "  \"user\": \"test_user\",\n"
        + "  \"team_id\": \"T1234567\",\n"
        + "  \"user_id\": \"U1234567\",\n"
        + "  \"bot_id\": \"B12345678\",\n"
        + "  \"enterprise_id\": \"E12345678\"\n"
        + "}";
    static String ng = "{\n"
        + "  \"ok\": false,\n"
        + "  \"error\": \"invalid\"\n"
        + "}";

    static String oauthV2Access = "{\n"
        + "    \"ok\": true,\n"
        + "    \"access_token\": \"" + VALID_TOKEN + "\",\n"
        + "    \"token_type\": \"bot\",\n"
        + "    \"scope\": \"commands,incoming-webhook\",\n"
        + "    \"bot_user_id\": \"U0KRQLJ9H\",\n"
        + "    \"app_id\": \"A0KRD7HC3\",\n"
        + "    \"team\": {\n"
        + "        \"name\": \"Slack Softball Team\",\n"
        + "        \"id\": \"T9TK3CUKW\"\n"
        + "    },\n"
        + "    \"enterprise\": {\n"
        + "        \"name\": \"slack-sports\",\n"
        + "        \"id\": \"E12345678\"\n"
        + "    },\n"
        + "    \"authed_user\": {\n"
        + "        \"id\": \"U1234\",\n"
        + "        \"scope\": \"chat:write\",\n"
        + "        \"access_token\": \"xoxp-1234\",\n"
        + "        \"token_type\": \"user\"\n"
        + "    }\n"
        + "}";

    @WebServlet
    public static class AuthTestMockEndpoint extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setStatus(200);
            resp.setContentType("application/json");
            if (req.getRequestURI().equals("/api/oauth.v2.access")) {
                resp.getWriter().write(oauthV2Access);
                return;
            }
            if (req.getHeader("Authorization") == null || !req.getHeader("Authorization").equals("Bearer " + VALID_TOKEN)) {
                resp.getWriter().write(ng);
            } else {
                resp.getWriter().write(ok);
            }
        }
    }

    private int port;
    private Server server;

    public AuthTestMockServer() {
        this(PortProvider.getPort(AuthTestMockServer.class.getName()));
    }

    public AuthTestMockServer(int port) {
        setup(port);
    }

    private void setup(int port) {
        this.port = port;
        this.server = new Server(this.port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(AuthTestMockEndpoint.class, "/*");
    }

    public String getMethodsEndpointPrefix() {
        return "http://127.0.0.1:" + port + "/api/";
    }

    public void start() throws Exception {
        int retryCount = 0;
        while (retryCount < 5) {
            try {
                server.start();
                return;
            } catch (SocketException e) {
                // java.net.SocketException: Permission denied may arise
                // only on the GitHub Actions environment.
                setup(PortProvider.getPort(AuthTestMockServer.class.getName()));
                retryCount++;
            }
        }
    }

    public void stop() throws Exception {
        server.stop();
    }
}
