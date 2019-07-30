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

package io.frinx.cli.unit.brocade.network.instance.vlan;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.vlan.L2P2PVlanConfigWriter;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.vlan.L2VSIVlanConfigWriter;
import io.frinx.cli.unit.brocade.network.instance.vrf.vlan.DefaultVlanConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;

public final class VlanConfigWriter extends CompositeWriter<Config> implements CliWriter<Config> {

    public VlanConfigWriter(Cli cli) {
        super(Lists.newArrayList(
                new DefaultVlanConfigWriter(cli),
                new L2P2PVlanConfigWriter(),
                new L2VSIVlanConfigWriter()
        ));
    }
}
