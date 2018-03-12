/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.TypedReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.RouterAdvertisementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AdvertisementConfigReader implements TypedReader<Config, ConfigBuilder>, CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_SUB_INTERFACE_CFG = "do show running-config interface %s";
    private static final Pattern IPV6_ADVERTISEMENT_ENABLED = Pattern.compile("\\s*ipv6 nd suppress-ra.*");

    private final Cli cli;

    public Ipv6AdvertisementConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull final InstanceIdentifier<Config> id,
                                             @Nonnull final ConfigBuilder configBuilder,
                                             @Nonnull final ReadContext readContext)
        throws ReadFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        // using show running-config interface <GigabitEthernet 0/0/0/0>
        parseAdvertisementConfig(
            blockingRead(String.format(SH_SINGLE_SUB_INTERFACE_CFG, ifcName), cli, id, readContext), configBuilder);
    }

    @Override
    public boolean containsAllKeys(final Stream<? extends Identifier<? extends DataObject>> keys,
                                   final InstanceIdentifier<Config> instanceIdentifier) {
        final InterfaceKey interfaceKey = instanceIdentifier.firstKeyOf(Interface.class);
        final SubinterfaceKey subinterfaceKey = instanceIdentifier.firstKeyOf(Subinterface.class);

        return interfaceKey != null && subinterfaceKey != null &&
            Ipv6CheckUtil.checkParentInterfaceType(interfaceKey.getName(), EthernetCsmacd.class, Ieee8023adLag.class) &&
            Ipv6CheckUtil.checkSubInterfaceId(subinterfaceKey.getIndex(), ZERO_SUBINTERFACE_ID);
    }

    @VisibleForTesting
    void parseAdvertisementConfig(final String output, final ConfigBuilder builder) {
        // ipv6 nd suppress-ra
        ParsingUtils.parseField(output, 0,
            IPV6_ADVERTISEMENT_ENABLED::matcher,
            matcher -> true,
            builder::setSuppress);
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final Config readValue) {
        ((RouterAdvertisementBuilder) parentBuilder).setConfig(readValue);
    }
}