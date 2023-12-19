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
package com.agorapulse.slack.install;

import com.agorapulse.slack.install.event.BotDeletedEvent;
import com.agorapulse.slack.install.event.BotSavedEvent;
import com.agorapulse.slack.install.event.InstallerDeletedEvent;
import com.agorapulse.slack.install.event.InstallerSavedEvent;
import com.slack.api.bolt.Initializer;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.model.block.LayoutBlock;
import io.micronaut.context.event.ApplicationEventPublisher;

import java.util.List;

public class ObservableInstallationService implements InstallationService {

    private final ApplicationEventPublisher publisher;
    private final InstallationService delegate;

    public ObservableInstallationService(ApplicationEventPublisher publisher, InstallationService delegate) {
        this.publisher = publisher;
        this.delegate = delegate;
    }

    public Class<? extends InstallationService> getDelegateType() {
        return delegate.getClass();
    }

    @Override
    public boolean isHistoricalDataEnabled() {
        return delegate.isHistoricalDataEnabled();
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {
        delegate.setHistoricalDataEnabled(isHistoricalDataEnabled);
    }

    @Override
    public void saveInstallerAndBot(Installer installer) throws Exception {
        delegate.saveInstallerAndBot(installer);

        publisher.publishEvent(new BotSavedEvent(installer.getEnterpriseId(), installer.getTeamId(), installer.getBotUserId()));
        publisher.publishEvent(new InstallerSavedEvent(installer.getEnterpriseId(), installer.getTeamId(), installer.getInstallerUserId()));
    }

    @Override
    public void saveBot(Bot bot) throws Exception {
        delegate.saveBot(bot);

        publisher.publishEvent(new BotSavedEvent(bot.getEnterpriseId(), bot.getTeamId(), bot.getBotUserId()));
    }

    @Override
    public void deleteBot(Bot bot) throws Exception {
        delegate.deleteBot(bot);

        publisher.publishEvent(new BotDeletedEvent(bot.getEnterpriseId(), bot.getTeamId(), bot.getBotUserId()));
    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {
        delegate.deleteInstaller(installer);

        publisher.publishEvent(new InstallerDeletedEvent(installer.getEnterpriseId(), installer.getTeamId(), installer.getInstallerUserId()));
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        return delegate.findBot(enterpriseId, teamId);
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        return delegate.findInstaller(enterpriseId, teamId, userId);
    }

    @Override
    public String getInstallationGuideText(String enterpriseId, String teamId, String userId) {
        return delegate.getInstallationGuideText(enterpriseId, teamId, userId);
    }

    @Override
    public List<LayoutBlock> getInstallationGuideBlocks(String enterpriseId, String teamId, String userId) {
        return delegate.getInstallationGuideBlocks(enterpriseId, teamId, userId);
    }

    @Override
    public void deleteAll(String enterpriseId, String teamId) {
        delegate.deleteAll(enterpriseId, teamId);

        publisher.publishEvent(new BotSavedEvent(enterpriseId, teamId, null));
        publisher.publishEvent(new InstallerSavedEvent(enterpriseId, teamId, null));
    }

    @Override
    public Initializer initializer() {
        return delegate.initializer();
    }

}
