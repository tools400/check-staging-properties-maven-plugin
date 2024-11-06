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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CheckStagingPropertiesMojoTest extends AbstractMojoTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldContainEmptyList() throws MojoExecutionException {
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();
        ArrayList<? extends Properties> properties = mojo.getProperties();
        assertEquals(0, properties.size());
    }

    @Test
    public void shouldNotUseNonPropertiesFiles() throws IOException, MojoExecutionException {
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();

        folder.newFolder("child");
        folder.newFile("app-DEV.properties");
        folder.newFile("app-PRD.properties");
        folder.newFile(".DS_Store");
        folder.newFile("test.pdf");

        assertEquals(2, mojo.getProperties().size());
    }

    @Test
    public void testGetPropertiesRecursively() throws Exception {
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();

        folder.newFile("app-DEV.properties");
        folder.newFile("app-PRD.properties");
        folder.newFolder("child");
        folder.newFile("child/app-STG.properties");

        assertEquals(3, mojo.getProperties().size());
    }

    @Test
    public void shouldContainEmptyPropertiesListWhenInputFileDoesNotExist() throws MojoExecutionException {
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(new File("/does/not/exist"),null);
        assertEquals(0, mojo.getProperties().size());
    }

    @Test
    public void shouldExecutePluginWhenDirectoryEntryIsNotPreconfigured() throws Exception {
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();
        mojo.execute(); // should work
    }

    @Test
    public void shouldBreakBuildWhenPropertiesSizesAreNotEqual() throws Exception {
        createTestPropertiesFile("app-DEV.properties", "test.one =");
        createTestPropertiesFile("app-PRD.properties", "test.one =\ntest.two =");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();
        exception.expect(MojoFailureException.class);
        exception.expectMessage("Sizes (number of keys) are not equal");

        mojo.execute();
    }

    @Test
    public void shouldBreakBuildWhenPropertiesKeysAreNotEqual() throws Exception {
        createTestPropertiesFile("app-DEV.properties", "test.one =\ntest.three =");
        createTestPropertiesFile("app-PRD.properties", "test.one =\ntest.two =");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();
        exception.expect(MojoFailureException.class);
        exception.expectMessage("Keys are not equal");

        mojo.execute();
    }

    @Test
    public void shouldBreakBuildWhenPropertiesValuesAreEmptyNoGroups() throws Exception {
        createTestPropertiesFile("app-DEV.properties", "test.one = one\ntest.two =");
        createTestPropertiesFile("app-PRD.properties", "test.one =\ntest.two =");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo();
        exception.expect(MojoFailureException.class);
        final String exceptionMessage = "There are some empty values in:\n`[file: app-DEV.properties, keys: \n" +
                "test.two\n" +
                ", file: app-PRD.properties, keys: \n" +
                "test.two\n" +
                "test.one\n" +
                "]`";
        exception.expectMessage(exceptionMessage);
        mojo.execute();
    }

    @Test
    public void shouldBreakBuildWhenPropertiesValuesAreEmptyGroups() throws Exception {
        createTestPropertiesFile("app-DEV.properties", "test.one = one\ntest.two =");
        createTestPropertiesFile("app-PRD.properties", "test.one =\ntest.two =");
        createTestPropertiesFile("bla-DEV.properties", "bla.bla=bla");
        createTestPropertiesFile("bla-PRD.properties", "bla.bla=bla");

        ArrayList<String> groups = new ArrayList<>();
        groups.add("bla-.*");
        groups.add("app-.*");
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(folder.getRoot(), groups);

        exception.expect(MojoFailureException.class);
        final String exceptionMessage = "There are some empty values in group `app-.*` and\n`[file: app-DEV.properties, keys: \n" +
                "test.two\n" +
                ", file: app-PRD.properties, keys: \n" +
                "test.two\n" +
                "test.one\n" +
                "]`";
        exception.expectMessage(exceptionMessage);
        mojo.execute();
    }

    @Test
    public void groupPatternMatchingOfFilenamesProperties() throws Exception {
        createTestPropertiesFile("test-DEV.properties", "test.one=one\ntest.two=two");
        createTestPropertiesFile("test-PRD.properties", "test.one=one\ntest.two=two");
        createTestPropertiesFile("bla-DEV.properties", "bla.bla=bla");
        createTestPropertiesFile("bla-PRD.properties", "bla.bla=bla");

        ArrayList<String> groups = new ArrayList<>();
        groups.add("test-.*");
        groups.add("bla-.*");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(folder.getRoot(), groups);
        mojo.execute();
    }

    @Test
    public void groupPatternMatchingOfFilenamesYaml() throws Exception {
        createTestPropertiesFile("test-DEV.yaml", "test:\n  one: \"one\"\n  two: \"two\"\n  boolean: true\n  integer: 20");
        createTestPropertiesFile("test-PRD.yaml", "test:\n  one: \"one\"\n  two: \"two\"\n  boolean: false\n  integer: 30");
        createTestPropertiesFile("bla-DEV.yaml", "bla:\n  bla: \"bla\"");
        createTestPropertiesFile("bla-PRD.yaml", "bla:\n  bla: \"bla\"");

        ArrayList<String> groups = new ArrayList<>();
        groups.add("test-.*\\.yaml");
        groups.add("bla-.*\\.yaml");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(folder.getRoot(), groups);
        mojo.execute();
    }

    //@Test
    public void groupPatternMatchingMixedFileTypes() throws Exception {
        createTestPropertiesFile("test-DEV.properties", "test.one=one\ntest.two=two\ntest.three=three");
        createTestPropertiesFile("test-PRD.yaml", "test:\n  one: \"one\"\n  two: \"two\"");

        ArrayList<String> groups = new ArrayList<>();
        groups.add("test-.*");

        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(folder.getRoot(), groups);

        exception.expect(MojoExecutionException.class);
        exception.expectMessage("Unexpected file type `test-PRD.yaml`");

        mojo.execute();
    }

    @Test
    public void unreadableFile() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName != null && osName.startsWith("windows")) {
            return;
        }
        File f = createTestPropertiesFile("test-DEV.properties", "test.one=one\ntest.two=two");
        boolean successful = f.setReadable(false); // Does not work on Windows.
        assertTrue(successful);
        ArrayList<String> groups = new ArrayList<>();
        groups.add("test-.*");
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(folder.getRoot(), groups);
        exception.expect(MojoExecutionException.class);
        mojo.execute();
    }

    @Test
    public void fileAsDirectory() throws Exception {
        File f = createTestPropertiesFile("test.properties", "");
        TestCheckStagingPropertiesMojo mojo = new TestCheckStagingPropertiesMojo(f, new ArrayList<String>());
        mojo.getProperties();

    }

}
