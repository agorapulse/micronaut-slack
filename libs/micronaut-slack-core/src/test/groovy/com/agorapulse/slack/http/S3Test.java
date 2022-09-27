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

import com.agorapulse.micronaut.aws.s3.SimpleStorageService;
import com.agorapulse.slack.SlackConfiguration;
import com.slack.api.bolt.App;
import com.slack.api.bolt.service.OAuthStateService;
import com.slack.api.bolt.service.builtin.AmazonS3OAuthStateService;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
@Property(name = "slack.bucket", value = S3Test.BUCKET_NAME)
@Property(name = "slack.bucket", value = S3Test.BUCKET_NAME)
public class S3Test {

    public static final String BUCKET_NAME = "slack-installations.test.agorapulse.com";


    @Inject ApplicationContext context;
    @Inject App app;
    @Inject SlackConfiguration slackConfiguration;
    @Inject SimpleStorageService simpleStorageService;

    @BeforeEach
    void setup() {
        if (!simpleStorageService.listBucketNames().contains(BUCKET_NAME)) {
            simpleStorageService.createBucket(BUCKET_NAME);
        }
    }

    @Test
    void testS3ServicesInitialized() {
        Assertions.assertTrue(context.getBean(OAuthStateService.class) instanceof AmazonS3OAuthStateService);
    }

}
