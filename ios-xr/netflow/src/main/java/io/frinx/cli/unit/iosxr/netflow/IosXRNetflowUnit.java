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

package io.frinx.cli.unit.iosxr.netflow;

import static io.frinx.cli.iosxr.IosXrDevices.IOS_XR_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.api.TranslationUnitCollector.Registration;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.NetflowInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.NetflowInterfaceReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.netflow.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.EgressFlowsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.IngressFlowsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.top.NetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRNetflowUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private Registration reg;

    public IosXRNetflowUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.$YangModuleInfoImpl.getInstance()
        );
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();

        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(final ModifiableWriterRegistryBuilder wRegistry, final Cli cli) {
        wRegistry.add(new GenericWriter<>(IIDs.NE_INTERFACES, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_INTERFACE, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_CONFIG, new NoopCliWriter<>()));

        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_INGRESSFLOWS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_IN_INGRESSFLOW, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_IN_IN_CONFIG, new IngressFlowConfigWriter(cli)));

        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_EGRESSFLOWS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_EG_EGRESSFLOW, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_IN_IN_EG_EG_CONFIG, new EgressFlowConfigWriter(cli)));
    }

    private void provideReaders(final ModifiableReaderRegistryBuilder rRegistry, final Cli cli) {
        rRegistry.addStructuralReader(IIDs.NETFLOW, NetflowBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_IN_INTERFACE, new NetflowInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_IN_IN_CONFIG, new NetflowInterfaceConfigReader()));

        rRegistry.addStructuralReader(IIDs.NE_IN_IN_INGRESSFLOWS, IngressFlowsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_IN_IN_IN_INGRESSFLOW, new IngressFlowReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_IN_IN_IN_IN_CONFIG, new IngressFlowConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_IN_IN_EGRESSFLOWS, EgressFlowsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_IN_IN_EG_EGRESSFLOW, new EgressFlowReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_IN_IN_EG_EG_CONFIG, new EgressFlowConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR Netflow translate unit";
    }

}