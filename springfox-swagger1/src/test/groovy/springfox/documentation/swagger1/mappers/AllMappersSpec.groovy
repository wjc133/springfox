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

package springfox.documentation.swagger1.mappers

import spock.lang.Specification
import springfox.documentation.service.*
import springfox.documentation.swagger1.dto.*
import springfox.documentation.swagger1.mixins.MapperSupport

@Mixin(MapperSupport)
public class AllMappersSpec extends Specification {
    def "All AuthorizationTypesMapper null sources are mapped to null targets"() {
        given:
        AuthorizationTypesMapper mapper = authMapper()
        when:
        def mapped = mapper."toSwagger$typeToTest.simpleName"(null)
        then:
        mapped == null
        where:
        typeToTest << [ApiKey, OAuth, BasicAuth, ImplicitGrant, AuthorizationCodeGrant, TokenEndpoint,
                       TokenRequestEndpoint, AuthorizationScope, SecurityReference, LoginEndpoint]
    }

    def "All AllowableValuesMapper null sources are mapped to null targets"() {
        given:
        AllowableValuesMapper mapper = allowableValuesMapper()
        when:
        def mapped = mapper."toSwagger$typeToTest.simpleName"(null)
        then:
        mapped == null
        where:
        typeToTest << [AllowableListValues, AllowableRangeValues]
    }

    def "All ServiceModelToSwaggerMapper null sources are mapped to null targets"() {
        given:
        ServiceModelToSwaggerMapper mapper = serviceMapper()
        when:
        def mapped = mapper."toSwagger$typeToTest.simpleName"(null)
        then:
        mapped == null
        where:
        typeToTest << [ApiDescription, ApiInfo, ApiListing, ApiListingReference, ModelDto, ModelPropertyDto, Operation,
                       Parameter, ResourceListing, ResponseMessage]
    }
}
