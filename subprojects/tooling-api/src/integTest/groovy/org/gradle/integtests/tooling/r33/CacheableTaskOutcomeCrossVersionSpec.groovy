/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.integtests.tooling.r33

import org.gradle.integtests.tooling.fixture.ProgressEvents
import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.events.OperationType
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.task.TaskSuccessResult

class CacheableTaskOutcomeCrossVersionSpec extends ToolingApiSpecification {
    def setup() {
        buildFile << """
            apply plugin: 'base'

            task cacheable {
                def outputFile = new File(buildDir, "output")
                inputs.file("input")
                outputs.file(outputFile)
                outputs.cacheIf { true }

                doLast {
                    outputFile.parentFile.mkdirs()
                    outputFile.text = "done"
                }
            }
"""
        file("gradle.properties") << """
            org.gradle.cache.tasks=true
"""
        file("input").text = "input file"
    }

    @ToolingApiVersion('>=3.3')
    @TargetGradleVersion('>=3.3')
    def "cacheable task is reported as FROM_CACHE"() {
        when:
        def pushToCacheEvents = new ProgressEvents()
        runCacheableBuild(pushToCacheEvents)
        then:
        !cacheableTaskResult(pushToCacheEvents).fromCache
        !cacheableTaskResult(pushToCacheEvents).upToDate

        when:
        file("build").deleteDir()
        and:
        def pullFromCacheResults = new ProgressEvents()
        runCacheableBuild(pullFromCacheResults)
        then:
        cacheableTaskResult(pullFromCacheResults).fromCache
        cacheableTaskResult(pullFromCacheResults).upToDate
    }

    @ToolingApiVersion('<3.3')
    @TargetGradleVersion('>=3.3')
    def "cacheable task is reported as UP-TO-DATE on older TAPI versions"() {
        when:
        def pushToCacheEvents = new ProgressEvents()
        runCacheableBuild(pushToCacheEvents)
        then:
        !cacheableTaskResult(pushToCacheEvents).upToDate

        when:
        file("build").deleteDir()
        and:
        def pullFromCacheResults = new ProgressEvents()
        runCacheableBuild(pullFromCacheResults)
        then:
        cacheableTaskResult(pullFromCacheResults).upToDate
    }

    private TaskSuccessResult cacheableTaskResult(ProgressEvents events) {
        events.operations.size() == 1
        (TaskSuccessResult)events.operations[0].result
    }

    private void runCacheableBuild(ProgressListener pullFromCacheResults) {
        withConnection {
            ProjectConnection connection ->
                connection.newBuild().forTasks('cacheable').addProgressListener(pullFromCacheResults, EnumSet.of(OperationType.TASK)).run()
        }
    }
}