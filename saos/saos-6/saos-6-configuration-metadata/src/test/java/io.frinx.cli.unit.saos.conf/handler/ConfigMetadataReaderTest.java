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

package io.frinx.cli.unit.saos.conf.handler;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMetadataReaderTest {
    private static final String OUTPUT = "diff -b startup-config /tmp/20221.out\n"
            + "4c4\n"
            + "< ! Created:       Fri Jul 28 18:31:43 2000\n"
            + "---\n"
            + "> ! Created:       Fri Jul 28 18:34:08 2000\n";

    private static final String EXCEPTED_DATE = "Fri Jul 28 18:31:43 2000";
    private static final String WRONG_DATE = "Fri Jul 28 18:34:08 2000";

    @Test
    public void testParse() {
        Optional<String> date = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        Assert.assertEquals(date.get(), EXCEPTED_DATE);

        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        Assert.assertNotEquals(date1.get(), WRONG_DATE);

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        Assert.assertFalse(date2.isPresent());
    }
}
