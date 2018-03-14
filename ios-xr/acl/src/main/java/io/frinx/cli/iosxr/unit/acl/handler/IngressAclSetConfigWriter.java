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

package io.frinx.cli.iosxr.unit.acl.handler;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.unit.acl.handler.util.AclUtil;
import io.frinx.cli.iosxr.unit.acl.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.Acl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;

public class IngressAclSetConfigWriter implements CliWriter<Config> {

    static final String MOD_CURR_ATTR = "interface {$name}\n" +
            "{% if ($delete) %}no {%endif%}{$aclType} access-group {$config.set_name} ingress\n" +
            "exit\n";

    private final Cli cli;

    public IngressAclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        InterfaceCheckUtil.checkInterface(writeContext, interfaceName);
        final InstanceIdentifier<AclSets> aclSetIID = RWUtils.cutId(instanceIdentifier, Acl.class).child(AclSets.class);
        AclUtil.checkAclExists(aclSetIID, config, writeContext);

        final Class<? extends ACLTYPE> aclType = config.getType();
        checkArgument(aclType != null, "Missing acl type");

        checkIngressAclSetConfigExists(instanceIdentifier, aclType, writeContext, interfaceName);
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(MOD_CURR_ATTR,
                "name", interfaceName,
                "aclType", AclUtil.getStringType(aclType),
                "config", config));
    }

    private void checkIngressAclSetConfigExists(final InstanceIdentifier<Config> instanceIdentifier,
                                                final Class<? extends ACLTYPE> aclType,
                                                final WriteContext writeContext,
                                                final String interfaceName) {
        // find ingress acl set already set for type (ipv4/ipv6)
        final Optional<IngressAclSets> ingressAclSetsOptional =
            writeContext.readBefore(RWUtils.cutId(instanceIdentifier, IngressAclSets.class));

        final java.util.Optional<IngressAclSet> aclSetName = Stream
            .of(ingressAclSetsOptional)
            .filter(Optional::isPresent)
            .map(item -> item.get().getIngressAclSet())
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(aclSet -> aclSet.getType().equals(aclType))
            .findFirst();

        aclSetName.ifPresent(name -> {
            throw new IllegalArgumentException(f(
                "Could not add ingress-acl-set config for interface %s, already exists for type %s. "
                    + "Please delete ingress-acl-set config with name %s for interface %s",
                interfaceName, aclType, aclSetName.get(), interfaceName));
        });
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey(name)))
                .isPresent();
        if (!ifcExists) {
            // No point in removing ACL from nonexisting ifc
            return;
        }

        final Class<? extends ACLTYPE> aclType = config.getType();
        checkArgument(aclType != null, "Missing acl type");

        blockingWriteAndRead(cli, instanceIdentifier, config, fT(MOD_CURR_ATTR,
                "delete", true,
                "name", name,
                "aclType", AclUtil.getStringType(aclType),
                "config", config));
    }
}
