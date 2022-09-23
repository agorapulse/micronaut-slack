package com.agorapulse.slack.install.enumerate;

import com.slack.api.bolt.model.Bot;

import java.util.stream.Stream;

public interface InstallationEnumerationService {
    Stream<Bot> findAllBots();

}
