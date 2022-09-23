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

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PortProvider {

    private PortProvider() {
    }

    private static final int MINIMUM = 1024;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentMap<String, Integer> PORTS = new ConcurrentHashMap<>();

    public static int getPort(String name) {
        return PORTS.computeIfAbsent(name, key -> randomPort());
    }

    private static int randomPort() {
        while (true) {
            int randomPort = RANDOM.nextInt(9999);
            if (randomPort < MINIMUM) {
                randomPort += MINIMUM;
            }
            if (isAvailable(randomPort)) {
                return randomPort;
            }
        }
    }

    private static boolean isAvailable(int port) {
        try (Socket ignored = new Socket("127.0.0.1", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
}
