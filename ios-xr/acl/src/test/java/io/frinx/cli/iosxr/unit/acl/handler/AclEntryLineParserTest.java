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

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.IcmpMsgType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.acl.icmp.type.IcmpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange.Enumeration;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;

public class AclEntryLineParserTest {

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport, icmpMessageType);
    }

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport, null);
    }

    static AclEntry createIpv6AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(sequenceId, fwdAction, null, ipv6Config, transport, icmpMessageType);
    }

    static AclEntry createIpv6AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, null, ipv6Config, transport, null);
    }

    static AclEntry createAclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                   Config ipv4Config,
                                   org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                           .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                   Transport transport, final Short icmpMessageType) {

        Actions actions = AclEntryLineParser.createActions(fwdAction);
        AclEntryBuilder builder = new AclEntryBuilder();
        // sequence id
        builder.setSequenceId(sequenceId);
        builder.setConfig(new ConfigBuilder()
                .setSequenceId(sequenceId)
                .build()
        );
        // fwd action
        builder.setActions(actions);
        builder.setTransport(transport);
        if (ipv4Config != null) {
            // ipv4
            builder.setIpv4(new Ipv4Builder().setConfig(ipv4Config).build());
        } else {
            // ipv6
            builder.setIpv6(new Ipv6Builder().setConfig(ipv6Config).build());
        }
        if (icmpMessageType != null) {
            builder.addAugmentation(AclEntry1.class, new AclEntry1Builder()
                    .setIcmp(new IcmpBuilder()
                            .setConfig(
                                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext
                                            .rev180314.acl.icmp.type.icmp.ConfigBuilder()
                                            .setMsgType(new IcmpMsgType(icmpMessageType))
                                            .build()
                            ).build()
                    ).build()
            );
        }
        return builder.build();
    }

    @Test
    public void testIpv4() {
        String lines = "Mon May 14 14:36:55.408 UTC\n"
                + "ipv4 access-list foo\n"
                + " 2 deny ipv4 host 1.2.3.4 any\n"
                + " 3 permit udp 192.168.1.1/24 10.10.10.10/24\n"
                + " 4 permit tcp host 1.2.3.4 eq www any\n"
                + " 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10\n"
                + " 6 permit udp 0.0.0.0 0.255.255.255 eq 10 0.0.0.0 0.255.255.255 gt 10\n"
                + " 7 permit udp host 1.1.1.1 gt 10 0.0.0.0 0.255.128.255 lt 10\n"
                + " 8 permit udp 0.0.0.0 0.255.0.255 lt 10 any range 10 10\n"
                + " 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023\n"
                + " 14 permit ipv4 any any ttl gt 12\n"
                + " 15 permit udp any neq 80 any ttl neq 10\n"
                + " 26 permit icmp any any router-solicitation\n"
                + "!\n";
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 2 deny ipv4 host 1.2.3.4 any
            long sequenceId = 2;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 3 permit udp 192.168.1.1/24 10.10.10.10/24
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("192.168.1.1/24"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("10.10.10.10/24"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 3;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 4 permit tcp host 1.2.3.4 eq www any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("www")
                                    .build())
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 4;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("0..10"))
                    .build());
            long sequenceId = 5;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 6 permit udp 0.0.0.0 0.255.255.255 eq 10 0.0.0.0 0.255.255.255 gt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.255.255"))
                            .build())
                    .setDestinationAddressWildcarded((new DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.255.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(new PortNumber(10)))
                            .setDestinationPort(new PortNumRange("10..65535"))
                            .build());
            long sequenceId = 6;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 7 permit udp host 1.1.1.1 gt 10 0.0.0.0 0.255.128.255 lt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setDestinationAddressWildcarded((new DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.128.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("10..65535"))
                            .setDestinationPort(new PortNumRange("0..10"))
                            .build());
            long sequenceId = 7;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 8 permit udp 0.0.0.0 0.255.0.255 lt 10 any range 10 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setDestinationAddress((new Ipv4Prefix("0.0.0.0/0")));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded((new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.0.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("0..10"))
                            .setDestinationPort(new PortNumRange("10..10"))
                            .build());
            long sequenceId = 8;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("1024..65535"))
                            .setDestinationPort(new PortNumRange("0..1023"))
                            .build());
            long sequenceId = 13;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 14 permit ipv4 any any ttl gt 12
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("13..255"))
                    .build());
            long sequenceId = 14;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 15 permit udp any neq 80 any ttl neq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("11..9"))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("81..79"))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 15;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 26 permit icmp any any router-solicitation
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            long sequenceId = 26;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null,
                    (short) 10));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findLineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4.class);
            Assert.assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    public void testFindLineWithSequenceId() {
        String lines = "a\n"
                + " 1 foo\n"
                + " 2 bar baz\n"
                + "xxx";
        Assert.assertEquals(Optional.of("1 foo"), AclEntryLineParser.findLineWithSequenceId(1L, lines));
        Assert.assertEquals(Optional.of("2 bar baz"), AclEntryLineParser.findLineWithSequenceId(2L, lines));
        Assert.assertEquals(Optional.empty(), AclEntryLineParser.findLineWithSequenceId(3L, lines));
    }

    @Test
    public void testIpv6() {
        String lines = "ipv6 access-list foo\n"
                + " 1 permit ipv6 any any\n"
                + " 3 permit icmpv6 any any router-solicitation\n"
                + " 4 deny ipv6 2001:db8:a0b:12f0::1/55 any\n"
                + " 5 permit tcp host ::1 host ::1\n"
                + " 6 permit tcp host ::1 host ::1 lt www ttl eq 10\n"
                + " 7 permit icmpv6 any host ::1 8 ttl neq 10\n"
                + " 8 permit udp f::a a::b eq 10 fe80:0000:0000:0000:0202:b3ff:fe1e:8329 "
                + "fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10\n"
                + " 9 permit udp host fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10 f::a a::b lt 10\n"
                + " 10 permit udp f::a a::b lt 10 any range 10 10\n"
                + " 11 remark foo\n"
                + " 21 remark bar\n"
                + " 31 remark baz2\n"
                + "!\n";
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 1 permit ipv6 any any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 1;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 3 permit icmpv6 any any router-solicitation
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 3;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null,
                    (short) 133));
        }
        {
            // 4 deny ipv6 2001:db8:a0b:12f0::1/55 any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv6Prefix("2001:db8:a0b:12f0::1/55"));
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 4;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 5 permit tcp host ::1 host ::1
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 5;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 6 permit tcp host ::1 host ::1 lt www ttl eq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));

            configBuilder.addAugmentation(Config4.class, new Config4Builder().setHopRange(new HopRange("10..10"))
                    .build());

            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setDestinationPortNamed("0..www")
                                    .build())
                            .build());
            long sequenceId = 6;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 7 permit icmpv6 any host ::1 8 ttl neq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            configBuilder.addAugmentation(Config4.class, new Config4Builder()
                    .setHopRange(new HopRange("11..9"))
                    .build());
            long sequenceId = 7;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null,
                    (short) 8));
        }
        {
            // 8 permit udp f::a a::b eq 10 FE80:0000:0000:0000:0202:B3FF:FE1E:8329
            // FE80:0000:0000:0000:0202:B3FF:FE1E:8329 gt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                            .ext.rev180314.src.dst.ipv6.address.wildcarded.SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build())
                    .setDestinationAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .acl.ext.rev180314.src.dst.ipv6.address.wildcarded.DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("fe80:0000:0000:0000:0202:b3ff:fe1e:8329"))
                            .setWildcardMask(new Ipv6Address("fe80:0000:0000:0000:0202:b3ff:fe1e:8329"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(new PortNumber(10)))
                            .setDestinationPort(new PortNumRange("10..65535"))
                            .build());
            long sequenceId = 8;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 9 permit udp host fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10 f::a a::b lt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("fe80:0000:0000:0000:0202:b3ff:fe1e:8329/128"));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setDestinationAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .acl.ext.rev180314.src.dst.ipv6.address.wildcarded.DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("10..65535"))
                            .setDestinationPort(new PortNumRange("0..10"))
                            .build());
            long sequenceId = 9;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 10 permit udp f::a a::b lt 10 any range 10 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setSourceAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                            .ext.rev180314.src.dst.ipv6.address.wildcarded.SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build()))
                    .build());
            configBuilder.setDestinationAddress(new Ipv6Prefix("::/0"));
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("0..10"))
                            .setDestinationPort(new PortNumRange("10..10"))
                            .build());
            long sequenceId = 10;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findLineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV6.class);
            Assert.assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

}
