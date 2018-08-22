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

package io.frinx.cli.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospfv3.OspfV3Writer;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfV3ProtocolWriter implements OspfV3Writer<Config> {

    private Cli cli;

    public OspfV3ProtocolWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> iid, Config data, WriteContext context)
            throws WriteFailedException {
        final String processName = iid.firstKeyOf(Protocol.class).getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);

        blockingWriteAndRead(cli, iid, data,
                f("router ospfv3 %s %s", processName, nwInsName),
                "root");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> iid, Config dataBefore, Config dataAfter,
                                               WriteContext context) throws WriteFailedException {
        // NOOP
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> iid, Config data, WriteContext context)
            throws WriteFailedException {
        final String processName = iid.firstKeyOf(Protocol.class)
                .getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);
        blockingWriteAndRead(cli, iid, data,
                f("no router ospfv3 %s %s", processName, nwInsName));
    }
}
