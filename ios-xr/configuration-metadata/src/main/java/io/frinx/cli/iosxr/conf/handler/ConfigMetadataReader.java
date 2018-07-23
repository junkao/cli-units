/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.conf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.norev.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.norev.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReader implements CliOperReader<ConfigurationMetadata, ConfigurationMetadataBuilder> {

    private static final String SHOW_LAST_COMMIT_TIME = "show configuration commit list | utility egrep \"^1 \"";
    private static final String DATE_REGEX = "\\s*.+\\s{2,}.+\\s{2,}.+\\s{2,}.+\\s{2,}.+\\s{3,}(?<time>.+)\\s*";
    private static final Pattern PATTERN = Pattern.compile(DATE_REGEX);
    private final Cli cli;

    public ConfigMetadataReader(final Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static Optional<String> getLastConfigurationFingerprint(String timeFormat) throws ParseException {

        return ParsingUtils.parseField(timeFormat, 0, PATTERN::matcher, m -> m.group("time"));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConfigurationMetadata> instanceIdentifier, @Nonnull
            ConfigurationMetadataBuilder configurationMetadataBuilder, @Nonnull ReadContext readContext) throws
            ReadFailedException {
        String output = blockingRead(SHOW_LAST_COMMIT_TIME, cli, instanceIdentifier, readContext);

        try {
            Optional<String> data = getLastConfigurationFingerprint(output);
            data.ifPresent(configurationMetadataBuilder::setLastConfigurationFingerprint);
        } catch (ParseException e) {
            throw new ReadFailedException(instanceIdentifier, e);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull ConfigurationMetadata
            configurationMetadata) {
        // NOOP, root reader
    }
}

