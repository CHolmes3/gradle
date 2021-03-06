/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.resources.normalization.internal;

import com.google.common.collect.Sets;
import org.gradle.api.GradleException;

import java.util.Set;

public class DefaultRuntimeClasspathNormalization implements RuntimeClasspathNormalizationInternal {
    private final Set<String> ignores = Sets.newHashSet();
    private RuntimeClasspathNormalizationStrategy finalizedStrategy;

    @Override
    public synchronized void ignore(String pattern) {
        if (finalizedStrategy != null) {
            throw new GradleException("Cannot configure runtime classpath normalization after execution started.");
        }
        ignores.add(pattern);
    }

    @Override
    public synchronized RuntimeClasspathNormalizationStrategy buildFinalStrategy() {
        if (finalizedStrategy == null) {
            finalizedStrategy = new RuntimeClasspathNormalizationStrategy(ignores);
        }
        return finalizedStrategy;
    }
}
