/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice.modulecache;

import org.gradle.api.artifacts.ResolvedModuleVersion;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepository;
import org.gradle.internal.component.model.ModuleSource;
import org.gradle.internal.component.external.model.ModuleVersionMetaData;
import org.gradle.internal.component.external.model.MutableModuleVersionMetaData;

import java.math.BigInteger;

public interface ModuleMetaDataCache {
    CachedMetaData cacheMissing(ModuleComponentRepository repository, ModuleComponentIdentifier id);

    CachedMetaData cacheMetaData(ModuleComponentRepository repository, ModuleVersionMetaData metaData, ModuleSource moduleSource);

    CachedMetaData getCachedModuleDescriptor(ModuleComponentRepository repository, ModuleComponentIdentifier id);

    interface CachedMetaData {
        ResolvedModuleVersion getModuleVersion();

        MutableModuleVersionMetaData getMetaData();

        long getAgeMillis();

        BigInteger getDescriptorHash();

        boolean isMissing();

        ModuleSource getModuleSource();
    }
}
