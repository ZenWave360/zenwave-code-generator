package io.zenwave360.generator.plugins;

import com.jayway.jsonpath.JsonPath;
import io.zenwave360.generator.DocumentedOption;
import io.zenwave360.generator.parsers.Model;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class AbstractOpenAPIGenerator implements GeneratorPlugin {

    enum OperationType {
        GET, PUT, POST, DELETE, PATCH, HEAD, PARAMETERS
    }

    @DocumentedOption(description = "Java API package name")
    public String apiPackage = "io.example.api";
    @DocumentedOption(description = "Java Models package name")
    public String modelPackage = "io.example.api.model";

    @DocumentedOption(description = "Project role: PROVIDER\\|CLIENT")
    public RoleType role = RoleType.PROVIDER;

    @DocumentedOption(description = "Operation IDs to generate code for")
    public List<String> operationIds = new ArrayList<>();

    @DocumentedOption(description = "Status codes to generate code for (default: 200, 201, 202 and 400")
    public List<String> statusCodes = List.of("200", "201", "202", "400");

    public String getApiPackageFolder() {
        return this.apiPackage.replaceAll("\\.", "/");
    }

    public String getModelPackageFolder() {
        return this.apiPackage.replaceAll("\\.", "/");
    }

    public Map<String, List<Map<String, Object>>> getOperationsGroupedByTag(Model apiModel, OperationType... operationTypes) {
        Map<String, List<Map<String, Object>>> operationsByTag = new HashMap<>();
        List<Map<String, Object>> operations = JsonPath.read(apiModel, "$.paths[*].*");
        for (Map<String, Object> operation : operations) {
            if (matchesFilters(operation, operationTypes)) {
                String tag = (String) ObjectUtils.firstNonNull(operation.get("x--normalizedTagName"), "DefaultService");
                if (!operationsByTag.containsKey(tag)) {
                    operationsByTag.put(tag, new ArrayList<>());
                }
                operationsByTag.get(tag).add(operation);
            }
        }
        return operationsByTag;
    }

    public boolean matchesFilters(Map<String, Object> operation, OperationType... operationTypes) {
        var operationOperationType = OperationType.valueOf(operation.get("x--httpVerb").toString().toUpperCase());
        return matchedOperationTypes(operation, operationOperationType, operationTypes) && matchesOperationIds(operation, operationIds);
    }

    protected boolean matchedOperationTypes(Map<String, Object> operation, OperationType operationOperationType, OperationType... operationTypes) {
        return operationTypes == null || operationTypes.length == 0 || Arrays.stream(operationTypes).anyMatch(operationType -> operationOperationType == operationType);
    }

    protected boolean matchesOperationIds(Map<String, Object> operation, List<String> operationIds) {
        if (operationIds == null || operationIds.isEmpty()) {
            return true;
        }
        String operationId = (String) operation.get("operationId");
        return operationIds.contains(operationId);
    }
}