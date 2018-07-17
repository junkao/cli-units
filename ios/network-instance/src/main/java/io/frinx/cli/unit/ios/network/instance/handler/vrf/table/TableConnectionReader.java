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

package io.frinx.cli.unit.ios.network.instance.handler.vrf.table;

import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.table.BgpTableConnectionReader;
import io.frinx.cli.ospf.handler.table.OspfTableConnectionReader;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.TableConnectionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TableConnectionReader
        extends CompositeListReader<TableConnection, TableConnectionKey, TableConnectionBuilder>
        implements CliConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    public TableConnectionReader(Cli cli) {
        super(new ArrayList<ListReaderCustomizer<TableConnection, TableConnectionKey, TableConnectionBuilder>>() {{
                add(new BgpTableConnectionReader(cli));
                add(new OspfTableConnectionReader(cli));
            }
        });
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<TableConnection> list) {
        ((TableConnectionsBuilder) builder).setTableConnection(list);
    }

    @Nonnull
    @Override
    public TableConnectionBuilder getBuilder(@Nonnull InstanceIdentifier<TableConnection> instanceIdentifier) {
        return new TableConnectionBuilder();
    }
}
