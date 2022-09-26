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
package com.agorapulse.slack.install;

import com.slack.api.bolt.service.InstallationService;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;

import javax.inject.Singleton;

@Singleton
public class ObservableInstallationServiceFactory implements BeanCreatedEventListener<InstallationService> {
    private final ApplicationEventPublisher publisher;

    public ObservableInstallationServiceFactory(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public InstallationService onCreated(BeanCreatedEvent<InstallationService> event) {
        if (event.getBeanDefinition().getBeanType().equals(InstallationService.class)) {
            return new ObservableInstallationService(publisher, event.getBean());
        }
        return event.getBean();
    }

}
