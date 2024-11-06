package de.codecentric;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractMojoTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    protected File createTestPropertiesFile(String filename, String content) throws Exception {
        File f = new File(folder.getRoot().toString() + "/" + filename);
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write(content);
        w.close();
        return f;
    }

    protected class TestCheckStagingPropertiesMojo extends CheckStagingPropertiesMojo {
        TestCheckStagingPropertiesMojo() {
            this.directory = folder.getRoot();
            this.groups = null;
        }

        TestCheckStagingPropertiesMojo(File directory, List<String> groups) {
            this.directory = directory;
            this.groups = groups;
        }
    }
}
