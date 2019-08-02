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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceArpConfigReaderTest {
    private static final String SH_RUN = "show running-config interface Bundle-Ether1000.200";
    private static final String SH_RUN_OUTPUT = "interface Bundle-Ether1000.200\n"
        + " encapsulation dot1q 100\n"
        + " service-policy output 100M-Policing-Kakuho\n"
        + " ipv4 address 218.45.246.205 255.255.255.252\n"
        + " ipv6 address 2403:7a00:6:1a::1/64\n"
        + " description D300220206\n"
        + " ipv4 access-group D300000000_out egress\n"
        + " ipv6 access-group D300000000_out egress\n"
        + " arp timeout 60\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private SubinterfaceArpConfigReader target;

    private static final String INTERFACE_NAME = "Bundle-Ether1000";
    private static final Long SUBIFC_INDEX = Long.valueOf(200L);

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);
    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(
        IIDs.IN_IN_SU_SU_AUG_SUBIFAUGETHEXT_AR_CONFIG,
        INTERFACE_KEY, SUBIFC_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new SubinterfaceArpConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
            .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getCacheTimeout(), CoreMatchers.is(60L));
    }
}
