/*
 * Copyright 2022 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bloomberg.bmq.it.util;

import com.bloomberg.bmq.SessionOptions;
import com.bloomberg.bmq.Uri;
import com.bloomberg.bmq.impl.infr.util.Argument;
import com.bloomberg.bmq.impl.infr.util.SystemUtil;
import java.io.IOException;
import java.util.UUID;

public interface BmqBroker extends TestTcpServer {

    enum Domains {
        Priority("bmq.test.mmap.priority"),
        Fanout("bmq.test.mmap.fanout");

        private final String domain;

        Domains(String domain) {
            this.domain = domain;
        }

        public String value() {
            return domain;
        }

        public Uri generateQueueUri() {
            return generateQueueUri("java-it-", true, null);
        }

        public Uri generateQueueUri(Uri uri, String appId) {
            return generateQueueUri(uri.queue(), false, appId);
        }

        private Uri generateQueueUri(String prefix, boolean addUniqueId, String appId) {
            String sb = "bmq://" + domain + "/" + generateQueueName(prefix, addUniqueId, appId);
            return new Uri(sb);
        }

        private String generateQueueName(String prefix, boolean addUniqueId, String appId) {
            Argument.expectNonNull(prefix, "prefix");
            Argument.expectCondition(!prefix.isEmpty(), "'prefix' must be non-empty");

            final boolean hasAppId = appId != null && !appId.isEmpty();
            if (hasAppId) {
                Argument.expectCondition(
                        this == Fanout, "'appId' can be non-empty only for Fanout");
            }

            StringBuilder sb = new StringBuilder().append(prefix);

            if (addUniqueId) {
                sb.append(UUID.randomUUID());
            }

            if (hasAppId) {
                sb.append("?id=").append(appId);
            }

            return sb.toString();
        }
    }

    String defaultTier();

    SessionOptions sessionOptions();

    void setDropTmpFolder();

    void setDumpBrokerOutput();

    static BmqBroker createStartedBroker() throws IOException {
        int port = SystemUtil.getEphemeralPort();

        return createStartedBroker(port);
    }

    static BmqBroker createStartedBroker(int port) throws IOException {
        BmqBroker bmqBroker = createStoppedBroker(port);
        bmqBroker.start();

        return bmqBroker;
    }

    static BmqBroker createStoppedBroker() throws IOException {
        int port = SystemUtil.getEphemeralPort();

        return createStoppedBroker(port);
    }

    static BmqBroker createStoppedBroker(int port) throws IOException {
        return nonDockerized()
                ? BmqBrokerTestServer.createStoppedBroker(port)
                : BmqBrokerContainer.createContainer(port);
    }

    static boolean nonDockerized() {
        return System.getProperty("bmqBroker.nonDockerized") != null;
    }
}
