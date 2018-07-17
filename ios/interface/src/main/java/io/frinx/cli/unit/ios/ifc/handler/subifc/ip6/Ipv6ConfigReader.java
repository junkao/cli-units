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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import static io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6AddressReader.IPV6_UNICAST_ADDRESS;
import static io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6AddressReader.SH_INTERFACE_IP;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public Ipv6ConfigReader(Cli cli) {
        this.cli = cli;
    }

    public static final short DEFAULT_PREFIX_LENGHT = (short) 64;

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class)
                .getName();
        Long subId = id.firstKeyOf(Subinterface.class)
                .getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            parseAddressConfig(configBuilder, blockingRead(String.format(SH_INTERFACE_IP, name), cli, id,
                    readContext), id);
        }
    }

    @VisibleForTesting
    static void parseAddressConfig(ConfigBuilder configBuilder, String output, InstanceIdentifier<Config> id) {
        Ipv6AddressNoZone address = id.firstKeyOf(Address.class)
                .getIp();
        configBuilder.setIp(address);
        configBuilder.setPrefixLength(DEFAULT_PREFIX_LENGHT);

        output = NEWLINE.splitAsStream(output)
            .filter(line -> line.contains(address.getValue()))
                .findAny()
                .orElse("");

        parseField(output,
                IPV6_UNICAST_ADDRESS::matcher,
            m -> Short.parseShort(m.group("prefix")),
                configBuilder::setPrefixLength);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AddressBuilder) builder).setConfig(config);
    }
}
