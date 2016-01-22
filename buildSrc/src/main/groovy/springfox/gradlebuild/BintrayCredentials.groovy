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

package springfox.gradlebuild

import org.gradle.api.Project

class BintrayCredentials {
    private final Project project

    BintrayCredentials(Project project) {
        this.project = project
    }

    String getUsername() {
        return project.hasProperty('bintrayUsername') ? project.property('bintrayUsername') : 'admin'
    }

    String getPassword() {
        return project.hasProperty('bintrayPassword') ? project.property('bintrayPassword') : 'password'
    }
}
