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

package springfox.documentation.spring.web.readers.operation

import com.fasterxml.classmate.GenericType
import com.fasterxml.classmate.GenericType
import com.fasterxml.classmate.MemberResolver
import com.fasterxml.classmate.MemberResolver
import com.fasterxml.classmate.TypeResolver
import com.fasterxml.classmate.TypeResolver
import com.fasterxml.classmate.members.ResolvedMethod
import com.fasterxml.classmate.members.ResolvedMethod
import com.google.common.base.Predicate
import com.google.common.base.Predicate
import com.google.common.collect.FluentIterable
import com.google.common.collect.FluentIterable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Specification
import spock.lang.Specification
import springfox.documentation.spring.web.dummy.DummyClass
import springfox.documentation.spring.web.dummy.DummyClass
import springfox.documentation.spring.web.dummy.models.Example
import springfox.documentation.spring.web.dummy.models.Example
import springfox.documentation.spring.web.dummy.models.Parent
import springfox.documentation.spring.web.dummy.models.Parent
import springfox.documentation.spring.web.mixins.HandlerMethodsSupport
import springfox.documentation.spring.web.mixins.HandlerMethodsSupport

import java.lang.reflect.ParameterizedType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.Type

import static com.google.common.collect.Lists.*
import static HandlerMethodResolver.*
import static com.google.common.collect.Lists.newArrayList
import static springfox.documentation.spring.web.readers.operation.HandlerMethodResolver.byArgumentCount

class HandlerMethodResolverSpec extends Specification implements HandlerMethodsSupport {
    def "Methods with same name are distinguished based on variance of parameters and return types"() {
        given:
        def sut = new HandlerMethodResolver(new TypeResolver())
        def resolvedParameters = sut.methodParameters(handlerMethod)
                .collect() { it.resolvedParameterType.getErasedType().simpleName }
                .sort()
        def resolvedReturnType = sut.methodReturnType(handlerMethod).erasedType.simpleName
        expect:
        parameters == resolvedParameters
        returnType == resolvedReturnType
        where:
        handlerMethod      | returnType       | parameters
        methodWithChild()  | "void"           | ["Child", "Integer"]
        methodWithParent() | "ResponseEntity" | ["Integer", "Parent"]
    }

    def "When method was not resolvable calling methodParameters returns empty list"() {
        given:
        def sut = new HandlerMethodResolver(new TypeResolver()) {
        }
        when:
        def handlerMethod = unresolvableMethod()
        and:
        def parameters = sut.methodParameters(handlerMethod)
        then:
        parameters.size() == 0
    }

    def "When method was not resolvable calling methodReturnType returns return type as resolved"() {
        given:
        def sut = new HandlerMethodResolver(new TypeResolver()) {
        }
        when:
        def returnType = sut.methodReturnType(handlerMethod)
        then:
        expectedReturnType.equals(returnType.erasedType.simpleName)
        where:
        handlerMethod      | expectedReturnType
        methodWithChild()  | "void"
        methodWithParent() | "ResponseEntity"
    }

    def "Sorts on argument count"() {
        given:
        def resolver = new TypeResolver()
        def memberResolver = new MemberResolver(resolver)
        def dummyClass = memberResolver.resolve(resolver.resolve(DummyClass), null, null)
        def allMethods = newArrayList(dummyClass.memberMethods)

        when:
        def list = FluentIterable.from(allMethods).filter(subset())
        def sorted = byArgumentCount().sortedCopy(list)

        then:
        sorted.get(0).name == 'methodWithNoArgs'
        sorted.get(1).name == 'methodWithOneArgs'
        sorted.get(2).name == 'methodWithTwoArgs'
        where:
        handlerMethods << [
                methodOnDummyClasss('methodWithTwoArgs', int, String),
                methodOnDummyClasss('methodWithOneArgs', int),
                methodOnDummyClasss('methodWithNoArgs')
        ]
    }

    private Predicate<ResolvedMethod> subset() {
        new Predicate<ResolvedMethod>() {
            @Override
            boolean apply(ResolvedMethod input) {
                return ['methodWithNoArgs', 'methodWithOneArgs', 'methodWithTwoArgs'].contains(input.name)
            }
        }
    }

