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

    @SuppressWarnings("unchecked")
    public List<MetricRule> load(String rulesFileLocation) throws Exception {
        Resource res = resourceLoader.getResource(rulesFileLocation);
        try (InputStream in = res.getInputStream()) {
            Map<String, Object> root = mapper.readValue(in, Map.class);
            Object rulesObj = root.get("rules");
            String intermediate = mapper.writeValueAsString(rulesObj);
            return mapper.readValue(intermediate, mapper.getTypeFactory().constructCollectionType(List.class, MetricRule.class));
        }
    }
}
