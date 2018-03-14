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

package io.frinx.cli.unit.brocade.ifc.handler;

import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getIfcNumber;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getTypeOnDevice;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.parseType;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8A88;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TpIdInterfaceWriter implements CliWriter<Config1> {

    static final String MOD_CURR_ATTR = "configure terminal\n" +
            "tag-type {$tpId} {$typeOnDev} {$ifc}\n" +
            "end";

    private Cli cli;

    public TpIdInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                       @Nonnull Config1 dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String typeOnDevice = getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);

        String tpIdForDevice = getTpIdForDevice(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter, fT(MOD_CURR_ATTR,
                "tpId", tpIdForDevice,
                "typeOnDev", typeOnDevice,
                "ifc", ifcNumber));
    }

    private static String getTpIdForDevice(@Nonnull Config1 dataAfter) {
        if (dataAfter.getTpid() == TPID0X8A88.class) {
            return "88A8";
        } else {
            String simpleTpIdClassName = dataAfter.getTpid().getSimpleName().toLowerCase();
            return simpleTpIdClassName.substring(simpleTpIdClassName.indexOf('x') + 1).toUpperCase();
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                        @Nonnull Config1 dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String typeOnDevice = getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);

        String tpIdForDevice = getTpIdForDevice(dataBefore);

        blockingDeleteAndRead(cli, id, fT(MOD_CURR_ATTR,
                "tpId", tpIdForDevice,
                "typeOnDev", typeOnDevice,
                "ifc", ifcNumber));
    }
}
