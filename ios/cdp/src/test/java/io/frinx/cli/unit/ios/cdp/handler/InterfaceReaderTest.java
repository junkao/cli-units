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

package io.frinx.cli.unit.ios.cdp.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String XE_OUTPUT = "GigabitEthernet1 is up, line protocol is up\n"
            + "  Encapsulation "
            + "ARPA\n"
            + "  Sending CDP packets every 60 seconds\n"
            + "  Holdtime is 180 seconds\n"
            + "\n"
            + " cdp "
            + "enabled interfaces : 1\n"
            + " interfaces up          : 1\n"
            + " interfaces down        : 0\n";

    private static final List<InterfaceKey> XE_EXPECTED = Lists.newArrayList("GigabitEthernet1")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final String IOS_OUTPUT = "FastEthernet0/0 is up, line protocol is up\n"
            + "  Encapsulation "
            + "ARPA\n"
            + "  Sending CDP packets every 60 seconds\n"
            + "  Holdtime is 180 seconds\n"
            + "GigabitEthernet1/0 is up, line protocol is up\n"
            + "  Encapsulation ARPA\n"
            + "  Sending CDP packets "
            + "every 60 seconds\n"
            + "  Holdtime is 180 seconds\n"
            + "GigabitEthernet2/0 is up, line protocol is "
            + "up\n"
            + "  Encapsulation ARPA\n"
            + "  Sending CDP packets every 60 seconds\n"
            + "  Holdtime is 180 "
            + "seconds\n";

    private static final List<InterfaceKey> IOS_EXPECTED = Lists.newArrayList("FastEthernet0/0",
            "GigabitEthernet1/0", "GigabitEthernet2/0")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testCdpInterfaceIds() throws Exception {
        Assert.assertEquals(IOS_EXPECTED, InterfaceReader.parseCdpInterfaces(IOS_OUTPUT));
        Assert.assertEquals(XE_EXPECTED, InterfaceReader.parseCdpInterfaces(XE_OUTPUT));
    }
}