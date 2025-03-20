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
package com.agorapulse.slack;

import com.agorapulse.slack.event.DuplicateEventsFilter;
import com.agorapulse.slack.event.RunOnceBoltEventHandler;
import com.agorapulse.slack.install.ObservableInstallationService;
import com.agorapulse.slack.install.enumerate.FileInstallationEnumerationService;
import com.agorapulse.slack.install.enumerate.InstallationEnumerationService;
import com.agorapulse.slack.oauth.DistributedAppAsyncMethodsClientFactory;
import com.agorapulse.slack.oauth.DistributedAppMethodsClientFactory;
import com.agorapulse.slack.handlers.MicronautAttachmentActionHandler;
import com.agorapulse.slack.handlers.MicronautBlockActionHandler;
import com.agorapulse.slack.handlers.MicronautBlockSuggestionHandler;
import com.agorapulse.slack.handlers.MicronautBoltEventHandler;
import com.agorapulse.slack.handlers.MicronautDialogCancellationHandler;
import com.agorapulse.slack.handlers.MicronautDialogSubmissionHandler;
import com.agorapulse.slack.handlers.MicronautDialogSuggestionHandler;
import com.agorapulse.slack.handlers.MicronautGlobalShortcutHandler;
import com.agorapulse.slack.handlers.MicronautMessageShortcutHandler;
import com.agorapulse.slack.handlers.MicronautSlashCommandHandler;
import com.agorapulse.slack.handlers.MicronautViewClosedHandler;
import com.agorapulse.slack.handlers.MicronautViewSubmissionHandler;
import com.agorapulse.slack.handlers.MicronautWorkflowStepEditHandler;
import com.agorapulse.slack.handlers.MicronautWorkflowStepExecuteHandler;
import com.agorapulse.slack.handlers.MicronautWorkflowStepSaveHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.bolt.App;
import com.slack.api.bolt.Initializer;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.service.OAuthStateService;
import com.slack.api.bolt.service.builtin.AmazonS3InstallationService;
import com.slack.api.bolt.service.builtin.AmazonS3OAuthStateService;
import com.slack.api.bolt.service.builtin.FileInstallationService;
import com.slack.api.bolt.service.builtin.FileOAuthStateService;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.MethodsClient;
import com.slack.api.model.event.Event;
import com.slack.api.util.http.SlackHttpClient;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.util.StringUtils;

import jakarta.annotation.Nullable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Factory class that creates and configures all necessary Slack API beans for the Micronaut context.
 * <p>
 * This factory initializes and registers essential Slack API components such as:
 * <ul>
 *   <li>Slack API clients and HTTP clients</li>
 *   <li>Installation services for storing app installations</li>
 *   <li>OAuth state services for managing OAuth flows</li>
 *   <li>Methods clients for sending API requests to Slack</li>
 *   <li>The central Bolt App instance with properly configured handlers</li>
 * </ul>
 * <p>
 * The factory automatically detects and registers handler beans from the Micronaut context
 * and supports both single-team and multi-team (distributed) Slack applications.
 */
@Factory
public class SlackFactory {

    /**
     * Creates the default Slack API configuration.
     *
     * @return the default Slack API configuration
     */
    @Bean
    @Singleton
    public SlackConfig slackConfig() {
        return SlackConfig.DEFAULT;
    }

    /**
     * Creates the HTTP client used for Slack API requests.
     *
     * @param config the Slack API configuration
     * @return configured HTTP client for Slack API calls
     */
    @Bean
    @Singleton
    public SlackHttpClient slackHttpClient(SlackConfig config) {
        return SlackHttpClient.buildSlackHttpClient(config);
    }

    /**
     * Creates the main Slack API client instance.
     * <p>
     * This is the core client used for all Slack API interactions.
     *
     * @return the Slack API client instance
     */
    @Bean
    @Singleton
    public Slack slack() {
        return Slack.getInstance();
    }

    /**
     * Creates a MethodsClient for making synchronous API calls to Slack.
     * <p>
     * If a single team bot token is configured, the client will be pre-configured with that token.
     * Otherwise, a token-less client is returned that requires tokens to be provided with each call.
     *
     * @param slack the Slack API client
     * @param configuration the Slack configuration
     * @return a configured methods client
     */
    @Bean
    @Singleton
    public MethodsClient methodsClient(Slack slack, SlackConfiguration configuration) {
        if (StringUtils.isNotEmpty(configuration.getSingleTeamBotToken())) {
            return slack.methods(configuration.getSingleTeamBotToken());
        }
        return slack.methods();
    }

