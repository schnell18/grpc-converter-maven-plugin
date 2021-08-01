package [=javaPackage].converter;

<#if converterImports?has_content>
<#list converterImports as import>
import [=import];
</#list>
</#if>

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

<#if converterStaticImports?has_content>
<#list converterStaticImports as import>
import static [=import];
</#list>
</#if>

public class Converters {

<#list messages as message>
    <#if message.fields?has_content && message.virtualType == false>
    public static [=message.name] to[=message.name]RpcModel([=message.qualifiedName] source) {
        if (source == null) {
            return null;
        }

        [=message.name].Builder builder = [=message.name].newBuilder();
        <#list message.fields as field>
        <#-- ############### START OF PLURAL TYPE CONVERSION ############### -->
        <#if field.richType.pluralType>
        <#-- ############### START OF PLURAL OBJECT CONVERSION ############### -->
        <#if field.richType.complexType>
        if (source.get[=field.name?cap_first]() != null) {
            for ([=field.richType.qualifiedName] e: source.get[=field.name?cap_first]()) {
                builder.add[=field.singularName?cap_first](to[=field.richType.name]RpcModel(e));
            }
        }
        <#-- ############### END OF PLURAL OBJECT CONVERSION ############### -->
        <#-- ############### START OF PLURAL STRING CONVERSION ############### -->
        <#elseif field.type == "String">
        if (source.get[=field.name?cap_first]() != null) {
            builder.addAll[=field.singularName?cap_first](source.get[=field.name?cap_first]());
        }
        <#-- ############### END OF PLURAL STRING CONVERSION ############### -->
        <#-- ############### START OF PLURAL DATE CONVERSION ############### -->
        <#elseif field.type == "java.util.Date" || field.type == "Date">
        if (source.get[=field.name?cap_first]() != null) {
            builder.set[=field.singularName?cap_first](fromMillis(source.get[=field.name?cap_first]().getTime()));
        }
        <#-- ############### END OF PLURAL DATE CONVERSION ############### -->
        <#-- ############### START OF PLURAL BOOLEAN CONVERSION ############### -->
        <#elseif field.type == "boolean">
        builder.set[=field.singularName?cap_first](source.is[=field.name?cap_first]());
        <#-- ############### END OF PLURAL BOOLEAN CONVERSION ############### -->
        <#-- ############### START OF PLURAL OTHER TYPE CONVERSION ############### -->
        <#else>
        if (source.get[=field.name?cap_first]() != null) {
            builder.addAll[=field.singularName?cap_first](source.get[=field.name?cap_first]());
        }
        <#-- ############### END OF PLURAL OTHER TYPE CONVERSION ############### -->
        </#if>
        <#-- ############### END OF PLURAL TYPE CONVERSION ############### -->
        <#-- ############### START OF SINGULAR TYPE CONVERSION ############### -->
        <#else>
        <#-- ############### START OF SINGULAR OBJECT CONVERSION ############### -->
        <#if field.richType.complexType>
        if (source.get[=field.name?cap_first]() != null) {
            builder.set[=field.name?cap_first](to[=field.richType.name?cap_first]RpcModel(source.get[=field.name?cap_first]()));
        }
        <#-- ############### END OF SINGULAR OBJECT CONVERSION ############### -->
        <#-- ############### START OF SINGULAR STRING CONVERSION ############### -->
        <#elseif field.type == "String" || field.type == "Boolean" || field.type == "Integer" || field.type == "Long" || field.type == "Float" || field.type == "Double">
        if (source.get[=field.name?cap_first]() != null) {
            builder.set[=field.name?cap_first](source.get[=field.name?cap_first]());
        }
        <#-- ############### END OF SINGULAR STRING CONVERSION ############### -->
        <#-- ############### START OF SINGULAR DATE CONVERSION ############### -->
        <#elseif field.type == "java.util.Date" || field.type == "Date">
        if (source.get[=field.name?cap_first]() != null) {
            builder.set[=field.name?cap_first](fromMillis(source.get[=field.name?cap_first]().getTime()));
        }
        <#-- ############### END OF SINGULAR DATE CONVERSION ############### -->
        <#-- ############### START OF SINGULAR BOOLEAN CONVERSION ############### -->
        <#elseif field.type == "boolean">
        builder.set[=field.name?cap_first](source.is[=field.name?cap_first]());
        <#-- ############### END OF SINGULAR BOOLEAN CONVERSION ############### -->
        <#-- ############### START OF SINGULAR OTHER TYPE CONVERSION ############### -->
        <#else>
        builder.set[=field.name?cap_first](source.get[=field.name?cap_first]());
        <#-- ############### END OF SINGULAR OTHER TYPE CONVERSION ############### -->
        </#if>
        <#-- ############### END OF SINGULAR TYPE CONVERSION ############### -->
        </#if>
        </#list>
        return builder.build();
    }

