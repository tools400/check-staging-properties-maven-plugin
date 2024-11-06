package de.codecentric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

import org.yaml.snakeyaml.Yaml;

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

public class YamlLoader {

    private File file;

    public YamlLoader(File file) {
        this.file = file;
    }

    public FileProperties load() throws FileNotFoundException {
        FileProperties props = new FileProperties(file);
        Yaml yaml = new Yaml();
        Object yamlObj = yaml.load(new FileInputStream(file.getAbsolutePath()));
        appendMap(props, (HashMap) yamlObj, null);
        return props;
    }

    private void appendMap(FileProperties props, HashMap map, String parentKey) {
        if (map.isEmpty()) {
            return;
        }
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = map.get(key);
            if (value instanceof HashMap) {
                appendMap(props, (HashMap) value, (String) key);
            } else {
                key = produceKey(parentKey, key);
                appendProperty(props, key, value);
            }
        }
    }

    private void appendProperty(FileProperties props, String key, Object value) {
        if (value == null) {
            value = "";
        }
        if (value instanceof Integer) {
            props.put(key, ((Integer) value).toString());
        } else if (value instanceof Boolean) {
            props.put(key, ((Boolean) value).toString());
        } else if (value instanceof Double) {
            props.put(key, ((Double) value).toString());
        } else if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            for (int i = 0; i < list.size(); i++) {
                appendProperty(props, produceKey(key, Integer.toString(i)), list.get(i));
            }
        } else {
            props.put(key, value);
        }
    }

    private String produceKey(String parentKey, String key) {
        if (parentKey != null) {
            key = parentKey + "." + key;
        }
        return key;
    }
}
