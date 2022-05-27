package io.zenwave360.generator.processors;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public abstract class AbstractBaseProcessor implements Processor {

    protected String targetProperty = "api";

    public <T extends AbstractBaseProcessor> T withTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
        return (T) this;
    }

    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    protected <T> T getJsonPath(Object model, String jsonPath) {
        try {
            return JsonPath.read(model, jsonPath);
        } catch(PathNotFoundException e) {
            return null;
        }
    }

    protected void addNormalizedTagName(Map<String, Object> operation) {
        if (operation != null) {
            String normalizedTagName = null;
            List tags = (List) operation.get("tags");
            if(tags != null) {
                String tag = (String) (tags.get(0) instanceof Map? (String) ((Map) tags.get(0)).get("name") : tags.get(0));
                normalizedTagName = normalizeTagName(tag);
            }
            operation.put("x--normalizedTagName", normalizedTagName);
        }
    }

    protected String normalizeTagName(String tagName) {
        if(tagName == null) {
            return null;
        }
        String[] tokens = RegExUtils.replaceAll(tagName, "[\\s-.]", " ").split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = StringUtils.capitalize(tokens[i]);
        }
        return RegExUtils.removePattern(StringUtils.join(tokens), "^(\\d+)");
    }

    protected void addOperationIdVariants(Map<String, Object> operation) {
        if (operation != null) {
            operation.put("x--operationIdCamelCase", StringUtils.capitalize((String) operation.get("operationId")));
            operation.put("x--operationIdKebabCase", StringUtils.capitalize((String) operation.get("operationId")));
        }
    }
}