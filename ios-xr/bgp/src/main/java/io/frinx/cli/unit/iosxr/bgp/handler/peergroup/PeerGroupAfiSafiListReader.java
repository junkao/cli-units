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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupAfiSafiListReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    static final String SH_AFI = "show running-config router bgp %s"
        + " neighbor-group %s | include address-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("address-family (?<family>.+)");
    private Cli cli;

    public PeerGroupAfiSafiListReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIds(@Nonnull InstanceIdentifier<AfiSafi> iid, @Nonnull
            ReadContext context) throws ReadFailedException {
        String networkInstanceName = iid.firstKeyOf(NetworkInstance.class).getName();
        String peerGroupName = iid.firstKeyOf(PeerGroup.class).getPeerGroupName();
        Long as = PeerGroupListReader.readAsNumberFromContext(iid, context);
        if (as == null) {
            return Collections.EMPTY_LIST;
        }
        String cmd = f(SH_AFI, as, peerGroupName);
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(networkInstanceName)) {
            String output = blockingRead(cmd, cli, iid, context);
            return ParsingUtils.parseFields(
                output,
                0,
                FAMILY_LINE::matcher,
                matcher -> matcher.group("family"),
                value -> GlobalAfiSafiReader.transformAfiFromString(value.trim())
            ).stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(AfiSafiKey::new)
            .collect(Collectors.toList());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AfiSafi> iid, @Nonnull
            AfiSafiBuilder afiSafiBuilder, @Nonnull ReadContext context) throws ReadFailedException {
        Class<? extends AFISAFITYPE> cla = iid.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(cla);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(cla).build());
    }
}