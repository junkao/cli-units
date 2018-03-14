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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.netflow.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class EgressFlowConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    static final String MOD_CURR_ATTR = "interface {$ifcName}\n" +
            "{% if ($delete) %}no {%endif%}flow {$netflowType} monitor {$dataAfter.monitor_name} " +
            "{% if ($dataAfter.sampler_name|onempty(EMPTY) != EMPTY) %} {% if (!$delete) %}sampler {$dataAfter.sampler_name} {%endif%}{%endif%}egress\n" +
            "exit";

    public EgressFlowConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void writeCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataAfter,
                                       @Nonnull final WriteContext writeContext) throws WriteFailedException {
        InterfaceCheckUtil.checkInterfaceTypeWithException(id, EthernetCsmacd.class, Ieee8023adLag.class);
        final InterfaceKey interfaceKey = NetflowUtils.checkInterfaceExists(id, writeContext);

        String ifcName = interfaceKey.getId().getValue();
        blockingWriteAndRead(cli, id, dataAfter, fT(MOD_CURR_ATTR,
                "ifcName", ifcName,
                "netflowType", NetflowUtils.getNetflowStringType(dataAfter.getNetflowType()),
                "dataAfter", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        InterfaceCheckUtil.checkInterfaceTypeWithException(id, EthernetCsmacd.class, Ieee8023adLag.class);

        String ifcName = id.firstKeyOf(Interface.class).getId().getValue();
        blockingDeleteAndRead(cli, id, fT(MOD_CURR_ATTR,
                "ifcName", ifcName,
                "netFlowType", NetflowUtils.getNetflowStringType(dataBefore.getNetflowType()),
                "dataAfter", dataBefore,
                "delete", true));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter, @Nonnull final WriteContext writeContext)
        throws WriteFailedException {

        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}
