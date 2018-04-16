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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.all.afi.safi.common.prefix.limit.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiPrefixLimitConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborAfiSafiPrefixLimitConfigWriter(Cli cli) {
        this.cli = cli;
    }

    static final String NEIGHBOR_AFI_PREFIX_LIMIT ="router bgp {$as} {$instance}\n" +
            "neighbor {$address}\n" +
            "address-family {$afiSafi}\n" +
            "{.if ($config.max_prefixes) }maximum-prefix {$config.max_prefixes}{.else}no maximum-prefix{/if}" +
            "{.if ($config.shutdown_threshold_pct.value) } {$config.shutdown_threshold_pct.value}{/if}" +
            "\n" +
            "exit\n" +
            "exit\n" +
            "exit\n";

    static final String DELETE_NEIGHBOR_AFI_PREFIX_LIMIT ="router bgp {$as} {$instance}\n" +
            "neighbor {$address}\n" +
            "address-family {$afiSafi}\n" +
            "no maximum-prefix\n" +
            "exit\n" +
            "exit\n" +
            "exit\n";

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        validate(config);
        final Global g = Preconditions.checkNotNull(bgpOptional.get().getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        blockingWriteAndRead(fT(NEIGHBOR_AFI_PREFIX_LIMIT,
                "as", g.getConfig().getAs().getValue(),
                "instance", instName,
                "address", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue()),
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(id.firstKeyOf(AfiSafi.class).getAfiSafiName()),
                "config", config),
                cli, id, config);
    }

    private static void validate(Config config) {
        if (config.getShutdownThresholdPct() != null)
            Preconditions.checkArgument(config.getMaxPrefixes() != null, "shutdown-threshold-pct cannot be defined when max-prefixes is missing");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get().getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        blockingDeleteAndRead(fT(DELETE_NEIGHBOR_AFI_PREFIX_LIMIT,
                "as", g.getConfig().getAs().getValue(),
                "instance", instName,
                "address", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue()),
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(id.firstKeyOf(AfiSafi.class).getAfiSafiName())),
                cli, id);
    }
}
