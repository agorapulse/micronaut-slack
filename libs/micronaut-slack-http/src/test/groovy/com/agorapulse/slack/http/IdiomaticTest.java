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

import com.agorapulse.gru.Content;
import com.agorapulse.gru.Gru;
import com.agorapulse.gru.RequestDefinitionBuilder;
import com.agorapulse.gru.micronaut.Micronaut;
import com.slack.api.app_backend.SlackSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.slack.api.app_backend.SlackSignature.HeaderNames.X_SLACK_REQUEST_TIMESTAMP;
import static com.slack.api.app_backend.SlackSignature.HeaderNames.X_SLACK_SIGNATURE;

public class IdiomaticTest {

    public static final String TEST_SIGNING_SECRET = "s3cr3t";

    private static final SlackSignature.Generator SIGNATURE_GENERATOR = new SlackSignature.Generator(TEST_SIGNING_SECRET);

    private final Gru gru = Gru.create(
            Micronaut.build(this)
                    .doWithContextBuilder(builder -> builder
                            .environments("idiomatic")
                            .properties(Collections.singletonMap("slack.signing-secret", IdiomaticTest.TEST_SIGNING_SECRET))
                    )
                    .start()
    );

    @AfterEach
    public void close() {
        gru.close();
    }

    @Test
    public void testHelloCommand() throws Throwable {
        gru.verify(test -> test
                .post("/slack/events", req -> slackEventRequest(req, "command=/hello"))
                .expect(resp -> resp.json("commandHelloResponse.json"))
        );
    }

    private static void slackEventRequest(RequestDefinitionBuilder builder, String requestBody) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = SIGNATURE_GENERATOR.generate(timestamp, requestBody);

        builder.header(X_SLACK_REQUEST_TIMESTAMP, timestamp)
                .header(X_SLACK_SIGNATURE, signature)
                .header("Content-Type", "application/json")
                .param("query", "queryValue")
                .json(Content.inline(requestBody));
    }

}
