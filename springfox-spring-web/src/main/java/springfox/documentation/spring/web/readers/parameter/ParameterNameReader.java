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

package springfox.documentation.spring.web.readers.parameter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ParameterNameReader implements ParameterBuilderPlugin {

    public static final String SPRING4_DISCOVERER = "org.springframework.core.DefaultParameterNameDiscoverer";
    private final ParameterNameDiscoverer parameterNameDiscover = parameterNameDiscoverer();

    @Override
    public void apply(ParameterContext context) {
        MethodParameter methodParameter = context.methodParameter();
        String name = findParameterNameFromAnnotations(methodParameter);
        if (isNullOrEmpty(name)) {
            Optional<String> discoveredName = discoveredName(methodParameter);
            name = discoveredName.isPresent()
                    ? discoveredName.get()
                    : format("param%s", methodParameter.getParameterIndex());
        }
        context.parameterBuilder()
                .name(name)
                .description(name);
    }

    private Optional<String> discoveredName(MethodParameter methodParameter) {
        String[] discoveredNames = parameterNameDiscover.getParameterNames(methodParameter.getMethod());
        return discoveredNames != null && methodParameter.getParameterIndex() < discoveredNames.length
                ? Optional.fromNullable(emptyToNull(discoveredNames[methodParameter.getParameterIndex()]))
                : Optional.<String>absent();
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private String findParameterNameFromAnnotations(MethodParameter methodParameter) {
        List<Annotation> methodAnnotations = newArrayList(methodParameter.getParameterAnnotations());
        return from(methodAnnotations)
                .filter(PathVariable.class).first().transform(pathVariableValue())
                .or(first(methodAnnotations, ModelAttribute.class).transform(modelAttributeValue()))
                .or(first(methodAnnotations, RequestParam.class).transform(requestParamValue()))
                .or(first(methodAnnotations, RequestHeader.class).transform(requestHeaderValue()))
                .orNull();
    }

    private ParameterNameDiscoverer parameterNameDiscoverer() {
        ParameterNameDiscoverer dicoverer;
        try {
            dicoverer = (ParameterNameDiscoverer) Class.forName(SPRING4_DISCOVERER).newInstance();
        } catch (Exception e) {
            dicoverer = new LocalVariableTableParameterNameDiscoverer();
        }
        return dicoverer;
    }

    private <T> Optional<T> first(List<Annotation> methodAnnotations, Class<T> ofType) {
        return from(methodAnnotations).filter(ofType).first();
    }

    private Function<RequestHeader, String> requestHeaderValue() {
        return new Function<RequestHeader, String>() {
            @Override
            public String apply(RequestHeader input) {
                return input.value();
            }
        };
    }

    private Function<RequestParam, String> requestParamValue() {
        return new Function<RequestParam, String>() {
            @Override
            public String apply(RequestParam input) {
                return input.value();
            }
        };
    }

    private Function<ModelAttribute, String> modelAttributeValue() {
        return new Function<ModelAttribute, String>() {
            @Override
            public String apply(ModelAttribute input) {
                return input.value();
            }
        };
    }

    private Function<PathVariable, String> pathVariableValue() {
        return new Function<PathVariable, String>() {
            @Override
            public String apply(PathVariable input) {
                return input.value();
            }
        };
    }

}
