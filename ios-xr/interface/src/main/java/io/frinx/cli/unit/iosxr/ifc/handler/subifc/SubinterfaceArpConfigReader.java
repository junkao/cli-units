/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceArpConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final Pattern CACHE_TIMEOUT_LINE = Pattern.compile("arp timeout (?<timeout>\\d+)");

    private Cli cli;

    public SubinterfaceArpConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        Long subId = id.firstKeyOf(Subinterface.class).getIndex();
        // Only parse configuration for non 0 subifc
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            return;
        }

        parseLoadInterval(blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, Util.getSubinterfaceName(id)),
            cli, id, ctx), builder);
    }

    private static void parseLoadInterval(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0,
            CACHE_TIMEOUT_LINE::matcher,
            matcher -> Long.valueOf(matcher.group("timeout")),
            builder::setCacheTimeout);
    }
}
