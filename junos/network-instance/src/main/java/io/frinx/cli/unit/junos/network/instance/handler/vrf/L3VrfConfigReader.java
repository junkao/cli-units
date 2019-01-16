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

package io.frinx.cli.unit.junos.network.instance.handler.vrf;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigReader
        implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private Cli cli;

    public L3VrfConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> instanceIdentifier,
        @Nonnull ConfigBuilder configBuilder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(name)) {
            return;
        }

        if (isL3Vrf(name, instanceIdentifier, readContext)) {
            configBuilder.setName(name);
            configBuilder.setType(L3VRF.class);
        }
    }

    @VisibleForTesting
    boolean isL3Vrf(String name, InstanceIdentifier<Config> instanceIdentifier, ReadContext readContext)
        throws ReadFailedException {

        return L3VrfReader.getL3VrfNames(this, cli, instanceIdentifier, readContext).stream()
                .filter(name::equals)
                .findFirst()
                .isPresent();
    }

    @Override
    public ConfigBuilder getBuilder(InstanceIdentifier<Config> builder) {
        // NOOP
        throw new UnsupportedOperationException("Should not be invoked");
    }
}