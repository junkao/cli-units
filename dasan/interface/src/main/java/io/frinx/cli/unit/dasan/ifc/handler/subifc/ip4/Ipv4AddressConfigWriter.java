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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv4AddressConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!Ipv4AddressConfigReader.checkSubId(instanceIdentifier)) {
            throw new IllegalArgumentException("Unable to manage IP for subinterface: "
                + instanceIdentifier.firstKeyOf(Subinterface.class).getIndex());
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config, "configure terminal",
                f("interface %s", ifcName.replace("Vlan", "br")),
                f("ip address %s/%d", config.getIp().getValue(), config.getPrefixLength()), "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new IllegalArgumentException("Unable to manage IP for subinterface: " + subId);
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config, "configure terminal",
                f("interface %s", ifcName.replace("Vlan", "br")), "no ip address", "end");
    }
}