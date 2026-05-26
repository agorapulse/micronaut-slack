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
package com.agorapulse.slack.http;

import com.agorapulse.micronaut.amazon.awssdk.s3.SimpleStorageService;
import com.agorapulse.slack.install.ObservableInstallationService;
import com.agorapulse.slack.install.S3InstallationService;
import com.agorapulse.slack.install.S3OAuthStateService;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.service.OAuthStateService;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@Property(name = "aws.s3.region", value = "us-east-1")
@Property(name = "aws.s3.bucket", value = S3Test.BUCKET_NAME)
@Property(name = "slack.bucket", value = S3Test.BUCKET_NAME)
public class S3Test {

    public static final String BUCKET_NAME = "slack-installations-test";

    @Inject ApplicationContext context;
    @Inject SimpleStorageService storageService;

    @BeforeEach
    void setUp() {
        if (!storageService.listBucketNames().contains(BUCKET_NAME)) {
            storageService.createBucket(BUCKET_NAME);
        }
    }

    @Test
    void s3OAuthStateServiceWiredAndOperational() {
        OAuthStateService stateService = context.getBean(OAuthStateService.class);
        assertTrue(stateService instanceof S3OAuthStateService,
            "expected S3OAuthStateService, got " + stateService.getClass().getName());

        // initializer should accept the bucket created above
        stateService.initializer().accept(null);

        // round-trip a state value
        String state = "test-state-" + System.nanoTime();
        assertFalse(stateService.isAvailableInDatabase(state));
        try {
            stateService.addNewStateToDatastore(state);
            assertTrue(stateService.isAvailableInDatabase(state));
            stateService.deleteStateFromDatastore(state);
            assertFalse(stateService.isAvailableInDatabase(state));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void s3InstallationServiceWiredAndOperational() {
        InstallationService installationService = context.getBean(InstallationService.class);
        assertTrue(installationService instanceof ObservableInstallationService,
            "expected ObservableInstallationService, got " + installationService.getClass().getName());
        assertTrue(S3InstallationService.class.equals(
            ((ObservableInstallationService) installationService).getDelegateType()),
            "expected delegate to be S3InstallationService, got "
                + ((ObservableInstallationService) installationService).getDelegateType().getName());

        // initializer should accept the bucket created above
        installationService.initializer().accept(null);

        // a missing key returns null (no exception)
        assertNotNull(installationService);
    }
}
