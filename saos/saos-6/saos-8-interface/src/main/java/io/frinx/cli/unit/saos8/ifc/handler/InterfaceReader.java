/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_PORTS = "configuration search string \" port \"";
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile(".*port (?<id>\\S+).*");

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SH_PORTS, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    List<InterfaceKey> parseInterfaceIds(String output) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("lldp"))
                .filter(l -> !l.startsWith("port tdm"))
                .map(INTERFACE_ID_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .filter(Objects::nonNull)
                .map(InterfaceKey::new)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}

