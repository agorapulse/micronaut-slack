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
package com.agorapulse.slack.install;

import com.slack.api.bolt.Initializer;
import com.slack.api.bolt.service.OAuthStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

/**
 * AWS SDK v2 equivalent of Bolt's {@code AmazonS3OAuthStateService}.
 * Persists OAuth state values as objects keyed {@code state/<state>}; the body
 * is the millisecond timestamp at which the state expires.
 */
public class S3OAuthStateService implements OAuthStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3OAuthStateService.class);

    private final S3Client s3;
    private final String bucketName;

    public S3OAuthStateService(S3Client s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public Initializer initializer() {
        return app -> {
            try {
                s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            } catch (NoSuchBucketException e) {
                throw new IllegalStateException("Failed to access the Amazon S3 bucket (name: " + bucketName + ")", e);
            }
        };
    }

    @Override
    public void addNewStateToDatastore(String state) {
        String value = String.valueOf(System.currentTimeMillis() + getExpirationInSeconds() * 1000);
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(getKey(state)).build(),
            RequestBody.fromString(value, StandardCharsets.UTF_8)
        );
    }

    @Override
    public boolean isAvailableInDatabase(String state) {
        ResponseBytes<GetObjectResponse> response = getObject(getKey(state));
        if (response == null) {
            return false;
        }
        String millisToExpire = response.asString(StandardCharsets.UTF_8);
        try {
            return Long.parseLong(millisToExpire) > System.currentTimeMillis();
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid state value detected - state: {}, millisToExpire: {}", state, millisToExpire);
            return false;
        }
    }

    @Override
    public void deleteStateFromDatastore(String state) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(getKey(state)).build());
    }

    private String getKey(String state) {
        return "state/" + state;
    }

    private ResponseBytes<GetObjectResponse> getObject(String fullKey) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(fullKey).build());
        } catch (S3Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Amazon S3 object not found (key: {}, S3Exception: {})", fullKey, e.toString());
            }
            return null;
        }
    }

}
