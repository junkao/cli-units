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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPoliciesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerPolicyReader implements CliConfigListReader<SchedulerPolicy, SchedulerPolicyKey, SchedulerPolicyBuilder> {

    private static final String SH_POLICY_MAPS = "do show running-config policy-map | include policy-map";
    private static final Pattern POLICY_NAME_LINE = Pattern.compile("policy-map (?<name>.+)");
    private Cli cli;

    public SchedulerPolicyReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SchedulerPolicyKey> getAllIds(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_POLICY_MAPS, cli, instanceIdentifier, readContext);
        return getSchedulerKeys(output);
    }

    @VisibleForTesting
    public static List<SchedulerPolicyKey> getSchedulerKeys(String output) {
        return ParsingUtils.parseFields(output, 0, POLICY_NAME_LINE::matcher,
            matcher -> matcher.group("name"), SchedulerPolicyKey::new);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<SchedulerPolicy> list) {
        ((SchedulerPoliciesBuilder) builder).setSchedulerPolicy(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier, @Nonnull SchedulerPolicyBuilder schedulerPolicyBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        schedulerPolicyBuilder.setName(policyName);
        schedulerPolicyBuilder.setConfig(new ConfigBuilder().setName(policyName).build());
    }
}
