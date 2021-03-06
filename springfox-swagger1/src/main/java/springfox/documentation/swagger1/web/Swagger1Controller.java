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

package springfox.documentation.swagger1.web;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger1.dto.ApiListing;
import springfox.documentation.swagger1.dto.ResourceListing;
import springfox.documentation.swagger1.mappers.ServiceModelToSwaggerMapper;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Multimaps.transformEntries;
import static springfox.documentation.swagger1.mappers.Mappers.toApiListingDto;
import static springfox.documentation.swagger1.web.ApiListingMerger.mergedApiListing;

@Controller
@ApiIgnore
@RequestMapping("${springfox.documentation.swagger.v1.path:/api-docs}")
public class Swagger1Controller {

    @Autowired
    private DocumentationCache documentationCache;

    @Autowired
    private ServiceModelToSwaggerMapper mapper;

    @Autowired
    private JsonSerializer jsonSerializer;

    @ApiIgnore
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<Json> getResourceListing(@RequestParam(value = "group", required = false) String swaggerGroup) {
        return getSwaggerResourceListing(swaggerGroup);
    }

    @ApiIgnore
    @RequestMapping(value = {"/{swaggerGroup}/{apiDeclaration}"}, method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<Json> getApiListing(@PathVariable String swaggerGroup, @PathVariable String apiDeclaration) {
        return getSwaggerApiListing(swaggerGroup, apiDeclaration);
    }

    private ResponseEntity<Json> getSwaggerApiListing(String swaggerGroup, String apiDeclaration) {
        String groupName = Optional.fromNullable(swaggerGroup).or("default");
        Documentation documentation = documentationCache.documentationByGroup(groupName);
        if (documentation == null) {
            return new ResponseEntity<Json>(HttpStatus.NOT_FOUND);
        }
        Multimap<String, springfox.documentation.service.ApiListing> apiListingMap = documentation.getApiListings();
        Map<String, Collection<ApiListing>> dtoApiListings
                = transformEntries(apiListingMap, toApiListingDto(mapper)).asMap();

        Collection<ApiListing> apiListings = dtoApiListings.get(apiDeclaration);
        return mergedApiListing(apiListings)
                .transform(toJson())
                .transform(toResponseEntity(Json.class))
                .or(new ResponseEntity<Json>(HttpStatus.NOT_FOUND));
    }

    private Function<ApiListing, Json> toJson() {
        return new Function<ApiListing, Json>() {
            @Override
            public Json apply(ApiListing input) {
                return jsonSerializer.toJson(input);
            }
        };
    }

    private ResponseEntity<Json> getSwaggerResourceListing(String swaggerGroup) {
        String groupName = Optional.fromNullable(swaggerGroup).or(Docket.DEFAULT_GROUP_NAME);
        Documentation documentation = documentationCache.documentationByGroup(groupName);
        if (documentation == null) {
            return new ResponseEntity<Json>(HttpStatus.NOT_FOUND);
        }
        springfox.documentation.service.ResourceListing listing = documentation.getResourceListing();
        ResourceListing resourceListing = mapper.toSwaggerResourceListing(listing);

        return Optional.fromNullable(jsonSerializer.toJson(resourceListing))
                .transform(toResponseEntity(Json.class))
                .or(new ResponseEntity<Json>(HttpStatus.NOT_FOUND));
    }

    private <T> Function<T, ResponseEntity<T>> toResponseEntity(Class<T> clazz) {
        return new Function<T, ResponseEntity<T>>() {
            @Override
            public ResponseEntity<T> apply(T input) {
                return new ResponseEntity<T>(input, HttpStatus.OK);
            }
        };
    }
}
