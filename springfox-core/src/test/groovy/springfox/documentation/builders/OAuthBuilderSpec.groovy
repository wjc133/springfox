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

package springfox.documentation.builders

import spock.lang.Specification
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.GrantType

class OAuthBuilderSpec extends Specification {
    def "Setting properties on the builder with non-null values"() {
        given:
        def sut = new OAuthBuilder()
        when:
        sut."$builderMethod"(value)
        and:
        def built = sut.build()
        then:
        built."$property" == value

        where:
        builderMethod | value                      | property
        'scopes'      | [Mock(AuthorizationScope)] | 'scopes'
        'grantTypes'  | [Mock(GrantType)]          | 'grantTypes'
    }

    def "Setting builder properties to null values preserves existing values"() {
        given:
        def sut = new OAuthBuilder()
        when:
        sut."$builderMethod"(value)
        sut."$builderMethod"(null)
        and:
        def built = sut.build()
        then:
        built."$property" == value

        where:
        builderMethod | value                      | property
        'scopes'      | [Mock(AuthorizationScope)] | 'scopes'
        'grantTypes'  | [Mock(GrantType)]          | 'grantTypes'
    }
}
