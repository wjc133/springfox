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

package springfox.documentation.schema

import com.fasterxml.classmate.TypeResolver
import spock.lang.Specification

class TypesSpec extends Specification {
    def "detects void type"() {
        given:
        def typeResolver = new TypeResolver()
        expect:
        Types.isVoid(typeResolver.resolve(type)) == isVoid
        where:
        type         | isVoid
        Void.class   | true
        Void.TYPE    | true
        Integer.TYPE | false
    }
}
