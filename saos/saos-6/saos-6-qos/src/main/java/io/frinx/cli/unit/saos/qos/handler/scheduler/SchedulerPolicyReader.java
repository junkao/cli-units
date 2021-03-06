/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.qos.handler.scheduler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.qos.handler.scheduler.profile.ProfileSchedulerPolicyReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.service.ServiceSchedulerPolicyReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;

public class SchedulerPolicyReader
        extends CompositeListReader<SchedulerPolicy, SchedulerPolicyKey, SchedulerPolicyBuilder>
        implements CliConfigListReader<SchedulerPolicy, SchedulerPolicyKey, SchedulerPolicyBuilder> {

    public SchedulerPolicyReader(Cli cli) {
        super(Lists.newArrayList(
                new ProfileSchedulerPolicyReader(cli),
                new ServiceSchedulerPolicyReader(cli)
        ));
    }
}