    /**
     * Creates an AsyncMethodsClient for making asynchronous API calls to Slack.
     * <p>
     * If a single team bot token is configured, the client will be pre-configured with that token.
     * Otherwise, a token-less client is returned that requires tokens to be provided with each call.
     *
     * @param slack the Slack API client
     * @param configuration the Slack configuration
     * @return a configured asynchronous methods client
     */
    @Bean
    @Singleton
    public AsyncMethodsClient asyncMethodsClientx(Slack slack, SlackConfiguration configuration) {
        if (StringUtils.isNotEmpty(configuration.getSingleTeamBotToken())) {
            return slack.methodsAsync(configuration.getSingleTeamBotToken());
        }
        return slack.methodsAsync();
    }

    @Bean
    @Singleton
    @Secondary
    public OAuthStateService oAuthStateService(SlackConfiguration configuration) {
        return new FileOAuthStateService(configuration);
    }

    @Bean
    @Singleton
    @Requires(classes = AmazonS3.class)
    public OAuthStateService s3OAuthStateService(
        SlackConfiguration configuration,
        @Nullable @Named("slack") AmazonS3 slackAmazonS3,
        @Nullable AmazonS3 amazonS3
    ) {
        return createServiceIfS3Configured(configuration, slackAmazonS3, amazonS3, this::oAuthStateService, (bucket, s3) -> new AmazonS3OAuthStateService(bucket) {

            @Override
            public Initializer initializer() {
                return app -> {
                    boolean bucketExists = createS3Client().doesBucketExistV2(bucket);
                    if (!bucketExists) {
                        throw new IllegalStateException("Failed to access the Amazon S3 bucket (name: " + bucket + ")");
                    }
                };
            }

            @Override
            protected AmazonS3 createS3Client() {
                return s3;
            }

        });
    }

    @Bean
    @Singleton
    @Secondary
    public InstallationService installationService(SlackConfiguration slackConfiguration) {
        return new FileInstallationService(slackConfiguration);
    }

    @Bean
    @Singleton
    @Requires(classes = AmazonS3.class)
    public InstallationService s3InstallationService(
        SlackConfiguration configuration,
        @Nullable @Named("slack") AmazonS3 slackAmazonS3,
        @Nullable AmazonS3 amazonS3
    ) {
        return createServiceIfS3Configured(configuration, slackAmazonS3, amazonS3, this::installationService, (bucket, s3) -> new AmazonS3InstallationService(bucket) {
            @Override
            public Initializer initializer() {
                return app -> {
                    boolean bucketExists = createS3Client().doesBucketExistV2(bucket);
                    if (!bucketExists) {
                        throw new IllegalStateException("Failed to access the Amazon S3 bucket (name: " + bucket + ")");
                    }
                };
            }

            @Override
            protected AmazonS3 createS3Client() {
                return s3;
            }

        });
    }

    @Bean
    @Singleton
    public InstallationEnumerationService installationEnumerationService(InstallationService installationService, SlackConfiguration slackConfiguration) {
        if (installationService instanceof FileInstallationService || installationService instanceof ObservableInstallationService && ((ObservableInstallationService) installationService).getDelegateType().equals(FileInstallationService.class)) {
            return new FileInstallationEnumerationService(slackConfiguration, FileInstallationEnumerationService.DEFAULT_ROOT_DIR, false);
        }

        return null;
    }

    @Bean
    @Singleton
    public DistributedAppMethodsClientFactory distributedAppMethodsClientFactory(Slack slack, InstallationService service) {
        return new DistributedAppMethodsClientFactory() {
            @Override
            public Optional<MethodsClient> createClient(String enterpriseId, String methodsId) {
                Bot bot = service.findBot(enterpriseId, methodsId);
                if (bot != null) {
                    return Optional.of(slack.methods(bot.getBotAccessToken(), bot.getTeamId()));
                }

                return Optional.empty();
            }

            @Override
            public Optional<MethodsClient> createClient(Bot bot) {
                return Optional.of(slack.methods(bot.getBotAccessToken(), bot.getTeamId()));
            }
        };
    }

    @Bean
    @Singleton
    public DistributedAppAsyncMethodsClientFactory distributedAppAsyncMethodsClientFactory(Slack slack, InstallationService service) {
        return new DistributedAppAsyncMethodsClientFactory() {
            @Override
            public Optional<AsyncMethodsClient> createClient(String enterpriseId, String methodsId) {
                Bot bot = service.findBot(enterpriseId, methodsId);
                if (bot != null) {
                    return Optional.of(slack.methodsAsync(bot.getBotAccessToken(), bot.getTeamId()));
                }

                return Optional.empty();
            }

            @Override
            public Optional<AsyncMethodsClient> createClient(Bot bot) {
                return Optional.of(slack.methodsAsync(bot.getBotAccessToken(), bot.getTeamId()));
            }

        };
    }

