package ${api.apiPackage};

import org.springframework.messaging.Message;

import java.util.function.Consumer;

<#if api.modelPackage??>
import ${api.modelPackage}.*;
</#if>


public interface I${operation.operationIdCamelCase} extends Function<Flux<Message<?>>, Mono<Void>> {

<#list operation.messages as message>
    <#assign messageType = exposeMessage?string('Message<' + message.paramType + '>', message.paramType)>
    public static interface ${message.name} extends Function<Flux<${messageType}>, Mono<Void>> {
    }

</#list>

}