    def "Is able to determine if both types are void"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(subClass)
        expect:
        sut.bothAreVoids(resolvedType, superClass) == areSame
        where:
        superClass | subClass  | areSame
        Void.TYPE  | Integer   | false
        Void.TYPE  | Void      | true
        Void.TYPE  | Void.TYPE | true
        Void       | Integer   | false
        Void       | Void      | true
        Void       | Void.TYPE | true
    }

    def "Is able to determine super classes"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(subClass)
        expect:
        sut.isSuperClass(resolvedType, superClass) == isSuperClass
        where:
        superClass   | subClass         | isSuperClass
        Parent       | DummyClass.Child | false
        Parent       | Example          | true
        String       | Integer          | false
        Object       | String           | true
        Integer.TYPE | Integer          | false
    }

    def "Is able to determine sub classes"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(superClass)
        expect:
        sut.isSubClass(resolvedType, subClass) == isSubClass
        where:
        superClass | subClass         | isSubClass
        Parent     | DummyClass.Child | false
        Parent     | Example          | true
        String     | Integer          | false
        Object     | String           | true
    }

    def "Is able to determine generic super classes"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(ChildResponseEntity, subClass)
        def superClassParameterized = parameterizedType(superClass, parameterizedType)
        expect:
        sut.isGenericTypeSuperClass(resolvedType, superClassParameterized) == isSuperClass
        where:
        parameterizedType   | superClass | subClass         | isSuperClass
        ResponseEntity      | Parent     | DummyClass.Child | true
        ResponseEntity      | Parent     | Example          | true
        ResponseEntity      | String     | Integer          | true
        ResponseEntity      | Object     | String           | true
        ChildResponseEntity | Parent     | DummyClass.Child | true
        ChildResponseEntity | Parent     | Example          | true
        ChildResponseEntity | String     | Integer          | true
        ChildResponseEntity | Object     | String           | true
        GenericType         | Parent     | DummyClass.Child | false
        GenericType         | Parent     | Example          | false
        GenericType         | String     | Integer          | false
        GenericType         | Object     | String           | false
    }

    def "Is able to determine generic classes are covariant"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(ChildResponseEntity, subClass)
        def superClassParameterized = parameterizedType(superClass, parameterizedType)
        expect:
        sut.covariant(resolvedType, superClassParameterized) == isSuperClass
        where:
        parameterizedType   | superClass | subClass         | isSuperClass
        ResponseEntity      | Parent     | DummyClass.Child | true
        ResponseEntity      | Parent     | Example          | true
        ResponseEntity      | String     | Integer          | true
        ResponseEntity      | Object     | String           | true
        ChildResponseEntity | Parent     | DummyClass.Child | true
        ChildResponseEntity | Parent     | Example          | true
        ChildResponseEntity | String     | Integer          | true
        ChildResponseEntity | Object     | String           | true
        GenericType         | Parent     | DummyClass.Child | false
        GenericType         | Parent     | Example          | false
        GenericType         | String     | Integer          | false
        GenericType         | Object     | String           | false
    }

    def "Is able to determine generic sub classes"() {
        given:
        def resolver = new TypeResolver()
        def sut = new HandlerMethodResolver(resolver)
        and:
        def resolvedType = resolver.resolve(ResponseEntity, superClass)
        ParameterizedType superClassParameterized = parameterizedType(subClass, parameterizedType)
        expect:
        sut.isGenericTypeSubclass(resolvedType, superClassParameterized) == isSubclass
        where:
        parameterizedType   | superClass | subClass         | isSubclass
        ResponseEntity      | Parent     | DummyClass.Child | true
        ResponseEntity      | Parent     | Example          | true
        ResponseEntity      | String     | Integer          | true
        ResponseEntity      | Object     | String           | true
        ChildResponseEntity | Parent     | DummyClass.Child | true
        ChildResponseEntity | Parent     | Example          | true
        ChildResponseEntity | String     | Integer          | true
        ChildResponseEntity | Object     | String           | true
        GenericType         | Parent     | DummyClass.Child | false
        GenericType         | Parent     | Example          | false
        GenericType         | String     | Integer          | false
        GenericType         | Object     | String           | false
    }

    class ChildResponseEntity<T> extends ResponseEntity {

        ChildResponseEntity(HttpStatus statusCode) {
            super(statusCode)
        }

        ChildResponseEntity(Object body, HttpStatus statusCode) {
            super(body, statusCode)
        }

        ChildResponseEntity(MultiValueMap headers, HttpStatus statusCode) {
            super(headers, statusCode)
        }

        ChildResponseEntity(Object body, MultiValueMap headers, HttpStatus statusCode) {
            super(body, headers, statusCode)
        }
    }

    static ParameterizedType parameterizedType(typeArgument, parameterizedType) {
        new ParameterizedType() {
            @Override
            Type[] getActualTypeArguments() {
                [typeArgument]
            }

            @Override
            Type getRawType() {
                parameterizedType
            }

            @Override
            Type getOwnerType() {
                ResponseEntity
            }
        }
    }
}