    @Bean
    @Context
    public App app(
        SlackConfiguration configuration,
        Slack slack,
        InstallationService installationService,
        OAuthStateService oAuthStateService,
        DuplicateEventsFilter duplicateEventsFilter,
        List<MicronautAttachmentActionHandler> attachmentActionHandlers,
        List<MicronautBlockActionHandler> blockActionHandlers,
        List<MicronautBlockSuggestionHandler> blockSuggestionHandlers,
        List<MicronautDialogCancellationHandler> dialogCancellationHandlers,
        List<MicronautDialogSubmissionHandler> dialogSubmissionHandlers,
        List<MicronautDialogSuggestionHandler> dialogSuggestionHandlers,
        List<MicronautGlobalShortcutHandler> globalShortcutHandlers,
        List<MicronautMessageShortcutHandler> messageShortcutHandlers,
        List<MicronautSlashCommandHandler> slashCommandHandlers,
        List<MicronautViewClosedHandler> viewClosedHandlers,
        List<MicronautViewSubmissionHandler> viewSubmissionHandlers,
        List<MicronautWorkflowStepEditHandler> workflowStepEditHandlers,
        List<MicronautWorkflowStepExecuteHandler> workflowStepExecuteHandlers,
        List<MicronautWorkflowStepSaveHandler> workflowStepSaveHandlers,
        List<MicronautBoltEventHandler<Event>> boltEventHandlers
    ) {
        App app = createApp(configuration, slack);

        attachmentActionHandlers.forEach(h -> app.attachmentAction(h.getCallbackIdPattern(), h));
        blockActionHandlers.forEach(h -> app.blockAction(h.getActionIdPattern(), h));
        blockSuggestionHandlers.forEach(h -> app.blockSuggestion(h.getActionIdPattern(), h));
        dialogCancellationHandlers.forEach(h -> app.dialogCancellation(h.getCallbackIdPattern(), h));
        dialogSubmissionHandlers.forEach(h -> app.dialogSubmission(h.getCallbackIdPattern(), h));
        dialogSuggestionHandlers.forEach(h -> app.dialogSuggestion(h.getCallbackIdPattern(), h));
        globalShortcutHandlers.forEach(h -> app.globalShortcut(h.getCallbackIdPattern(), h));
        messageShortcutHandlers.forEach(h -> app.messageShortcut(h.getCallbackIdPattern(), h));
        slashCommandHandlers.forEach(h -> app.command(h.getCommandIdPattern(), h));
        viewClosedHandlers.forEach(h -> app.viewClosed(h.getCallbackIdPattern(), h));
        viewSubmissionHandlers.forEach(h -> app.viewSubmission(h.getCallbackIdPattern(), h));
        workflowStepEditHandlers.forEach(h -> app.workflowStepEdit(h.getCallbackIdPattern(), h));
        workflowStepExecuteHandlers.forEach(h -> app.workflowStepExecute(h.getPattern(), h));
        workflowStepSaveHandlers.forEach(h -> app.workflowStepSave(h.getCallbackIdPattern(), h));

        // prevent duplicates
        boltEventHandlers.forEach(h -> app.event(h.getEventType(), new RunOnceBoltEventHandler<>(duplicateEventsFilter, h)));

        app.service(installationService);
        app.service(oAuthStateService);

        return app;
    }

    private static App createApp(SlackConfiguration configuration, Slack slack) {
        configuration.setSlack(slack);

        if (configuration.isOAuthInstallPathEnabled()) {
            return new App(configuration).asOAuthApp(true);
        }

        if (configuration.isOpenIDConnectEnabled()) {
            return new App(configuration).asOpenIDConnectApp(true);
        }

        return new App(configuration);
    }

    private <T> T createServiceIfS3Configured(SlackConfiguration configuration, AmazonS3 slackS3, AmazonS3 defaultS3, Function<SlackConfiguration, T> defaultService, BiFunction<String, AmazonS3, T> creator) {
        if (StringUtils.isEmpty(configuration.getBucket())) {
            return defaultService.apply(configuration);
        }

        AmazonS3 s3 = slackS3 == null ? defaultS3 : slackS3;

        if (s3 == null) {
            return  defaultService.apply(configuration);
        }

        return creator.apply(configuration.getBucket(), s3);
    }

}
