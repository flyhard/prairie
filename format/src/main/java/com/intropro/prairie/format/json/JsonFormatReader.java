/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intropro.prairie.format.json;

import com.intropro.prairie.format.AbstractFormatReader;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by presidentio on 10/7/15.
 */
public class JsonFormatReader extends AbstractFormatReader<Map<String, String>> {

    private BufferedReader bufferedReader;

    private ObjectMapper objectMapper = new ObjectMapper();
    private TypeReference<HashMap<String, Object>> typeRef
            = new TypeReference<HashMap<String, Object>>() {
    };

    JsonFormatReader(InputStream inputStream) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public Map<String, String> next() throws IOException {
        String line = bufferedReader.readLine();
        if (line == null) {
            return null;
        }
        return objectMapper.readValue(line, typeRef);
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
