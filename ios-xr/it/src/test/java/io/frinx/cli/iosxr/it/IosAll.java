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
package io.frinx.cli.iosxr.it;

import io.frinx.cli.iosxr.bgp.BgpUnit;
import io.frinx.cli.iosxr.ospf.OspfUnit;
import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.cli.unit.ios.xr.init.IosXrCliInitializerUnit;
import io.frinx.cli.unit.iosxr.ifc.IosXRInterfaceUnit;
import io.frinx.cli.unit.iosxr.network.instance.IosXRNetworkInstanceUnit;
import java.net.InetSocketAddress;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeConnectionParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.keepalive.keepalive.strategy.KeepaliveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public class IosAll extends io.frinx.cli.ios.it.IosAll {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("5.3.4")
            .build();

    private static final int PORT = 22;
    private static final String HOST = "192.168.1.211";
    private static final InetSocketAddress IOS_ADDR = new InetSocketAddress(HOST, PORT);

    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(IOS_ALL.getDeviceType())
            .setDeviceVersion(IOS_ALL.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("cisco")
                    .setPassword("cisco")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(30)
                    .setKeepaliveTimeout(30)
                    .setKeepaliveInitialDelay(30)
                    .build())
            .build();


    @Override
    protected Device getDeviceId() {
        return IOS_ALL;
    }

    @Override
    protected CliNode getCliNode() {
        return CLI_CFG;
    }

    protected InetSocketAddress getAddress() {
        return IOS_ADDR;
    }

    @Override
    protected TranslateRegistryImpl getTranslateRegistry(DataBroker mockBroker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(mockBroker);

        new GenericTranslateUnit(reg).init();
        new IosXRInterfaceUnit(reg).init();
        new BgpUnit(reg).init();
        new OspfUnit(reg).init();
        new IosXRNetworkInstanceUnit(reg).init();
        new io.frinx.cli.unit.iosxr.lldp.LldpUnit(reg).init();
        new IosXrCliInitializerUnit(reg).init();
        return reg;
    }

    @Ignore
    @Test
    public void testConnectivity() throws Exception {
        super.testConnectivity();
    }

    @Ignore
    @Test
    public void getInterfacesBA() throws Exception {
        super.getInterfacesBA();
    }

    @Ignore
    @Test
    public void getAllDOM() throws Exception {
        super.getAllDOM();
    }

    @Ignore
    @Test
    public void getAllDOMBenchmark() throws Exception {
        super.getAllDOMBenchmark();
    }
}
