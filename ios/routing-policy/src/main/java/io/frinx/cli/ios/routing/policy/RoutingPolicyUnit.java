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

package io.frinx.cli.ios.routing.policy;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.ios.routing.policy.handlers.ExtCommunitySetConfigWriter;
import io.frinx.cli.ios.routing.policy.handlers.ExtCommunitySetReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.defined.sets.top.DefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit implements TranslateUnit {

    private static final InstanceIdentifier<DefinedSets2> DEFINED_SETS_1 =
        IIDs.RO_DEFINEDSETS.augmentation(DefinedSets2.class);
    private static final InstanceIdentifier<BgpDefinedSets> BGP_DEFINED_SETS =
        DEFINED_SETS_1.child(BgpDefinedSets.class);
    private static final InstanceIdentifier<ExtCommunitySets> EXT_COMMUNITY_SETS =
        BGP_DEFINED_SETS.child(ExtCommunitySets.class);
    private static final InstanceIdentifier<ExtCommunitySet> EXT_COMMUNITY_SET =
        EXT_COMMUNITY_SETS.child(ExtCommunitySet.class);
    private static final InstanceIdentifier<Config> EXT_CS_CONFIG = EXT_COMMUNITY_SET.child(Config.class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosDevices.IOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readerRegistryBuilder,
                                @Nonnull ModifiableWriterRegistryBuilder writerRegistryBuilder,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readerRegistryBuilder, cli);
        provideWriters(writerRegistryBuilder, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writerRegistryBuilder, Cli cli) {
        // provide writers
        writerRegistryBuilder.add(new GenericWriter<>(IIDs.ROUTINGPOLICY, new NoopCliWriter<>()));
        writerRegistryBuilder.add(new GenericWriter<>(IIDs.RO_DEFINEDSETS, new NoopCliWriter<>()));
        writerRegistryBuilder.add(new GenericWriter<>(DEFINED_SETS_1, new NoopCliWriter<>()));
        writerRegistryBuilder.add(new GenericWriter<>(BGP_DEFINED_SETS, new NoopCliWriter<>()));
        writerRegistryBuilder.add(new GenericWriter<>(EXT_COMMUNITY_SETS, new NoopCliWriter<>()));
        writerRegistryBuilder.add(new GenericWriter<>(EXT_COMMUNITY_SET, new NoopCliWriter<>()));
        writerRegistryBuilder.addAfter(new GenericWriter<>(EXT_CS_CONFIG, new ExtCommunitySetConfigWriter(cli)),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readerRegistryBuilder, Cli cli) {
        // provide readers
        readerRegistryBuilder.addStructuralReader(IIDs.ROUTINGPOLICY, RoutingPolicyBuilder.class);
        readerRegistryBuilder.addStructuralReader(IIDs.RO_DEFINEDSETS, DefinedSetsBuilder.class);
        readerRegistryBuilder.addStructuralReader(DEFINED_SETS_1, DefinedSets2Builder.class);
        readerRegistryBuilder.addStructuralReader(BGP_DEFINED_SETS, BgpDefinedSetsBuilder.class);
        readerRegistryBuilder.addStructuralReader(EXT_COMMUNITY_SETS, ExtCommunitySetsBuilder.class);
        readerRegistryBuilder.add(new GenericConfigListReader<>(EXT_COMMUNITY_SET, new ExtCommunitySetReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            $YangModuleInfoImpl.getInstance(),
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.$YangModuleInfoImpl
                .getInstance());
    }

    @Override
    public String toString() {
        return "IOS Routing policy (Openconfig) translate unit";
    }
}
