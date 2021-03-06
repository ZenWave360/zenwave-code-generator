# SpringWebTestClientConfiguration

Generates spring WebTestClient tests from OpenAPI defined endpoints.

${javadoc}

## Options

| **Option**               | **Description**                                                              | **Type**    | **Default**                  | **Values**                  |
| ------------------------ | ---------------------------------------------------------------------------- | ----------- | ---------------------------- | --------------------------- |
| `specFile`               | API Specification File                                                       | String      |                              |                             |
| `targetFolder`           | Target folder to generate code to. If left empty, it will print to stdout.   | File        |                              |                             |
| `controllersPackage`     | The package to generate REST Controllers                                     | String      | {{basePackage}}.adapters.web |                             |
| `groupBy`                | Generate test classes grouped by                                             | GroupByType | SERVICE                      | SERVICE, OPERATION, PARTIAL |
| `testSuffix`             | Class name suffix for generated test classes                                 | String      | IT                           |                             |
| `basePackage`            | Applications base package                                                    | String      |                              |                             |
| `openApiApiPackage`      | The package to used by OpenAPI-Generator for generated api objects/classes   | String      |                              |                             |
| `openApiModelPackage`    | The package to used by OpenAPI-Generator for generated model objects/classes | String      | {{openApiApiPackage}}        |                             |
| `openApiModelNamePrefix` | Sets the prefix for model enums and classes used by OpenAPI-Generator        | String      |                              |                             |
| `openApiModelNameSuffix` | Sets the suffix for model enums and classes used by OpenAPI-Generator        | String      |                              |                             |
| `operationIds`           | OpenAPI operationIds to generate code for                                    | List        | []                           |                             |
| `statusCodes`            | Status codes to generate code for (default: 200, 201, 202 and 400            | List        | [200, 201, 202, 400]         |                             |

## Getting Help

```shell
jbang zw -p io.zenwave360.generator.plugins.SpringWebTestClientConfiguration --help
```
