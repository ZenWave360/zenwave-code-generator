package io.zenwave360.generator.parsers;

import io.zenwave360.jsonrefparser.$RefParser;
import io.zenwave360.jsonrefparser.$RefParserOptions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultYamlParser implements io.zenwave360.generator.parsers.Parser {

    public String specFile;
    public String targetProperty = "api";

    public DefaultYamlParser withSpecFile(String specFile) {
        this.specFile = specFile;
        return this;
    }

    public DefaultYamlParser withTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
        return this;
    }

    protected File findSpecFile(String specFile) {
        if(specFile.startsWith("classpath:")) {
            try {
                return new File(getClass().getClassLoader().getResource(specFile.replaceFirst("classpath:", "")).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(specFile);
    }

    @Override
    public Map<String, Model> parse() throws IOException {
        File file = findSpecFile(specFile);
        $RefParser parser = new $RefParser(file).withOptions(new $RefParserOptions($RefParserOptions.OnCircular.SKIP));
        Map model = new LinkedHashMap<>();
        model.put(targetProperty, new Model(file, parser.dereference().getRefs()));
        return model;
    }
}