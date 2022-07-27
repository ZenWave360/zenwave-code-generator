# ZenWave Code Generator

> :warning: Work in progress and not ready for use.

ZenWave Code Generator is a configurable and extensible code generator tool for **Domain Driven Design (DDD)** and **API-First** that can generate code from a mix of different models including:

- [JHipster Domain Language (JDL)](https://www.jhipster.tech/jdl/intro)
- [AsyncAPI](https://www.asyncapi.com/docs/getting-started/coming-from-openapi)
- [OpenAPI](https://swagger.io/specification/)

Using JHipster Domain Language as **Ubiquitous Language** for [Data on the Inside](https://blog.acolyer.org/2016/09/13/data-on-the-outside-versus-data-on-the-inside/) and **API-First** specs like **AsyncAPI** and **OpenAPI** to describe Inter Process Communications (IPC) for [Data on the Outside](https://blog.acolyer.org/2016/09/13/data-on-the-outside-versus-data-on-the-inside/).

![ZenWave Modeling Languages](docs/00-ZenWave-ModelingLanguages.excalidraw.svg)

- **JHipster Domain Language (JDL) as Ubiquitous Language:** To describe your domain core domain model
- **API-First specs like AsyncAPI and OpenAPI:** to describe Inter Process Communications (IPC) between bounded contexts/microservices.
- **ZenWave Code Generator:** to generate (_a lot of_) infrastructure, functional and testing code from your models and APIs.

**Note:** Official plugins are designed to generate functional code and tests on top of existing projects. Creating a base project is out of scope, but you can always go to [start.spring.io](http://start.spring.io) or [start.jhipster.tech](https://start.jhipster.tech/), in case your company doesn't already have a project starter or archetype.

# Table of Contents

- [ZenWave Code Generator](#zenwave-code-generator)
- [Table of Contents](#table-of-contents)
- [An "Online Food Delivery Service"](#an-online-food-delivery-service)
- [Designing a system from scratch with DDD and API-First](#designing-a-system-from-scratch-with-ddd-and-api-first)
  - [DDD: From Idea to JDL](#ddd-from-idea-to-jdl)
  - [API-First: Designing Inter-Service Communication](#api-first-designing-inter-service-communication)
    - [Access data owned by other bounded contexts: Direct Access vs CQRS](#access-data-owned-by-other-bounded-contexts-direct-access-vs-cqrs)
    - [Sagas](#sagas)
- [Refactoring a legacy monolith](#refactoring-a-legacy-monolith)
  - [Reverse engineering JDL from Java classes (JPA and MongoDB)](#reverse-engineering-jdl-from-java-classes-jpa-and-mongodb)
- [Adding functionality on top of an existent microservices archytecture](#adding-functionality-on-top-of-an-existent-microservices-archytecture)
  - [Reverse engineering JDL from Java classes (JPA and MongoDB)](#reverse-engineering-jdl-from-java-classes-jpa-and-mongodb-1)
  - [Reverse engineering JDL from OpenAPI definition schemas](#reverse-engineering-jdl-from-openapi-definition-schemas)
- [Generating functional and testing code: What can we generate for you today?](#generating-functional-and-testing-code-what-can-we-generate-for-you-today)
  - [JDL Server Entities (WIP)](#jdl-server-entities-wip)
  - [SpringData Repositories](#springdata-repositories)
  - [SpringData Repositories InMemory Mocks](#springdata-repositories-inmemory-mocks)
  - [OpenAPI Clients (using official OpenAPI generator)](#openapi-clients-using-official-openapi-generator)
  - [High Fidelity Stateful REST API Mocks (using sister project ZenWave ApiMock)](#high-fidelity-stateful-rest-api-mocks-using-sister-project-zenwave-apimock)
  - [AsyncAPI strongly typed interfaces and SpringCloudStreams3 implementations](#asyncapi-strongly-typed-interfaces-and-springcloudstreams3-implementations)
  - [AsyncAPI interfaces Mocks and Contract Tests (ToBeDefined)](#asyncapi-interfaces-mocks-and-contract-tests-tobedefined)
  - [SpringMVC and WebFlux Controller Stubs along with MapStruct Mappers from OpenAPI + JDL](#springmvc-and-webflux-controller-stubs-along-with-mapstruct-mappers-from-openapi--jdl)
  - [SpringMVC and WebFlux WebTestClient integration/unit tests from OpenAPI definitions](#springmvc-and-webflux-webtestclient-integrationunit-tests-from-openapi-definitions)
  - [KarateDSL Ent-to-End tests for REST APIs (using sister project ZenWave KarateIDE)](#karatedsl-ent-to-end-tests-for-rest-apis-using-sister-project-zenwave-karateide)

# An "Online Food Delivery Service"

We are going to use an hypothetical **Online Food Delivery Service** system as an example to showcase how you can model and design a complex distributed system using JDL, OpenAPI and AsyncAPI...

Whether you are:

- [Designing a system from scratch](#designing-a-system-from-scratch-with-ddd-and-api-first),
- [Refactoring a legacy monolith](#refactoring-a-legacy-monolith) or just
- [Adding functionality on top of an existent microservices architecture](#adding-functionality-on-top-of-an-existent-microservices-archytecture)

...ZenWave Code Generator can... [generate a lot of code for you](#generating-functional-and-testing-code-what-can-we-generate-for-you-today)

# Designing a system from scratch with DDD and API-First

## DDD: From Idea to JDL

- **Domain Map:** Sketch your full domain model

![01-DomainMap](docs/01-DomainMap.excalidraw.svg)

- **Domain Subdomains:** Decompose your model into manageable subdomains

![02-DomainSubdomains](docs/02-DomainSubdomains.excalidraw.svg)

- **Domain Bounded Contexts:** Separate your subdomains as separated bounded contexts. Entities from different bounded context can only be linked by _id_ but you can implement _query views_ and caches using patterns like CQRS to synchronize data from different BCs.

![03-DomainBoundedContexts](docs/03-DomainBoundedContexts.excalidraw.svg)

- **Define Aggregates and Entities in your Bounded Contexts:** Now you can describe your aggregate roots and their composing entities into separate JDL files (click to expand to see file contents):

<details markdown="1">
  <summary>Orders Bounded Context.jdl</summary>

```
//==========================================================
// Orders BC
//==========================================================

/**
 * The Order entity.
 */
 @AggregateRoot
entity Order {
    state OrderState /** state */
    customerId String /** customerId */
    // orderLines OrderLineItem /** orderLines */
    // paymentInfo OrderPaymentInfo
    // deliveryInfo OrderDeliveryInfo
}

enum OrderState {
    CREATED, CONFIRMED, CANCELLED
}

entity OrderLineItem {
    menuItemId Integer
    quantity Integer
}

/**
 * The OrderPaymentInfo entity.
 */
entity OrderPaymentInfo {
    creditCardId String
}

/**
 * The OrderDeliveryInfo entity.
 */
entity OrderDeliveryInfo {
    addressId String
}

relationship OneToMany {
    Order to OrderLineItem
}

relationship OneToOne {
	Order to OrderPaymentInfo
    Order to OrderDeliveryInfo
}
```

</details>
 
<details markdown="1">
  <summary>Restaurants Bounded Context.jdl</summary>

```
//==========================================================
// Restaurants BC
//==========================================================

/**
 * The Restaurant entity.
 */
@AggregateRoot
entity Restaurant {
    name String
}

entity MenuItem {
    name String
    price Integer
}

entity RestaurantAddress {
    address String
}

entity RestaurantOrder {
	orderId String
    status RestaurantOrderStatus
}

enum RestaurantOrderStatus {
    ACCEPTED, READY, DELIVERED
}

relationship OneToMany {
	Restaurant to MenuItem
}

relationship OneToOne {
	Restaurant to RestaurantAddress
}

relationship ManyToOne {
	Restaurant to RestaurantOrder
}
```

</details>

<details markdown="1">
  <summary>Delivery Bounded Context.jdl</summary>

```
//==========================================================
// Delivery BC
//==========================================================
entity DeliveryOrder {
    orderId String
    status DeliveryOrderStatus
}

enum DeliveryOrderStatus {
    ACCEPTED, ONTRANSIT, DELIVERED
}
```

</details>

<details  markdown="1">
  <summary>Customers Bounded Context.jdl</summary>

```
//==========================================================
// Customers BC
//==========================================================

/**
 * The Customer entity.
 */
@AggregateRoot
entity Customer {
    fullName String /** fullName */
}

entity CustomerAddress {}

entity CreditCard {
    cardNumber String
}

relationship OneToMany {
	Customer to CustomerAddress
    Customer to CreditCard
}
```

</details>

![04-DDD-Agreggates-BoundedContexts-Orders_JDL.png](docs/04-DDD-Agreggates-BoundedContexts-Orders_JDL.png)

![04-DDD-Agreggates-BoundedContexts](docs/04-DDD-Agreggates-BoundedContexts.excalidraw.svg)

## API-First: Designing Inter-Service Communication

When you separate your domain model into subdomains and bounded context, bounded contexts become a natural boundary to split your system into separate services and microservices you need to define a way to:

- Access data from other bounded contexts
- Coordinate inter process communications

We will use **OpenAPI**, **AsyncAPI** and other specs to define the communication between these services.

### Access data owned by other bounded contexts: Direct Access, Event Sourcing and CQRS 

- **Direct Access:** _TODO_ Link to JDL-2-OpenAPI generator
- **Event Sourcing and CQRS:** _TODO_ Templates for Transactional Outbox pattern with AsyncAPI for Event Sourcing and CQRS

### Sagas

> _TODO_ Code generation for Sagas based on AsyncAPI 3 proposal for channels menu and request-response (see expandable examples below)

Currently, you can use AsyncAPI 2 specification to describe message schemas and the channels they are written to but is not enough to describe the inter process communication between services like SAGAs but [ongoing work for version 3](https://github.com/asyncapi/spec/issues/618) is very promising regarding documenting IPCs like SAGAs and CQRS.

With new upcoming version of AsyncAPI 3, you can separate how you describe on separate files:

- Channels, messages and servers
- Applications connected to those channels

<details markdown="1">
  <summary>Food Delivery Service Order's Saga Asyncapi.yml example</summary>

```yaml
asyncapi: 3.0.0

info:
  title: Food Delivery Service Order's Saga
  version: 0.1.0

components:
  servers:

  channels:
    ordersSagaCommonChannel:
      address: orders/saga
      message:
        oneOf:
          - $ref: "#/components/messages/onOrderCreated"
          - $ref: "#/components/messages/onOrderAcceptedAtRestaurant"
          - $ref: "#/components/messages/onOrderReadyForPickup"
          - $ref: "#/components/messages/onOrderAcceptedAtDelivery"
          - $ref: "#/components/messages/onOrderPickedUp"
          - $ref: "#/components/messages/onOrderDeliveryStatusUpdated"
          - $ref: "#/components/messages/onOrderDelivered"
```

</details>

<details markdown="1">
  <summary>Applications connected to those channels: Restaurant Service Asyncapi.yml example</summary>

```yaml
asyncapi: 3.0.0

info:
  title: Restaurant Service
  version: 1.0.0

servers:
  kafka:
    $ref: "orders.saga.asyncapi.yaml#/components/servers/kafka"

channels:
  ordersSagaCommonChannel:
    $ref: "orders.saga.asyncapi.yaml#/components/channels/ordersSagaCommonChannel"

# Notice how each operation specifies/overrides which message/s is interested in
operations:
  onOrderCreated:
    description: Join the orders saga.
    action: receive
    channel: ordersSagaCommonChannel
    message:
      $ref: "orders.saga.asyncapi.yaml#/components/messages/onOrderCreated"
  onOrderAcceptedAtRestaurant:
    description: Restaurant informs is committed to prepare the order.
    action: send
    channel: ordersSagaCommonChannel
    message:
      $ref: "orders.saga.asyncapi.yaml#/components/messages/onOrderAcceptedAtRestaurant"
  onOrderReadyForPickup:
    description: Restaurant informs order is ready to pick up.
    action: send
    channel: ordersSagaCommonChannel
    message:
      $ref: "orders.saga.asyncapi.yaml#/components/messages/onOrderReadyForPickup"
```

</details>

![05-InterProcessComunication-API-First](docs/05-InterProcessComunication-API-First.excalidraw.svg)

# Refactoring a legacy monolith

## Reverse engineering JDL from Java classes (JPA and MongoDB)

If starting with legacy project, you can reverse engineer JDL from Java entity classes. JPA and MongoDB are supported.

It requires access to your project classpath, so you can just paste the following code on any test class or main method:

```java
String jdl = new JavaToJDLGenerator()
    .withPackageName("io.zenwave360.generator.jpa2jdl")
    .withPersistenceType(JavaToJDLGenerator.PersistenceType.JPA)
    .generate();
System.out.println(jdl);
```

```java
String jdl = new JavaToJDLGenerator()
    .withPackageName("io.zenwave360.generator.mongodb2jdl")
    .withPersistenceType(JavaToJDLGenerator.PersistenceType.MONGODB)
    .generate();
System.out.println(jdl);
```

# Adding functionality on top of an existent microservices architecture

## Reverse engineering JDL from Java classes (JPA and MongoDB)

When your domain java code evolves you may want to regenerate entities back from java code, see: [Reverse engineering JDL from Java classes (JPA and MongoDB)](#reverse-engineering-jdl-from-java-classes-jpa-and-mongodb)

## Reverse engineering JDL from OpenAPI definition schemas

Reverse engineer JDL entities from OpenAPI schemas:

```shell
jbang zw -p io.zenwave360.generator.plugins.OpenAPIToJDLConfiguration \
    specFile=openapi.yml targetFolder=target/out targetFile=entities.jdl
cat target/out/entities.jdl
```

# Generating functional and testing code: What can we generate for you today?

Aims to generate a complete Architecture based on Domain models expressed in JDL.

![06-ServiceImplementation-Hexagonal](docs/06-ServiceImplementation-Hexagonal.excalidraw.svg)

## JDL Backend Application

The following instructions were executed on top of a standard https://start.spring.io generated application for MongoDB, ElasticSearch and Mapstruct with the following JDL model as only input:

<details markdown="1">
  <summary>orders-model.jdl (expand to see)</summary>

```jdl
/* Customers */
@search(elasticsearch)
entity Customer {
	username String required minlength(3) maxlength(250),
	password String required minlength(3) maxlength(250),
	email String required minlength(3) maxlength(250),
	firstName String required minlength(3) maxlength(250),
	lastName String required minlength(3) maxlength(250)
}


/* Orders */
enum OrderStatus { CONFIRMED, SHIPPED, DELIVERED }

entity CustomerOrder { // Order is a reserved word
	date Instant,
	status OrderStatus
	customer Customer
	orderedItems OrderedItem[]
	paymentDetails PaymentDetails[]
	shippingDetails ShippingDetails
}

@embedded
entity OrderedItem {
	catalogItemId Long,
	name String required minlength(3) maxlength(250),
	price BigDecimal required,
	quantity Integer
}
entity PaymentDetails {
	creditCardNumber String
}

entity ShippingDetails {
	address String
}

service Customer with CustomerUseCases
service CustomerOrder,PaymentDetails,ShippingDetails with CustomerOrderUseCases
```
</details>

```shell
jbang zw -p io.zenwave360.generator.plugins.JDLBackendApplicationDefaultConfiguration \
    specFile=src/main/resources/model/orders-model.jdl \
    basePackage=io.zenwave360.example \
    persistence=mongodb \
    style=imperative \
    targetFolder=.
```

<details markdown="1">
  <summary>generator logs (expand to see)</summary>

```shell
// Domain models annotated for SpringData MondoDB
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/Customer.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/CustomerOrder.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/OrderedItem.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/OrderStatus.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/PaymentDetails.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/domain/ShippingDetails.java

// Inbound Interfaces
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/CustomerOrderUseCases.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/CustomerUseCases.java
// Inbound DTOS
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/CustomerCriteria.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/CustomerInput.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/CustomerOrderInput.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/OrderedItemInput.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/PaymentDetailsInput.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/inbound/dtos/ShippingDetailsInput.java

// Outbound Interfaces for MongoDB
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/mongodb/CustomerOrderRepository.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/mongodb/CustomerRepository.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/mongodb/OrderedItemRepository.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/mongodb/PaymentDetailsRepository.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/mongodb/ShippingDetailsRepository.java

// Outbound Interfaces and annotated DTO for ElasticSearch
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/search/CustomerDocument.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/outbound/search/CustomerSearchRepository.java

// Services/UseCases Implementation
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/CustomerOrderUseCasesImpl.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/CustomerUseCasesImpl.java

// Mapstruct Mappers used by Services/UseCases
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/mappers/CustomerMapper.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/mappers/CustomerOrderMapper.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/mappers/OrderedItemMapper.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/mappers/PaymentDetailsMapper.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/core/implementation/mappers/ShippingDetailsMapper.java

// CRUD SpringMVC REST Controllers
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/CustomerOrderResource.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/CustomerResource.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/PaymentDetailsResource.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/ShippingDetailsResource.java

```
</details>

#### JDL To OpenAPI

Generate a baseline OpenAPI definition from JDL entities:

- Component Schemas for entities, plain and paginated lists
- CRUD operations for entities

```shell
jbang zw -p io.zenwave360.generator.plugins.JDLToOpenAPIConfiguration \
    specFile=src/main/resources/model/orders-model.jdl \
    targetFile=src/main/resources/model/openapi.yml
```


#### SpringMVC Controllers from OpenAPI

Delete generated CRUD Controllers and generate SpringMVC Controller interfaces with the official OpenAPI Generator:

```shell
rm -rf src/main/java/io/zenwave360/example/adapters/web
mvn clean generate-sources
```

Generate new SpringMVC controllers from controllers interfaces generated by the OpenAPI Generator:

```shell

```shell
jbang zw -p io.zenwave360.generator.plugins.JDLOpenAPIControllersConfiguration \
    specFile=src/main/resources/model/openapi.yml \
    jdlFile=src/main/resources/model/orders-model.jdl \
    basePackage=io.zenwave360.example \
    openApiApiPackage=io.zenwave360.example.adapters.web \
    openApiModelPackage=io.zenwave360.example.adapters.web.model \
    openApiModelNameSuffix=DTO \
    targetFolder=.
```

<details markdown="1">
  <summary>generator logs (expand to see)</summary>

```shell
// REST Controllers implementing OpenAPI generated interfaces
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/CustomerApiController.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/CustomerOrderApiController.java
// Mapstruct Mappers used by REST Controllers
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/mappers/CustomerDTOsMapper.java
DEBUG TemplateFileWriter - Writing template with targetFile: src/main/java/io/zenwave360/example/adapters/web/mappers/CustomerOrderDTOsMapper.java
```
</details>

## SpringMVC and WebFlux WebTestClient integration/unit tests from OpenAPI (and JDL) definitions

Generates test for SpringMVC or Spring WebFlux using WebTestClient based on OpenAPI specification.

```shell
jbang zw -p io.zenwave360.generator.plugins.SpringWebTestClientConfiguration \
    specFile=src/main/resources/model/openapi.yml \
    jdlFile=src/main/resources/model/orders-model.jdl \
    targetFolder=src/test/java \
    controllersPackage=io.zenwave360.example.adapters.web \
    openApiApiPackage=io.zenwave360.example.adapters.web \
    openApiModelPackage=io.zenwave360.example.adapters.web.model \
    openApiModelNameSuffix=DTO \
    groupBy=SERVICE
```

<details markdown="1">
  <summary>generator logs (expand to see)</summary>

```shell
DEBUG TemplateFileWriter - Writing template with targetFile: io/zenwave360/example/adapters/web/ControllersTestSet.java
DEBUG TemplateFileWriter - Writing template with targetFile: io/zenwave360/example/adapters/web/CustomerApiControllerIT.java
DEBUG TemplateFileWriter - Writing template with targetFile: io/zenwave360/example/adapters/web/CustomerOrderApiControllerIT.java
```
</details>

## High Fidelity Stateful REST API Mocks (using sister project ZenWave ApiMock)

See sister project [ZenWave ApiMock](https://github.com/ZenWave360/zenwave-apimock)

See also medium article: [High Fidelity Stateful Mocks (Consumer Contracts) with OpenAPI and KarateDSL @medium](https://medium.com/@ivangsa/high-fidelity-stateful-mocks-consumer-contracts-with-openapi-and-karatedsl-85a7f31cf84e)

## AsyncAPI strongly typed interfaces and SpringCloudStreams3 implementations

Generates strongly typed java code (Producer and Consumers) for Spring Cloud Streams 3 from AsyncAPI specification.

It supports:

- Imperative and Reactive styles
- Exposing your DTOs, Spring Messages or Kafka KStreams as parameter types.
- All message formats supported by AsyncAPI specification: AsyncAPI schema (inline), JSON Schema (external files) and Avro (external files).

> NOTE: some templates/combinations are still WIP

```shell
jbang zw -p io.zenwave360.generator.plugins.SpringCloudStream3Configuration \
    specFile=asyncapi.yml targetFolder=target/out \
    apiPackage=io.example.integration.test.api \
    modelPackage=io.example.integration.test.api.model \
    role=<PROVIDER | CLIENT> \
    style=<IMPERATIVE | REACTIVE>
```

## AsyncAPI interfaces Mocks and Contract Tests (ToBeDefined)

_TODO_: Use Pact.io? Spring Cloud Contract? Roll your own?

## KarateDSL Ent-to-End tests for REST APIs (using sister project ZenWave KarateIDE)

Use sister project [ZenWave KarateIDE](https://marketplace.visualstudio.com/items?itemName=KarateIDE.karate-ide)

[![KarateIDE: Generate KarateDSL Tests from OpenAPI in VSCode](https://github.com/ZenWave360/karate-ide/raw/master/resources/screenshots/generating-tests-from-openapi-youtube-embed.png)](https://www.youtube.com/watch?v=pYyRvly4cG8)

You can also find to deep dives into Contract Testing and API Mocking in this two medium articles:

- [Generating Karate Test Features from OpenAPI @medium](https://medium.com/@ivangsa/from-manual-to-contract-testing-with-karatedsl-and-karateide-i-5884f1732680#8311)
- [Generate Tests that simulates end-user Business Flows @medium](https://medium.com/@ivangsa/from-manual-to-contract-testing-with-karatedsl-and-karateide-i-5884f1732680#9b70)