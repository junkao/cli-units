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

package io.frinx.cli.unit.huawei.ifc.handler;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    // FIXME: https://github.com/tomj74/chunk-templates/pull/29
    private static final String WRITE_TEMPLATE = "system-view\n"
        + "interface ${data.name}\n"
        + "{$data|update(mtu,mtu `$data.mtu`\n,undo mtu\n)}"
        + "{$data|update(description,description `$data.description`\n,undo description\n)}"
        //  + "{$data|update(is_enabled,shutdown\n,undo shutdown\n}"
        + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
        + "commit\n"
        + "return";


    private static final String DELETE_TEMPLATE = "system-view\n"
        + "undo interface ${data.name}\n"
        + "commit\n"
        + "return";


    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        return fT(WRITE_TEMPLATE, "before", before, "data", after,
            "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
    }

    @Override
    public boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data);
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }
}
