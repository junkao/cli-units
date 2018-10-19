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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortVlanMemberConfigWriterTest {

    private static final String WRITE_INPUT_001 = "configure terminal\n"
            + "bridge\n"
            + "vlan add 1,2,3 3/4 tagged\n"
            + "end\n";
    private static final String WRITE_INPUT_002 = "configure terminal\n"
            + "bridge\n"
            + "vlan add 3 3/4 untagged\n"
            + "end\n";

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private PhysicalPortVlanMemberConfigWriter target;
    private InstanceIdentifier<Config> id;
    private ConfigBuilder builder = new ConfigBuilder();
    // test data
    private Config data;
    List<TrunkVlans> lst;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PhysicalPortVlanMemberConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("Ethernet3/4"))
                .augmentation(Interface1.class).child(Ethernet.class).augmentation(Ethernet1.class)
                .child(SwitchedVlan.class).child(Config.class);

        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ethIfAggregationConfigBuilder.setAggregateId("Bundle-Ether8");
        lst = new ArrayList<>();
        TrunkVlans trunkVlans1 = new TrunkVlans(new VlanId(1));
        TrunkVlans trunkVlans2 = new TrunkVlans(new VlanId(2));
        TrunkVlans trunkVlans3 = new TrunkVlans(new VlanId(3));
        lst.add(trunkVlans1);
        lst.add(trunkVlans2);
        lst.add(trunkVlans3);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        target.writeCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_001, response.getValue()
                .getContent());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        VlanId vlanId = new VlanId(3);
        data = builder.setInterfaceMode(VlanModeType.ACCESS).setAccessVlan(vlanId).build();
        target.writeCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_002, response.getValue()
                .getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        VlanId vlanId = new VlanId(3);
        data = builder.setInterfaceMode(VlanModeType.ACCESS).setAccessVlan(vlanId).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        VlanId vlanId = new VlanId(3);
        Config newData = builder.setInterfaceMode(VlanModeType.ACCESS).setAccessVlan(vlanId).build();
        target.updateCurrentAttributes(id, data, newData, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        Config newData = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        target.updateCurrentAttributes(id, data, newData, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testvalidateIfcConfiguration_001() throws Exception {
        data = builder.build();
        Set<Integer> lsi = target.getVlanIds(data);
        Assert.assertEquals(lsi.size(), 0);
    }

}