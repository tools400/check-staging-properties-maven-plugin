package de.codecentric;

import static org.junit.Assert.assertEquals;

/*
 * #%L
 * check-staging-properties-maven-plugin
 * %%
 * Copyright (C) 2016 codecentric AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Test;

public class TestYamlLoader extends AbstractMojoTest {

    @Test
    public void testSimpleDataTypes() throws Exception {
        File file = createTestPropertiesFile("test-DEV.yaml", "test:\n  string: \"one\"\n  hex: 0x12d4\n  octal: 012345\n  exp: 12.3015e+05\n  boolean: true\n  integer: 20\n");
        
        Properties props = new YamlLoader(file).load();
        
        assertEquals("one", props.get("test.string"));
        assertEquals("4820", props.get("test.hex"));
        assertEquals("5349", props.get("test.octal"));
        assertEquals("1230150.0", props.get("test.exp"));
        assertEquals("true", props.get("test.boolean"));
        assertEquals("20", props.get("test.integer"));
    }

    @Test
    public void testListDataTypes() throws Exception {
        File file = createTestPropertiesFile("test-DEV.yaml", "test:\n  items:\n    - 6\n    - 7\n    - 8\n");
        
        Properties props = new YamlLoader(file).load();
        
        assertEquals("6", props.get("test.items.0"));
        assertEquals("7", props.get("test.items.1"));
        assertEquals("8", props.get("test.items.2"));
    }

    @Test
    public void testEmptyProperty() throws Exception {
        
        File file = createTestPropertiesFile("test-DEV.yaml", "test:\n  items:\n    - 6\n    - 7\n    - 8\n  emptyProp:\n");
        
        Properties props = new YamlLoader(file).load();
        
        assertEquals("", props.get("test.emptyProp"));
    }
}
