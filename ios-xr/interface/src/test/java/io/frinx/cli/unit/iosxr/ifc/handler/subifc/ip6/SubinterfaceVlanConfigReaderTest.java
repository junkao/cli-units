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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanLogicalConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class SubinterfaceVlanConfigReaderTest {

    private static final String SH_RUN_INT = "Fri Nov 23 13:18:34.834 UTC\n"
            + "interface Ethernet1/1.5\n"
            + " encapsulation dot1q 25\n"
            + "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setVlanId(new VlanLogicalConfig.VlanId(new VlanId(25)))
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new SubinterfaceVlanConfigReader(Mockito.mock(Cli.class));
        SubinterfaceVlanConfigReader.parseVlanTag(SH_RUN_INT, actualConfig,
                Pattern.compile("encapsulation dot1q (?<tag>[0-9]+)"));
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

    }
}
