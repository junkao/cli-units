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

package io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceWriter implements L3VrfListWriter<Interface, InterfaceKey> {

    private final Cli cli;
    private static final String MOD_TEMPLATE = "configure terminal\n" +
            "interface {$anInt.id}\n" +
            "{% if ($delete) %}no{%endif%}ip vrf forwarding {$netInstanceKey.config.name}\n" +
            "end";

    public VrfInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Interface> instanceIdentifier, Interface anInterface,
                                              WriteContext writeContext) throws WriteFailedException {

        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey(anInterface.getId())))
                .isPresent();
        Preconditions.checkArgument(ifcExists, "Interface: %s does not exist, cannot add it to VRF", anInterface.getId());

        final NetworkInstance networkInstance =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();

        blockingWriteAndRead(cli, instanceIdentifier, anInterface,
                fT(MOD_TEMPLATE,
                        "anIntId", anInterface,
                        "netInstanceKey", networkInstance));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Interface> id, Interface dataBefore,
                                               Interface dataAfter, WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Interface> instanceIdentifier, Interface anInterface,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey networkInstanceKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey(anInterface.getId())))
                .isPresent();
        if (!ifcExists) {
            // No point of removing vrf from nonexisting ifc
            return;
        }

        blockingDeleteAndRead(cli, instanceIdentifier,
                fT(MOD_TEMPLATE,
                       "anIntId", anInterface,
                        "netInstKeyName", networkInstanceKey));
    }
}