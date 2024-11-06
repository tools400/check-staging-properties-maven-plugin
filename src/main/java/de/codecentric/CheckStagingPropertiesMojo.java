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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY)
class CheckStagingPropertiesMojo extends AbstractMojo {

    @Parameter(defaultValue = "src/main/resources")
    File directory;

    @Parameter
    List<String> groups;

    private static final String DEFAULT_GROUP = ".*";

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isGroupingEnabled()) {
            for (String group : groups) {
                doChecks(group, getProperties(group));
            }
        } else {
            doChecks(DEFAULT_GROUP, getProperties(DEFAULT_GROUP));
        }
    }

    private ArrayList<FileProperties> getPropertiesRecursively(File directory, String pattern)
            throws MojoExecutionException {
        ArrayList<FileProperties> propertyFiles = new ArrayList<>(20);
        if (directory == null || !directory.exists()) {
            getLog().warn("Directory `" +
                    (directory == null ? "" : directory.getAbsolutePath()) +
                    "` does not exist. Skipping.");
            return propertyFiles;
        }

        final File[] files = directory.listFiles();
        if (files == null) {
            getLog().warn("Directory `" +
                    directory.getAbsolutePath() +
                    "` does not denote a directory. Skipping.");
            return propertyFiles;
        }

        String fileType = null;

        for (File file : files) {
            if (file.isDirectory()) {
                propertyFiles.addAll(getPropertiesRecursively(file, pattern));
                continue;
            }
            if (!(isPropertiesFile(file) || isYamlFile(file)) || !matchesGroupPattern(pattern, file)) {
                continue;
            }

            if (fileType == null) {
                if (isPropertiesFile(file)) {
                    fileType = Files.PROPERTIES;
                } else if (isYamlFile(file)) {
                    fileType = Files.YAML;
                } else {
                    throw new MojoExecutionException("Unsupported file type `" + file.getName() + "`");
                }
            } else {
                if (Files.PROPERTIES.equals(fileType)) {
                    if (!isPropertiesFile(file)) {
                        throw new MojoExecutionException("Unexpected file type `" + file.getName() + "`");
                    }
                } else if (Files.YAML.equals(fileType)) {
                    if (!isYamlFile(file)) {
                        throw new MojoExecutionException("Unexpected file type `" + file.getName() + "`");
                    }
                }
            }

            FileProperties props = new FileProperties(file);
            try {
                if (isPropertiesFile(file)) {
                    props.load();
                } else if (isYamlFile(file)) {
                    props = new YamlLoader(file).load();
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot read file `" + file.getName() + "`", e);
            }
            propertyFiles.add(props);
        }
        return propertyFiles;
    }

    ArrayList<FileProperties> getProperties() throws MojoExecutionException {
        return getPropertiesRecursively(directory, DEFAULT_GROUP);
    }

    ArrayList<FileProperties> getProperties(String pattern) throws MojoExecutionException {
        return getPropertiesRecursively(directory, pattern);
    }

    private void doChecks(String group, ArrayList<FileProperties> props)
            throws MojoExecutionException, MojoFailureException {
        if (props.size() > 1) {
            if (!StagingProperties.sizesEqual(props)) {
                throw new MojoFailureException("In group `" + group + "`: Sizes (number of keys) are not equal");
            }

            if (!StagingProperties.keysEqual(props)) {
                throw new MojoFailureException("In group `" + group + "`: Keys are not equal");
            }

            if (!StagingProperties.valuesPresent(props)) {
                List<String> errors = new LinkedList<>();
                for (int i = 0; i < props.size(); i++) {
                    String missingValues = "";
                    for (Object key : props.get(i).keySet()) {
                        String value = (String) props.get(i).get(key);
                        if (value == null || "".equals(value)) {
                            missingValues += key + "\n";
                        }
                    }
                    errors.add("file: " + props.get(i).getFileName() + ", keys: \n" + missingValues);
                }
                if (DEFAULT_GROUP.equals(group)) {
                    throw new MojoFailureException("There are some empty values in:\n`" + errors + "`");
                } else {
                    throw new MojoFailureException("There are some empty values in group `" + group + "` and\n`" + errors + "`");
                }
            }
        }
    }

    private boolean isGroupingEnabled() {
        return groups != null && groups.size() > 0;
    }

    private boolean matchesGroupPattern(String pattern, File file) {
        return Files.matchesGroup(file, pattern);
    }

    private boolean isPropertiesFile(File file) {
        return Files.isPropertiesFile(file);
    }

    private boolean isYamlFile(File file) {
        return Files.isYamlFile(file);
    }
}
