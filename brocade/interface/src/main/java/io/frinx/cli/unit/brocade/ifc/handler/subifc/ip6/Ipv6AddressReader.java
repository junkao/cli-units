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

package io.frinx.cli.unit.brocade.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6AddressesReader;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class Ipv6AddressReader extends AbstractIpv6AddressesReader {

    static final String SH_INTERFACE_IP = "show running-config interface {$ifcType} {$ifcNumber} | include ipv6";
    private static final Pattern IPV6_LOCAL_ADDRESS = Pattern.compile(
            "ipv6 address (?<ipv6local>fe80:[^\\\\s]+)/(?<prefix>[0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8])");
    private static final Pattern IPV6_UNICAST_ADDRESS = Pattern.compile(
            "ipv6 address (?!(fe80:))(?<ipv6unicast>[^\\\\s]+)/(?<prefix>[0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8])");

    public Ipv6AddressReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        Class<? extends InterfaceType> ifcType = Util.parseType(ifcName);
        String ifcNumber = Util.getIfcNumber(ifcName);
        return fT(SH_INTERFACE_IP, "ifcType", Util.getTypeOnDevice(ifcType), "ifcNumber", ifcNumber);
    }

    @Override
    protected Pattern getLocalIpLine() {
        return IPV6_LOCAL_ADDRESS;
    }

    @Override
    protected Pattern getUnicastIpLine() {
        return IPV6_UNICAST_ADDRESS;
    }
}