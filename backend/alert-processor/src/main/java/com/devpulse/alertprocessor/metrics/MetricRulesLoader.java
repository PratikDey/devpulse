package com.devpulse.alertprocessor.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class MetricRulesLoader {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    public MetricRulesLoader(ResourceLoader resourceLoader) { this.resourceLoader = resourceLoader; }

    /**
     * Load metric rules from a YAML resource and convert them to a list of {@link MetricRule}.
     *
     * Expected YAML structure:
     *  rules:
     *    - id: "rule1"
     *      name: "High CPU"
     *      promql: "..."
     *      threshold: 0.9
     *      comparator: "GREATER_THAN"
     *      durationSeconds: 60
     *      severity: "CRITICAL"
     *      description: "..."
     *
     * The loader:
     *  - Resolves the resource using the Spring ResourceLoader.
     *  - Validates that the file exists and contains a top-level `rules` node.
     *  - Maps the node to a List\<MetricRule\>.
     *
     * Throws IllegalStateException when the resource is missing or the `rules` node is absent.
     *
     * @param rulesFileLocation Spring resource location (classpath:..., file:..., etc.)
     * @return list of loaded MetricRule instances
     * @throws Exception on IO or mapping errors
     */

    @SuppressWarnings("unchecked")
    public List<MetricRule> load(String rulesFileLocation) throws Exception {
        Resource res = resourceLoader.getResource(rulesFileLocation);

        if(!res.exists()) {
            throw new IllegalStateException("Rules file not found: " + rulesFileLocation);
        }
        try (InputStream in = res.getInputStream()) {
            Map<String, Object> root = mapper.readValue(in, Map.class);
            Object rulesObj = root.get("rules");

            if(rulesObj == null) {
                throw new IllegalStateException("YAML missing 'rules' root node");
            }

            String intermediate = mapper.writeValueAsString(rulesObj);
            return mapper.readValue(intermediate, mapper.getTypeFactory().constructCollectionType(List.class, MetricRule.class));
        }
    }
}
