syntax = "proto3";

package [=protobufPackage];
<#if importGoogleTimestamp>
import "google/protobuf/timestamp.proto";
</#if>

option java_package = "[=javaPackage]";
option java_outer_classname = "[=outerClassName]";
option java_multiple_files = true;

<#assign services = services>
<#list services as service>
    <#assign methods = service.methods>
service [=service.className] {
    <#list methods as method>
    <#if method.description??>
    // [=method.description]
    </#if>
    rpc [=method.name] ([=method.unifiedParameter.name]) returns ([=method.returnType]);

    </#list>
}

</#list>

<#list messages as message>
message [=message.name] {
    <#if message.fields?has_content>
    <#assign fields = message.fields>
    <#list fields as field>
    // [=field.description!"TODO: 请添加字段说明"]
    <#if field.richType.pluralType>repeated </#if>[=field.protobufType] <#if field.richType.pluralType>[=field.singularName]<#else>[=field.name]</#if> = [=field.sequenceNo];

    </#list>
    </#if>
}

</#list>

// vim: set ai nu nobk expandtab ts=4 sw=2 tw=72 syntax=protobuf :
