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
package springfox.documentation.swagger.web;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;

import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.service.Tags.emptyTags;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

@Component
@Order(value = SWAGGER_PLUGIN_ORDER)
public class SwaggerApiListingReader implements ApiListingBuilderPlugin {
    @Override
    public void apply(ApiListingContext apiListingContext) {
        Class<?> controllerClass = apiListingContext.getResourceGroup().getControllerClass();
        Optional<Api> apiAnnotation = fromNullable(findAnnotation(controllerClass, Api.class));
        String description = emptyToNull(apiAnnotation.transform(descriptionExtractor()).orNull());

        Set<String> tagSet = apiAnnotation.transform(tags())
                .or(Sets.<String>newTreeSet());
        if (tagSet.isEmpty()) {
            tagSet.add(apiListingContext.getResourceGroup().getGroupName());
        }
        apiListingContext.apiListingBuilder()
                .description(description)
                .tags(tagSet);
    }

    private Function<Api, String> descriptionExtractor() {
        return new Function<Api, String>() {
            @Override
            public String apply(Api input) {
                return input.description();
            }
        };
    }

    private Function<Api, Set<String>> tags() {
        return new Function<Api, Set<String>>() {
            @Override
            public Set<String> apply(Api input) {
                return newTreeSet(from(newArrayList(input.tags())).filter(emptyTags()).toSet());
            }
        };
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return pluginDoesApply(delimiter);
    }
}
