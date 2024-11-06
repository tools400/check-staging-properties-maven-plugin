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

import java.io.File;

final class Files {

    public static final String PROPERTIES = "properties";
    public static final String YAML = "yaml";
    public static final String YML = "yml";
    
    static String getExtension(File f) {
        int i = f.getName().lastIndexOf('.');
        return (i > 0 ? f.getName().substring(i + 1) : null);
    }

    static boolean isPropertiesFile(File f) {
        String extension = getExtension(f);
        return extension != null && extension.equals(PROPERTIES);
    }

    static boolean isYamlFile(File f) {
        String extension = getExtension(f);
        return extension != null && (extension.equals(YAML) || extension.equals(YML));
    }

    static boolean matchesGroup(File f, String groupPattern) {
        return groupPattern == null || f.getName().matches(groupPattern);
    }

}
