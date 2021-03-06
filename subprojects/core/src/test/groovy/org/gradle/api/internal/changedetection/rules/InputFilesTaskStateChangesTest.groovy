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

package org.gradle.api.internal.changedetection.rules

import org.gradle.api.UncheckedIOException
import org.gradle.api.internal.changedetection.state.FileCollectionSnapshotter
import org.gradle.api.internal.changedetection.state.FileCollectionSnapshotterRegistry
import org.gradle.api.internal.changedetection.state.GenericFileCollectionSnapshotter
import org.gradle.api.internal.changedetection.state.TaskExecution
import org.gradle.api.resources.normalization.internal.ResourceNormalizationStrategies
import spock.lang.Issue
import spock.lang.Subject

@Subject(InputFilesTaskStateChanges)
class InputFilesTaskStateChangesTest extends AbstractTaskStateChangesTest {
    def normalizationStrategies = ResourceNormalizationStrategies.NOT_CONFIGURED

    @Issue("https://issues.gradle.org/browse/GRADLE-2967")
    def "constructor adds context when input snapshot throws UncheckedIOException" () {
        setup:
        def cause = new UncheckedIOException("thrown from stub")
        def mockInputFileSnapshotter = Mock(FileCollectionSnapshotter)
        def mockInputFileSnapshotterRegistry = Mock(FileCollectionSnapshotterRegistry)

        when:
        new InputFilesTaskStateChanges(Mock(TaskExecution), Mock(TaskExecution), stubTask, mockInputFileSnapshotterRegistry, normalizationStrategies)

        then:
        1 * mockInputFileSnapshotterRegistry.getSnapshotter(GenericFileCollectionSnapshotter) >> mockInputFileSnapshotter
        1 * mockInputs.getFileProperties() >> fileProperties(prop: "a")
        1 * mockInputFileSnapshotter.snapshot(_, _, _, _) >> { throw cause }
        0 * _

        def e = thrown(UncheckedIOException)
        e.message.contains(stubTask.getName())
        e.message.contains("up-to-date")
        e.message.contains("input")
        e.cause == cause
    }
}
