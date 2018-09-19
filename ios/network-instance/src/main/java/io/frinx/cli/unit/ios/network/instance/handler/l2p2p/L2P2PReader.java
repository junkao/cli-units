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

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.registry.common.CompositeListReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PReader implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public static final String SH_INTERFACES_XCONNECT = "show running-config | include ^interface|^ *xconnect";
    public static final Pattern XCONNECT_ID_LINE = Pattern.compile("(?<interface>\\S+)\\s+xconnect\\s+(?<ip>\\S+)\\s+"
            + "(?<vccid>\\S+)\\s+(?<encaps>.*)");
    private static final Pattern PW_CLASS = Pattern.compile(".*pw-class (?<pwclass>\\S+)");

    public static final String SH_LOCAL_CONNECT = "show running-config | include interworking ethernet";
    public static final Pattern LOCAL_CONNECT_ID_LINE = Pattern.compile("connect (?<network>\\S+)\\s+"
            + "(?<interface1>\\S+)\\s+(?<interface2>\\S+)\\s+interworking ethernet");

    private Cli cli;

    public L2P2PReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(instanceIdentifier, readContext, this.cli, this);
    }

    static List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<?> instanceIdentifier,
                                              @Nonnull ReadContext readContext,
                                              @Nonnull Cli cli,
                                              @Nonnull CliReader reader) throws ReadFailedException {
        if (!instanceIdentifier.getTargetType()
                .equals(NetworkInstance.class)) {
            instanceIdentifier = RWUtils.cutId(instanceIdentifier, NetworkInstance.class);
        }

        // Parse xconnect based local-remote l2p2p
        List<NetworkInstanceKey> l2Ids = parseXconnectIds(reader.blockingRead(SH_INTERFACES_XCONNECT, cli,
                instanceIdentifier, readContext));
        // Parse xconnect based local-local l2p2p
        l2Ids.addAll(parseLocalConnectIds(reader.blockingRead(SH_LOCAL_CONNECT, cli, instanceIdentifier, readContext)));

        return l2Ids;
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseXconnectIds(String output) {
        String linePerInterface = realignXconnectInterfacesOutput(output);

        return ParsingUtils.parseFields(linePerInterface, 0,
                XCONNECT_ID_LINE::matcher,
                L2P2PReader::getXconnectId,
                NetworkInstanceKey::new);
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseLocalConnectIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                LOCAL_CONNECT_ID_LINE::matcher,
            matcher -> matcher.group("network"),
                NetworkInstanceKey::new);
    }

    public static String realignXconnectInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }

    private static String getXconnectId(Matcher matcher) {
        String ifc = matcher.group("interface");
        String ipRemote = matcher.group("ip");
        String encaps = matcher.group("encaps");

        Matcher pwClass = PW_CLASS.matcher(encaps);

        // Use pw-class as l2vpn name if possible
        if (pwClass.matches()) {
            return pwClass.group("pwclass");
        } else {
            // otherwise use interface + remote IP as name
            return String.format("%s xconnect %s", ifc, ipRemote);
        }
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @Nonnull NetworkInstanceBuilder networkInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class)
                .getName();
        networkInstanceBuilder.setName(name);
    }
}
