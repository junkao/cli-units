/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import static io.frinx.cli.ios.bgp.handler.BgpProtocolReader.DEFAULT_BGP_INSTANCE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class GlobalConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {


    private static final String SH_SUMM = "sh run | include ^router bgp|^ *address-family|^ *bgp router-id";
    private static final String AS_GROUP = "as";
    private static final String ROUTER_ID_GROUP = "routerId";
    private static final Pattern AS_PATTERN = Pattern.compile("router bgp (?<as>\\S*).*");
    private static final Pattern ROUTER_ID_PATTERN = Pattern.compile("bgp router-id (?<routerId>\\S*).*");

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        parseConfigAttributes(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext),
            configBuilder, vrfName);

        parseGlobalAs(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(String output, ConfigBuilder builder, String vrfName) {
        String[] vrfSplit = NeighborReader.getSplitedOutput(output);

        if(DEFAULT_BGP_INSTANCE.equals(vrfName)) {
            if (vrfSplit.length > 1 && vrfSplit[0] != null) {
                setGlobalRouterId(builder, vrfSplit[0]);
            }
        } else {
            parseVrf(builder, vrfName, vrfSplit);
        }
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
            Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();
        optionalVrfOutput.ifPresent(value -> setGlobalRouterId(configBuilder, value));
    }

    private static void setGlobalRouterId(ConfigBuilder cBuilder, String output) {
        ParsingUtils.parseField(output.replaceAll("bgp router-id", "\nbgp router-id"), 0,
            ROUTER_ID_PATTERN::matcher,
            matcher -> matcher.group(ROUTER_ID_GROUP),
            (String value) -> cBuilder.setRouterId(new DottedQuad(value)));
    }

    @VisibleForTesting
    public static void parseGlobalAs(String output, ConfigBuilder cBuilder) {
        ParsingUtils.parseField(output, 0,
            AS_PATTERN::matcher,
            matcher -> matcher.group(AS_GROUP),
            (String value) -> cBuilder.setAs(new AsNumber(Long.valueOf(value))));
    }

}
