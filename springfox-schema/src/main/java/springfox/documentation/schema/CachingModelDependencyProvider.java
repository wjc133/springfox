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
package springfox.documentation.schema;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Qualifier("cachedModelDependencies")
public class CachingModelDependencyProvider implements ModelDependencyProvider {
    private final LoadingCache<ModelContext, Set<ResolvedType>> cache;
    private final ModelDependencyProvider delegate;

    @Autowired
    public CachingModelDependencyProvider(@Qualifier("default") final ModelDependencyProvider delegate) {
        this.delegate = delegate;
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build(new CacheLoader<ModelContext, Set<ResolvedType>>() {
                    public Set<ResolvedType> load(ModelContext key) {
                        return delegate.dependentModels(key);
                    }
                });
    }

    @Override
    public Set<ResolvedType> dependentModels(ModelContext modelContext) {
//      return delegate.dependentModels(modelContext);
        return cache.getUnchecked(modelContext);
    }

}