    <#-- converts RPC model to biz model -->
    public static [=message.qualifiedName] from[=message.name?cap_first]RpcModel([=message.name] source) {
        if (source == null) {
            return null;
        }
        [=message.qualifiedName] dest = new [=message.qualifiedName]();
        <#list message.fields as field>
        <#-- ############### START OF PLURAL TYPE CONVERSION ############### -->
        <#if field.richType.pluralType>
        <#-- ############### START OF PLURAL OBJECT CONVERSION ############### -->
        <#if field.richType.complexType>

        List<[=field.richType.qualifiedName]> [=field.singularName]List = new ArrayList<[=field.richType.qualifiedName]>();
        for ([=field.richType.name] e : source.get[=field.singularName?cap_first]List()) {
            if (e != null) [=field.singularName]List.add(from[=field.richType.name?cap_first]RpcModel(e));
        }
        dest.set[=field.name?cap_first]([=field.singularName]List);
        <#-- ############### END OF PLURAL OBJECT CONVERSION ############### -->
        <#-- ############### START OF PLURAL STRING CONVERSION ############### -->
        <#elseif field.type = "String">
        <#if field.type?contains("Set<") >
        dest.set[=field.name?cap_first](new HashSet(source.get[=field.singularName?cap_first]List()));
        <#else>
        dest.set[=field.name?cap_first](source.get[=field.singularName?cap_first]List());
        </#if>
        <#-- ############### END OF PLURAL STRING CONVERSION ############### -->
        <#-- ############### START OF PLURAL DATE CONVERSION ############### -->
        <#elseif field.type == "java.util.Date" || field.type == "Date">
        <#-- TODO fix it later -->
        dest.set[=field.name?cap_first](source.get[=field.singularName?cap_first]List());
        <#-- ############### END OF PLURAL DATE CONVERSION ############### -->
        <#-- ############### START OF PLURAL OTHER TYPE CONVERSION ############### -->
        <#else>
        <#if field.type?contains("Set<") >
        dest.set[=field.name?cap_first](new HashSet(source.get[=field.singularName?cap_first]List()));
        <#else>
        dest.set[=field.name?cap_first](source.get[=field.singularName?cap_first]List());
        </#if>
        </#if>
        <#-- ############### END OF PLURAL OTHER TYPE CONVERSION ############### -->
        <#-- ############### END OF PLURAL TYPE CONVERSION ############### -->
        <#-- ############### START OF SINGULAR TYPE CONVERSION ############### -->
        <#else>
        <#-- ############### START OF SINGULAR OBJECT CONVERSION ############### -->
        <#if field.richType.complexType>
        dest.set[=field.name?cap_first](from[=field.richType.name?cap_first]RpcModel(source.has[=field.name?cap_first]() ? source.get[=field.name?cap_first]() : null));
        <#-- ############### END OF SINGULAR OBJECT CONVERSION ############### -->
        <#-- ############### START OF SINGULAR STRING CONVERSION ############### -->
        <#elseif field.type = "String">
        dest.set[=field.name?cap_first]("".equals(source.get[=field.name?cap_first]()) ? null : source.get[=field.name?cap_first]());
        <#-- ############### END OF SINGULAR STRING CONVERSION ############### -->
        <#-- ############### START OF SINGULAR DATE CONVERSION ############### -->
        <#elseif field.type == "java.util.Date" || field.type == "Date">
        if (source.has[=field.name?cap_first]()) {
            dest.set[=field.name?cap_first](new Date(source.get[=field.name?cap_first]().getSeconds() * 1000));
        }
        <#-- ############### END OF SINGULAR DATE CONVERSION ############### -->
        <#-- ############### START OF SINGULAR OTHER TYPE CONVERSION ############### -->
        <#else>
        dest.set[=field.name?cap_first](source.get[=field.name?cap_first]());
        </#if>
        <#-- ############### END OF SINGULAR OTHER TYPE CONVERSION ############### -->
        </#if>
        <#-- ############### END OF SINGULAR TYPE CONVERSION ############### -->
        </#list>
        return dest;
    }
    </#if>

</#list>

}

// vim: set ai nu nobk expandtab ts=4 sw=2 tw=72 syntax=java :
