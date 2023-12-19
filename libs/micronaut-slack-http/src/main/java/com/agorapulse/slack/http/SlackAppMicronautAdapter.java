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

import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.SlackRequestParser;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;

import jakarta.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The default adaptor that translates Micronaut specific interfaces into Bolt's ones.
 * This component requires singleton {@link AppConfig} instance managed by the Micronaut DI container.
 *
 * This is a copy of <code>com.slack.api.bolt.micronaut.SlackAppMicronautAdapter</code> that guarantees compatibility with
 * older versions of Micronaut.
 */
@Singleton
public class SlackAppMicronautAdapter {

    private SlackRequestParser requestParser;

    public SlackAppMicronautAdapter(AppConfig appConfig) {
        this.requestParser = new SlackRequestParser(appConfig);
    }

    public Request<?> toSlackRequest(HttpRequest<?> req, String requestBody) {
        RequestHeaders headers = new RequestHeaders(
                req.getHeaders() != null ? req.getHeaders().asMap() : Collections.emptyMap());

        InetSocketAddress isa = req.getRemoteAddress();
        String remoteAddress = null;
        if (isa != null && isa.getAddress() != null) {
            remoteAddress = toString(isa.getAddress().getAddress());
        }
        SlackRequestParser.HttpRequest rawRequest = SlackRequestParser.HttpRequest.builder()
                .requestUri(req.getPath())
                .queryString(req.getParameters() != null ? req.getParameters().asMap() : Collections.emptyMap())
                .headers(headers)
                .requestBody(requestBody)
                .remoteAddress(remoteAddress)
                .build();
        return requestParser.parse(rawRequest);
    }

    public HttpResponse<String> toMicronautResponse(Response resp) {
        HttpStatus status = HttpStatus.valueOf(resp.getStatusCode());
        MutableHttpResponse<String> response = HttpResponse.status(status);
        for (Map.Entry<String, List<String>> header : resp.getHeaders().entrySet()) {
            String name = header.getKey();
            for (String value : header.getValue()) {
                response.header(name, value);
            }
        }
        response.body(resp.getBody());
        response.contentType(resp.getContentType());
        if (resp.getBody() != null) {
            response.contentLength(resp.getBody().length());
        } else {
            response.contentLength(0);
        }
        return response;
    }

    private static String toString(byte[] rawBytes) {
        int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }

}
