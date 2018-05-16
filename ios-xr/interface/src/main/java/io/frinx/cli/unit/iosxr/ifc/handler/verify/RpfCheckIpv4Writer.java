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

package io.frinx.cli.unit.iosxr.ifc.handler.verify;

import static io.frinx.cli.unit.iosxr.ifc.handler.verify.RpfCheckUtils.IPV4_VERIFY_CMD_BASE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv4.verify.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RpfCheckIpv4Writer implements CliWriter<Ipv4> {

    //"ipv4 verify unicast source reachable-via any allow-self-ping allow-default"
    private final Cli cli;

    public RpfCheckIpv4Writer(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull final InstanceIdentifier<Ipv4> instanceIdentifier, @Nonnull final Ipv4 dataAfter,
                                       @Nonnull final WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        final StringBuilder verifyCmd = prepareCmd(dataAfter);

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
            f("interface %s", interfaceName),
            verifyCmd.toString(),
            "root");
    }

    @VisibleForTesting
    StringBuilder prepareCmd(final @Nonnull Ipv4 dataAfter) {
        final StringBuilder verifyCmd = new StringBuilder(
            f(IPV4_VERIFY_CMD_BASE, dataAfter.getRpfCheck().getName().toLowerCase())
        );
        RpfCheckUtils.appendAllowConfigCmdParams(dataAfter, verifyCmd);
        return verifyCmd;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Ipv4> id, @Nonnull final Ipv4 dataBefore,
                                        @Nonnull final Ipv4 dataAfter, @Nonnull final WriteContext writeContext)
        throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull final InstanceIdentifier<Ipv4> instanceIdentifier, @Nonnull final Ipv4 dataBefore,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(cli, instanceIdentifier,
            f("interface %s", interfaceName),
            f("no " + IPV4_VERIFY_CMD_BASE, dataBefore.getRpfCheck().getName().toLowerCase()),
            "root");
    }
}