/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.schema.property.field

import springfox.documentation.schema.AlternateTypesSupport
import springfox.documentation.schema.AlternateTypesSupport
import springfox.documentation.schema.DefaultGenericTypeNamingStrategy
import springfox.documentation.schema.DefaultGenericTypeNamingStrategy
import springfox.documentation.schema.SchemaSpecification
import springfox.documentation.schema.SchemaSpecification
import springfox.documentation.schema.TypeWithGettersAndSetters
import springfox.documentation.schema.TypeWithGettersAndSetters
import springfox.documentation.schema.mixins.ModelPropertyLookupSupport
import springfox.documentation.schema.mixins.ModelPropertyLookupSupport
import springfox.documentation.schema.mixins.TypesForTestingSupport
import springfox.documentation.schema.mixins.TypesForTestingSupport
import springfox.documentation.service.AllowableListValues
import springfox.documentation.service.AllowableListValues

import static com.google.common.collect.Lists.*
import static com.google.common.collect.Lists.newArrayList
import static springfox.documentation.spi.DocumentationType.*
import static springfox.documentation.spi.DocumentationType.SWAGGER_12
import static springfox.documentation.spi.schema.contexts.ModelContext.*
import static springfox.documentation.spi.schema.contexts.ModelContext.fromParent
import static springfox.documentation.spi.schema.contexts.ModelContext.inputParam

@Mixin([TypesForTestingSupport, ModelPropertyLookupSupport, AlternateTypesSupport])
class FieldModelPropertySpec extends SchemaSpecification {
    def namingStrategy = new DefaultGenericTypeNamingStrategy()

    def "Extracting information from resolved fields"() {
        given:
        def modelContext = inputParam(TypeWithGettersAndSetters, SWAGGER_12, alternateTypeProvider(), namingStrategy)
        def field = field(TypeWithGettersAndSetters, fieldName)
        def sut = new FieldModelProperty(fieldName, field, alternateTypeProvider())

        expect:
        sut.propertyDescription() == null //documentationType(): Added test
        !sut.required
        typeNameExtractor.typeName(fromParent(modelContext, sut.getType())) == typeName
        sut.qualifiedTypeName() == qualifiedTypeName
        if (allowableValues != null) {
            def values = newArrayList(allowableValues)
            sut.allowableValues() == new AllowableListValues(values, "string")
        } else {
            sut.allowableValues() == null
        }
        sut.getName() == fieldName
        sut.getType() == field.getType()

        where:
        fieldName     || description | isRequired | typeName | qualifiedTypeName | allowableValues
        "intProp"     || "int Property Field" | true | "int" | "int" | null
        "boolProp"    || null | false | "boolean" | "boolean" | null
        "enumProp"    || null | false | "string" | "springfox.documentation.schema.ExampleEnum" | ["ONE", "TWO"]
        "genericProp" || null | false | "GenericType«string»" | "springfox.documentation.schema.GenericType<java.lang.String>" | null
    }

    def "Extracting information from generic fields with array type binding"() {
        given:
        def typeToTest = TypeWithGettersAndSetters
        def modelContext = inputParam(typeToTest, SWAGGER_12, alternateTypeProvider(), namingStrategy)
        def field = field(typeToTest, fieldName)
        def sut = new FieldModelProperty(fieldName, field, alternateTypeProvider())

        expect:
        typeNameExtractor.typeName(fromParent(modelContext, sut.getType())) == typeName
        sut.qualifiedTypeName() == qualifiedTypeName
        sut.getName() == fieldName
        sut.getType() == field.getType()


        where:
        fieldName              || typeName | qualifiedTypeName
        "genericByteArray"     || "GenericType«Array«byte»»" | "springfox.documentation.schema.GenericType<byte[]>"
        "genericCategoryArray" || "GenericType«Array«Category»»" | "springfox.documentation.schema.GenericType<springfox.documentation.schema.Category[]>"
    }
}
