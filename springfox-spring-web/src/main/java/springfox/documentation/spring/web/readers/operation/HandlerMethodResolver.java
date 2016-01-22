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

package springfox.documentation.spring.web.readers.operation;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.service.ResolvedMethodParameter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class HandlerMethodResolver {

    private final TypeResolver typeResolver;

    public HandlerMethodResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public ResolvedType methodReturnType(HandlerMethod handlerMethod) {
        return resolvedMethod(handlerMethod).transform(toReturnType(typeResolver)).or(typeResolver.resolve(Void.TYPE));
    }

    public Optional<ResolvedMethod> resolvedMethod(HandlerMethod handlerMethod) {
        if (handlerMethod == null) {
            return Optional.absent();
        }
        Class hostClass = useType(handlerMethod.getBeanType())
                .or(handlerMethod.getMethod().getDeclaringClass());
        ResolvedType beanType = typeResolver.resolve(hostClass);
        MemberResolver resolver = new MemberResolver(typeResolver);
        resolver.setIncludeLangObject(false);
        ResolvedTypeWithMembers typeWithMembers = resolver.resolve(beanType, null, null);
        Iterable<ResolvedMethod> filtered = filter(newArrayList(typeWithMembers.getMemberMethods()),
                methodNamesAreSame(handlerMethod.getMethod()));
        return resolveToMethodWithMaxResolvedTypes(filtered, handlerMethod.getMethod());
    }

    private static Function<ResolvedMethod, ResolvedType> toReturnType(final TypeResolver resolver) {
        return new Function<ResolvedMethod, ResolvedType>() {
            @Override
            public ResolvedType apply(ResolvedMethod input) {
                return fromNullable(input.getReturnType()).or(resolver.resolve(Void.TYPE));
            }
        };
    }

    public static Optional<Class> useType(Class beanType) {
        if (Proxy.class.isAssignableFrom(beanType)) {
            return Optional.absent();
        }
        if (Class.class.getName().equals(beanType.getName())) {
            return Optional.absent();
        }
        return fromNullable(beanType);
    }

    public List<ResolvedMethodParameter> methodParameters(final HandlerMethod methodToResolve) {
        return resolvedMethod(methodToResolve)
                .transform(toParameters(methodToResolve))
                .or(Lists.<ResolvedMethodParameter>newArrayList());
    }

    private Function<ResolvedMethod, List<ResolvedMethodParameter>> toParameters(final HandlerMethod methodToResolve) {
        return new Function<ResolvedMethod, List<ResolvedMethodParameter>>() {
            @Override
            public List<ResolvedMethodParameter> apply(ResolvedMethod input) {
                List<ResolvedMethodParameter> parameters = newArrayList();
                MethodParameter[] methodParameters = methodToResolve.getMethodParameters();
                for (int i = 0; i < input.getArgumentCount(); i++) {
                    parameters.add(new ResolvedMethodParameter(methodParameters[i], input.getArgumentType(i)));
                }
                return parameters;
            }
        };
    }

    @VisibleForTesting
    static Ordering<ResolvedMethod> byArgumentCount() {
        return Ordering.from(new Comparator<ResolvedMethod>() {
            @Override
            public int compare(ResolvedMethod first, ResolvedMethod second) {
                return Ints.compare(first.getArgumentCount(), second.getArgumentCount());
            }
        });
    }

    @VisibleForTesting
    boolean bothAreVoids(ResolvedType candidateMethodReturnValue, Type returnType) {
        return (Void.class == candidateMethodReturnValue.getErasedType()
                || Void.TYPE == candidateMethodReturnValue.getErasedType())
                && (Void.TYPE == returnType
                || Void.class == returnType);
    }

    private static Iterable<ResolvedMethod> methodsWithSameNumberOfParams(Iterable<ResolvedMethod> filtered,
                                                                          final Method methodToResolve) {

        return filter(filtered, new Predicate<ResolvedMethod>() {
            @Override
            public boolean apply(ResolvedMethod input) {
                return input.getArgumentCount() == methodToResolve.getParameterTypes().length;
            }
        });
    }

    private static Predicate<ResolvedMethod> methodNamesAreSame(final Method methodToResolve) {
        return new Predicate<ResolvedMethod>() {
            @Override
            public boolean apply(ResolvedMethod input) {
                return input.getRawMember().getName().equals(methodToResolve.getName());
            }
        };
    }

    private Optional<ResolvedMethod> resolveToMethodWithMaxResolvedTypes(Iterable<ResolvedMethod> filtered,
                                                                         Method methodToResolve) {

        if (Iterables.size(filtered) > 1) {
            Iterable<ResolvedMethod> covariantMethods = covariantMethods(filtered, methodToResolve);
            if (Iterables.size(covariantMethods) == 0) {
                return Optional.of(byArgumentCount().max(filtered));
            } else if (Iterables.size(covariantMethods) == 1) {
                return FluentIterable.from(covariantMethods).first();
            } else {
                return Optional.of(byArgumentCount().max(covariantMethods));
            }
        }
        return FluentIterable.from(filtered).first();
    }

    private Iterable<ResolvedMethod> covariantMethods(Iterable<ResolvedMethod> filtered,
                                                      final Method methodToResolve) {

        return filter(methodsWithSameNumberOfParams(filtered, methodToResolve), onlyCovariantMethods(methodToResolve));
    }

    private Predicate<ResolvedMethod> onlyCovariantMethods(final Method methodToResolve) {
        return new Predicate<ResolvedMethod>() {
            @Override
            public boolean apply(ResolvedMethod input) {
                for (int index = 0; index < input.getArgumentCount(); index++) {
                    if (!covariant(input.getArgumentType(index), methodToResolve.getGenericParameterTypes()[index])) {
                        return false;
                    }
                }
                ResolvedType candidateMethodReturnValue = returnTypeOrVoid(input);
                return bothAreVoids(candidateMethodReturnValue, methodToResolve.getGenericReturnType())
                        || contravariant(candidateMethodReturnValue, methodToResolve.getGenericReturnType());
            }
        };
    }

    private ResolvedType returnTypeOrVoid(ResolvedMethod input) {
        ResolvedType returnType = input.getReturnType();
        if (returnType == null) {
            returnType = typeResolver.resolve(Void.class);
        }
        return returnType;
    }

    boolean contravariant(ResolvedType candidateMethodReturnValue, Type returnValueOnMethod) {
        return isSubClass(candidateMethodReturnValue, returnValueOnMethod)
                || isGenericTypeSubclass(candidateMethodReturnValue, returnValueOnMethod);
    }


    @VisibleForTesting
    boolean isGenericTypeSubclass(ResolvedType candidateMethodReturnValue, Type returnValueOnMethod) {
        return returnValueOnMethod instanceof ParameterizedType &&
                candidateMethodReturnValue.getErasedType()
                        .isAssignableFrom((Class<?>) ((ParameterizedType) returnValueOnMethod).getRawType());
    }

    @VisibleForTesting
    boolean isSubClass(ResolvedType candidateMethodReturnValue, Type returnValueOnMethod) {
        return returnValueOnMethod instanceof Class
                && candidateMethodReturnValue.getErasedType().isAssignableFrom((Class<?>) returnValueOnMethod);
    }

    @VisibleForTesting
    boolean covariant(ResolvedType candidateMethodArgument, Type argumentOnMethod) {
        return isSuperClass(candidateMethodArgument, argumentOnMethod)
                || isGenericTypeSuperClass(candidateMethodArgument, argumentOnMethod);
    }

    @VisibleForTesting
    boolean isGenericTypeSuperClass(ResolvedType candidateMethodArgument, Type argumentOnMethod) {
        return argumentOnMethod instanceof ParameterizedType &&
                ((Class<?>) ((ParameterizedType) argumentOnMethod).getRawType())
                        .isAssignableFrom(candidateMethodArgument.getErasedType());
    }

    @VisibleForTesting
    boolean isSuperClass(ResolvedType candidateMethodArgument, Type argumentOnMethod) {
        return argumentOnMethod instanceof Class
                && ((Class<?>) argumentOnMethod).isAssignableFrom(candidateMethodArgument.getErasedType());
    }
}
