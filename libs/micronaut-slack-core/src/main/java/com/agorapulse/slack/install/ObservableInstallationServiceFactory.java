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
