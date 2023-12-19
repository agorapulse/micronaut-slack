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
package com.agorapulse.slack.install.enumerate;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.util.JsonOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

public class AmazonS3InstallationEnumerationService implements InstallationEnumerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3InstallationEnumerationService.class);

    private final AmazonS3 s3;
    private final String bucketName;
    private boolean historicalDataEnabled;

    public AmazonS3InstallationEnumerationService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public Stream<Bot> findAllBots() {
        return new AmazonS3ObjectListingIterator(s3, bucketName, "bot/").toStream()
                .flatMap(objectListing -> objectListing.getObjectSummaries().stream())
                .filter(summary -> !historicalDataEnabled || summary.getKey().endsWith("-latest"))
                .map(summary -> {
                    try {
                        return toBot(getObject(s3, summary.getKey()));
                    } catch (IOException e) {
                        LOGGER.error("Failed to load Bot data for key {}", summary.getKey());
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    private S3Object getObject(AmazonS3 s3, String fullKey) {
        try {
            return s3.getObject(bucketName, fullKey);
        } catch (AmazonS3Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Amazon S3 object metadata not found (key: {}, AmazonS3Exception: {})", fullKey, e, e);
            } else {
                LOGGER.info("Amazon S3 object metadata not found (key: {}, AmazonS3Exception: {})", fullKey, e.toString());
            }
            return null;
        }
    }

    private Bot toBot(S3Object s3Object) throws IOException {
        if (s3Object == null) {
            return null;
        }
        String json = IOUtils.toString(s3Object.getObjectContent());
        return JsonOps.fromJson(json, DefaultBot.class);
    }

}
