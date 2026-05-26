/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2026 Agorapulse.
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
package com.agorapulse.slack.install.enumerate;

import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.util.JsonOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

public class S3InstallationEnumerationService implements InstallationEnumerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3InstallationEnumerationService.class);

    private final S3Client s3;
    private final String bucketName;
    private boolean historicalDataEnabled;

    public S3InstallationEnumerationService(S3Client s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public Stream<Bot> findAllBots() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix("bot/")
            .build();

        return s3.listObjectsV2Paginator(request).stream()
            .flatMap(response -> response.contents().stream())
            .filter(object -> !historicalDataEnabled || object.key().endsWith("-latest"))
            .map(object -> {
                try {
                    return toBot(object.key());
                } catch (Exception e) {
                    LOGGER.error("Failed to load Bot data for key {}", object.key());
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    private Bot toBot(String fullKey) {
        ResponseBytes<GetObjectResponse> responseBytes;
        try {
            responseBytes = s3.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fullKey)
                .build());
        } catch (S3Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Amazon S3 object not found (key: {}, S3Exception: {})", fullKey, e, e);
            } else {
                LOGGER.info("Amazon S3 object not found (key: {}, S3Exception: {})", fullKey, e.toString());
            }
            return null;
        }
        String json = responseBytes.asString(StandardCharsets.UTF_8);
        return JsonOps.fromJson(json, DefaultBot.class);
    }

}
