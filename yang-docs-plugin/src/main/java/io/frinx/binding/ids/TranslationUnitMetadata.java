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

package io.frinx.binding.ids;

import io.frinx.cli.utils.CapturingReaderRegistryBuilder;
import io.frinx.cli.utils.CapturingWriterRegistryBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * This class holds all the information we have about unit. Its only responsibility is to hold all data and should
 * contain only simple get() method. All complex operations above data is one in TranslationUnitMetadataHandler.
 */
public class TranslationUnitMetadata {

    private final Set<InstanceIdentifier<?>> writersSet;
    private final Set<InstanceIdentifier<?>> readersSet;
    private final Class<?> classObject;
    private final DocsUnitCollector unitCollector;
    private final CapturingReaderRegistryBuilder readerRegistryBuilder;
    private final CapturingWriterRegistryBuilder writerRegistryBuilder;
    private final CodecTranslator codecTranslator;

    TranslationUnitMetadata(DocsUnitCollector unitCollector, Set<InstanceIdentifier<?>> writersSet,
                            Set<InstanceIdentifier<?>> readersSet, Class<?> classObject, SchemaContext context,
                            BindingToNormalizedNodeCodec bindingCodec,
                            CapturingReaderRegistryBuilder readerRegistryBuilder,
                            CapturingWriterRegistryBuilder writerRegistryBuilder
    ) {

        this.classObject = classObject;
        this.readersSet = readersSet;
        this.writersSet = writersSet;
        this.unitCollector = unitCollector;
        this.readerRegistryBuilder = readerRegistryBuilder;
        this.writerRegistryBuilder = writerRegistryBuilder;
        this.codecTranslator = new CodecTranslator(bindingCodec, context);

    }


    public Set<InstanceIdentifier<?>> getReadersSet() {
        return readersSet;
    }

    public Stream<String> getReadersAsStrings() {
        return getReadersSet().stream()
                .map(codecTranslator::toBindingIndependent)
                .filter(Objects::nonNull)
                .map(codecTranslator::toStringId);
    }

    public Stream<String> getWritersAsStrings() {
        return getWritersSet().stream()
                .map(codecTranslator::toBindingIndependent)
                .filter(Objects::nonNull)
                .map(codecTranslator::toStringId);
    }

    public CapturingWriterRegistryBuilder getWriterRegistryBuilder() {
        return writerRegistryBuilder;
    }

    public CapturingReaderRegistryBuilder getReaderRegistryBuilder() {
        return readerRegistryBuilder;
    }

    public Set<InstanceIdentifier<?>> getWritersSet() {
        return writersSet;
    }

    public List<String> getDevicesVersion() {
        return getUnitCollector().getDevicesId().stream()
                .map(Device::getDeviceVersion)
                .collect(Collectors.toList());
    }

    public List<String> getDeviceType() {
        return getUnitCollector().getDevicesId().stream()
                .map(Device::getDeviceType)
                .distinct()
                .map(type -> type.replace(" ", "-"))
                .collect(Collectors.toList());
    }

    public String getName() {
        return classObject.getName();
    }

    public String getSimpleName() {
        return classObject.getSimpleName();
    }

    public DocsUnitCollector getUnitCollector() {
        return unitCollector;
    }

}