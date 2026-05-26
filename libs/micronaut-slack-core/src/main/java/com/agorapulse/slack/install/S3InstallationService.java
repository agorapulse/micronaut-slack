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
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.model.builtin.DefaultInstaller;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.util.JsonOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * AWS SDK v2 equivalent of Bolt's {@code AmazonS3InstallationService}.
 * Key layout and JSON shape match the v1 implementation so existing buckets remain readable.
 */
public class S3InstallationService implements InstallationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3InstallationService.class);

    private final S3Client s3;
    private final String bucketName;
    private boolean historicalDataEnabled;

    public S3InstallationService(S3Client s3, String bucketName) {
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
    public boolean isHistoricalDataEnabled() {
        return historicalDataEnabled;
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {
        this.historicalDataEnabled = isHistoricalDataEnabled;
    }

    @Override
    public void saveInstallerAndBot(Installer i) throws Exception {
        if (isHistoricalDataEnabled()) {
            save(getInstallerKey(i) + "-latest", JsonOps.toJsonString(i));
            save(getBotKey(i) + "-latest", JsonOps.toJsonString(i.toBot()));
            save(getInstallerKey(i) + "-" + i.getInstalledAt(), JsonOps.toJsonString(i));
            save(getBotKey(i) + "-" + i.getInstalledAt(), JsonOps.toJsonString(i.toBot()));
        } else {
            save(getInstallerKey(i), JsonOps.toJsonString(i));
            save(getBotKey(i), JsonOps.toJsonString(i.toBot()));
        }
    }

    @Override
    public void saveBot(Bot bot) throws Exception {
        String keyPrefix = getBotKey(bot.getEnterpriseId(), bot.getTeamId());
        if (isHistoricalDataEnabled()) {
            save(keyPrefix + "-latest", JsonOps.toJsonString(bot));
            save(keyPrefix + "-" + bot.getInstalledAt(), JsonOps.toJsonString(bot));
        } else {
            save(keyPrefix, JsonOps.toJsonString(bot));
        }
    }

    @Override
    public void deleteBot(Bot bot) {
        String key = getBotKey(bot.getEnterpriseId(), bot.getTeamId());
        if (isHistoricalDataEnabled()) {
            key = key + "-latest";
        }
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    @Override
    public void deleteInstaller(Installer installer) {
        String key = getInstallerKey(installer);
        if (isHistoricalDataEnabled()) {
            key = key + "-latest";
        }
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        if (enterpriseId != null) {
            // try finding org-level bot token first - teamId is intentionally null here
            String fullKey = getBotKey(enterpriseId, null);
            if (isHistoricalDataEnabled()) {
                fullKey = fullKey + "-latest";
            }
            if (getObjectMetadata(fullKey) != null) {
                try {
                    Bot bot = toBot(getObject(fullKey));
                    if (bot != null) {
                        return bot;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load org-level Bot installation for enterprise_id: {}", enterpriseId);
                }
            }
            // not found - going to find workspace level installation
        }
        String fullKey = getBotKey(enterpriseId, teamId);
        if (isHistoricalDataEnabled()) {
            fullKey = fullKey + "-latest";
        }
        if (getObjectMetadata(fullKey) == null && enterpriseId != null) {
            String nonGridKey = getBotKey(null, teamId);
            if (isHistoricalDataEnabled()) {
                nonGridKey = nonGridKey + "-latest";
            }
            ResponseBytes<GetObjectResponse> nonGridObject = getObject(nonGridKey);
            if (nonGridObject != null) {
                try {
                    Bot bot = toBot(nonGridObject);
                    if (bot != null) {
                        bot.setEnterpriseId(enterpriseId); // the workspace seems to be in a Grid org now
                        save(fullKey, JsonOps.toJsonString(bot));
                        return bot;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to save a new Bot data for enterprise_id: {}, team_id: {}", enterpriseId, teamId);
                }
            }
        }
        try {
            return toBot(getObject(fullKey));
        } catch (Exception e) {
            LOGGER.error("Failed to load Bot data for enterprise_id: {}, team_id: {}", enterpriseId, teamId);
            return null;
        }
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        if (enterpriseId != null) {
            // try finding org-level user token first - teamId is intentionally null here
            String fullKey = getInstallerKey(enterpriseId, null, userId);
            if (isHistoricalDataEnabled()) {
                fullKey = fullKey + "-latest";
            }
            if (getObjectMetadata(fullKey) != null) {
                try {
                    Installer installer = toInstaller(getObject(fullKey));
                    if (installer != null) {
                        return installer;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load org-level installation for enterprise_id: {}, user_id: {}", enterpriseId, userId);
                }
            }
            // not found - going to find workspace level installation
        }
        String fullKey = getInstallerKey(enterpriseId, teamId, userId);
        if (isHistoricalDataEnabled()) {
            fullKey = fullKey + "-latest";
        }
        if (getObjectMetadata(fullKey) == null && enterpriseId != null) {
            String nonGridKey = getInstallerKey(null, teamId, userId);
            if (isHistoricalDataEnabled()) {
                nonGridKey = nonGridKey + "-latest";
            }
            ResponseBytes<GetObjectResponse> nonGridObject = getObject(nonGridKey);
            if (nonGridObject != null) {
                try {
                    Installer installer = toInstaller(nonGridObject);
                    if (installer != null) {
                        installer.setEnterpriseId(enterpriseId); // the workspace seems to be in a Grid org now
                        saveInstallerAndBot(installer);
                        return installer;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to save a new Installer data for enterprise_id: {}, team_id: {}, user_id: {}",
                            enterpriseId, teamId, userId);
                }
            }
        }
        try {
            return toInstaller(getObject(fullKey));
        } catch (Exception e) {
            LOGGER.error("Failed to load Installer data for enterprise_id: {}, team_id: {}, user_id: {}",
                    enterpriseId, teamId, userId);
            return null;
        }
    }

    @Override
    public void deleteAll(String enterpriseId, String teamId) {
        String installerPrefix = "installer/"
                + Optional.ofNullable(enterpriseId).orElse("none")
                + "-"
                + Optional.ofNullable(teamId).orElse("none");
        String botPrefix = "bot/"
                + Optional.ofNullable(enterpriseId).orElse("none")
                + "-"
                + Optional.ofNullable(teamId).orElse("none");
        deleteAllObjectsMatchingPrefix(installerPrefix);
        deleteAllObjectsMatchingPrefix(botPrefix);
    }

    private void deleteAllObjectsMatchingPrefix(String prefix) {
        s3.listObjectsV2Paginator(ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build())
            .stream()
            .flatMap(response -> response.contents().stream())
            .map(S3Object::key)
            .forEach(key -> s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build()));
    }

    private void save(String s3Key, String json) {
        s3.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
            RequestBody.fromString(json, StandardCharsets.UTF_8)
        );
    }

    private HeadObjectResponse getObjectMetadata(String fullKey) {
        try {
            return s3.headObject(HeadObjectRequest.builder().bucket(bucketName).key(fullKey).build());
        } catch (NoSuchKeyException e) {
            return null;
        } catch (S3Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Amazon S3 object metadata not found (key: {}, S3Exception: {})", fullKey, e.toString());
            }
            return null;
        }
    }

    private ResponseBytes<GetObjectResponse> getObject(String fullKey) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(fullKey).build());
        } catch (NoSuchKeyException e) {
            return null;
        } catch (S3Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Amazon S3 object not found (key: {}, S3Exception: {})", fullKey, e.toString());
            }
            return null;
        }
    }

    private Bot toBot(ResponseBytes<GetObjectResponse> response) {
        if (response == null) {
            return null;
        }
        String json = response.asString(StandardCharsets.UTF_8);
        return JsonOps.fromJson(json, DefaultBot.class);
    }

    private Installer toInstaller(ResponseBytes<GetObjectResponse> response) {
        if (response == null) {
            return null;
        }
        String json = response.asString(StandardCharsets.UTF_8);
        return JsonOps.fromJson(json, DefaultInstaller.class);
    }

    private String getInstallerKey(Installer i) {
        return getInstallerKey(i.getEnterpriseId(), i.getTeamId(), i.getInstallerUserId());
    }

    private String getInstallerKey(String enterpriseId, String teamId, String userId) {
        return "installer/"
                + Optional.ofNullable(enterpriseId).orElse("none")
                + "-"
                + Optional.ofNullable(teamId).orElse("none")
                + "-"
                + userId;
    }

    private String getBotKey(Installer i) {
        return getBotKey(i.getEnterpriseId(), i.getTeamId());
    }

    private String getBotKey(String enterpriseId, String teamId) {
        return "bot/"
                + Optional.ofNullable(enterpriseId).orElse("none")
                + "-"
                + Optional.ofNullable(teamId).orElse("none");
    }
}
