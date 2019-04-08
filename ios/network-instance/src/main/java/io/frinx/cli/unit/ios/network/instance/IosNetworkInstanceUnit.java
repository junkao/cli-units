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

package io.frinx.cli.unit.ios.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.table.TableConnectionConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.table.TableConnectionReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosNetworkInstanceUnit extends AbstractUnit {

    public IosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS Network Instance (Openconfig) translate unit";
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        CheckRegistry checkRegistry = ChecksMap.getOpenconfigCheckRegistry();
        readRegistry.setCheckRegistry(checkRegistry);
        provideReaders(readRegistry, cli);
        writeRegistry.setCheckRegistry(checkRegistry);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);

        writeRegistry.addAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli),
                /*handle after ifc configuration*/
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG,
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG);

        writeRegistry.subtreeAddAfter(IIDs.NE_NE_CONNECTIONPOINTS,
                new ConnectionPointsWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG,
                        IIDs.NE_NE_CO_CO_ENDPOINTS,
                        IIDs.NE_NE_CO_CO_EN_ENDPOINT,
                        IIDs.NE_NE_CO_CO_EN_EN_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_LOCAL,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_REMOTE,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG),
                /*handle after network instance configuration*/ IIDs.NE_NE_CONFIG);

        //todo create proper writers once we support routing policies
        writeRegistry.addNoop(IIDs.NE_NE_INTERINSTANCEPOLICIES);
        writeRegistry.addNoop(IIDs.NE_NE_IN_APPLYPOLICY);
        writeRegistry.addNoop(IIDs.NE_NE_IN_AP_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli),
                IIDs.NE_NE_IN_INTERFACE);

        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new LocalAggregateConfigWriter(cli),
                IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        // Interfaces for VRF
        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.addAfter(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceWriter(cli),
                IIDs.NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_IN_IN_CONFIG);

        // Table connections for VRF
        writeRegistry.addNoop(IIDs.NE_NE_TABLECONNECTIONS);
        writeRegistry.addNoop(IIDs.NE_NE_TA_TABLECONNECTION);
        writeRegistry.addAfter(IIDs.NE_NE_TA_TA_CONFIG, new TableConnectionConfigWriter(cli),
                /*add after protocol writers*/
                IIDs.NE_NE_PR_PR_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs, L2P2P
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli));

        // Interfaces for VRF
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceReader(cli));

        // Protocols for VRF
        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader());

        // Local aggregates
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new LocalAggregateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new LocalAggregateConfigReader(cli));

        // Table connections for VRF
        readRegistry.subtreeAdd(IIDs.NE_NE_TA_TABLECONNECTION, new TableConnectionReader(cli),
                Sets.newHashSet(IIDs.NE_NE_TA_TA_CONFIG));

        // Connection points for L2P2p
        readRegistry.subtreeAdd(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsReader(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG,
                        IIDs.NE_NE_CO_CO_STATE,
                        IIDs.NE_NE_CO_CO_ENDPOINTS,
                        IIDs.NE_NE_CO_CO_EN_ENDPOINT,
                        IIDs.NE_NE_CO_CO_EN_EN_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_STATE,
                        IIDs.NE_NE_CO_CO_EN_EN_LOCAL,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_STATE,
                        IIDs.NE_NE_CO_CO_EN_EN_REMOTE,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_STATE));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }
}
