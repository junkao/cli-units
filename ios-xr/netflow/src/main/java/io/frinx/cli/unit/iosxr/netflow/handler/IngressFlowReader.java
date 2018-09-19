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

package io.frinx.cli.unit.iosxr.netflow.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.netflow.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.registry.common.TypedListReader;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.IngressFlowsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.IngressFlow;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.IngressFlowBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.IngressFlowKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressFlowReader implements TypedListReader<IngressFlow, IngressFlowKey, IngressFlowBuilder>,
        CliConfigListReader<IngressFlow, IngressFlowKey, IngressFlowBuilder> {

    private static final String SH_NETFLOW_INTF = "show running-config interface %s | include ingress";
    private static final Pattern FLOW_LINE = Pattern.compile("flow (?<type>.+) monitor \\S+( sampler .+)? ingress");

    private final Cli cli;

    public IngressFlowReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean containsAllKeys(final Stream<? extends Identifier<? extends DataObject>> keys,
                                   final InstanceIdentifier<IngressFlow> instanceIdentifier) {
        return InterfaceCheckUtil.checkInterfaceType(instanceIdentifier, EthernetCsmacd.class, Ieee8023adLag.class);
    }

    @Nonnull
    @Override
    public List<IngressFlowKey> getAllIdsForType(@Nonnull InstanceIdentifier<IngressFlow> instanceIdentifier,
                                                 @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class)
                .getId();
        return parseFlowKeys(blockingRead(String.format(SH_NETFLOW_INTF, interfaceId.getValue()), cli,
                instanceIdentifier, readContext));
    }

    private static List<IngressFlowKey> parseFlowKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                FLOW_LINE::matcher,
            matcher -> NetflowUtils.getType(matcher.group("type")),
                IngressFlowKey::new
        );
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<IngressFlow> list) {
        ((IngressFlowsBuilder) builder).setIngressFlow(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<IngressFlow> instanceIdentifier, @Nonnull
            IngressFlowBuilder ingressFlowBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final IngressFlowKey key = instanceIdentifier.firstKeyOf(IngressFlow.class);
        ingressFlowBuilder.setNetflowType(key.getNetflowType());
    }
}
