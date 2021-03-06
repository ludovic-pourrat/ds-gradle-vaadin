/*
 * Copyright 2018-2020 Devsoap Inc.
 *
 * Licensed under the Creative Commons Attribution-NoDerivatives 4.0
 * International Public License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      https://creativecommons.org/licenses/by-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devsoap.vaadinflow

import com.devsoap.spock.Smoke
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Ignore
import spock.lang.Unroll

/**
 * Smoke tests for supported Vaadin versions
 *
 * @author John Ahlroos
 * @since 1.0
 */
@Smoke
class VaadinVersionSmokeTest extends FunctionalTest {

    private static final List<String> LEGACY_VERSIONS = ['10.0.4', '11.0.0', '12.0.0', '13.0.0', '14.0.0']
    private static final List<String> VERSIONS = ['15.0.0']
    private static final String NEXT_VERSION = '15.0.0.alpha11'

    @Unroll
    void 'Test development mode with Vaadin #version'(String version) {
        setup:
        compatibilityMode = false
        vaadinVersion = version
        buildFile << '''
                    vaadin.autoconfigure()
                '''.stripIndent()
        run 'vaadinCreateProject'
        when:
        BuildResult result = run 'jar'
        BuildResult depInsightResult = run('dependencyInsight', '--dependency', 'com.vaadin:flow-server')
        then:
        result.task(':jar').outcome == TaskOutcome.SUCCESS
        depInsightResult.task(':dependencyInsight').outcome == TaskOutcome.SUCCESS
        depInsightResult.output.contains("com.vaadin:vaadin-core:$version")
        where:
        version << VERSIONS
    }

    @Unroll
    void 'Test development mode with legacy Vaadin #version'(String version) {
        setup:
            compatibilityMode = true
            vaadinVersion = version
            buildFile << '''
                    vaadin.autoconfigure()
                '''.stripIndent()
            run 'vaadinCreateProject'
        when:
            BuildResult result = run 'jar'
            BuildResult depInsightResult = run('dependencyInsight', '--dependency', 'com.vaadin:flow-server')
        then:
            result.task(':vaadinAssembleClient').outcome == TaskOutcome.SKIPPED
            result.task(':jar').outcome == TaskOutcome.SUCCESS
            depInsightResult.task(':dependencyInsight').outcome == TaskOutcome.SUCCESS
            depInsightResult.output.contains("com.vaadin:vaadin-core:$version")
        where:
            version << LEGACY_VERSIONS
    }

    @Unroll
    void 'Test production mode with legacy Vaadin #version'(String version) {
        setup:
            compatibilityMode = true
            vaadinVersion = version
            buildFile << '''
                    vaadin.productionMode = true
                    vaadin.autoconfigure()
                '''.stripIndent()
            run 'vaadinCreateProject'
        when:
            BuildResult result = run 'vaadinAssembleClient'
        then:
            result.task(':vaadinAssembleClient').outcome == TaskOutcome.SUCCESS
        where:
            version << LEGACY_VERSIONS
    }

    @Unroll
    void 'Test production mode with Vaadin #version'(String version) {
        setup:
        compatibilityMode = false
        vaadinVersion = version
        buildFile << '''
                    vaadin.productionMode = true
                    vaadin.autoconfigure()
                '''.stripIndent()
        run 'vaadinCreateProject'
        when:
        BuildResult result = run 'vaadinAssembleClient'
        then:
        result.task(':vaadinAssembleClient').outcome == TaskOutcome.SUCCESS
        where:
        version << VERSIONS
    }

    @Unroll
    void 'Test development mode with unsupported Vaadin #version'(String version) {
        setup:
            compatibilityMode = false
            vaadinVersion = version
            buildFile << '''
                repositories { vaadin.prereleases() }
                vaadin.unsupportedVersion = true
                vaadin.autoconfigure()
            '''.stripIndent()
            run 'vaadinCreateProject'
        when:
            BuildResult result = run 'jar'
            BuildResult depInsightResult = run('dependencyInsight', '--dependency', 'com.vaadin:flow-server')
        then:
            result.task(':jar').outcome == TaskOutcome.SUCCESS
            depInsightResult.task(':dependencyInsight').outcome == TaskOutcome.SUCCESS
            depInsightResult.output.contains("com.vaadin:vaadin-core:$version")
        where:
            version = NEXT_VERSION
    }

    @Unroll
    void 'Test production mode with unsupported Vaadin #version'(String version) {
        setup:
            compatibilityMode = false
            vaadinVersion = version
            buildFile << '''
                repositories { vaadin.prereleases() }
                vaadin.unsupportedVersion = true
                vaadin.productionMode = true
                vaadin.autoconfigure()
            '''.stripIndent()
            run 'vaadinCreateProject'
        when:
            BuildResult result = run 'vaadinAssembleClient'
        then:
            result.task(':vaadinAssembleClient').outcome == TaskOutcome.SUCCESS
        where:
            version = NEXT_VERSION
    }

}
