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

package springfox.documentation.service;

public class ApiInfo {

    public static final ApiInfo DEFAULT = new ApiInfo("Api Documentation", "Api Documentation", "1.0", "urn:tos",
            "Contact Email", "Creator", "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0");
    private final String version;
    private final String title;
    private final String description;
    private final String termsOfServiceUrl;
    private final String creatorName;
    private final String email;
    private final String license;
    private final String licenseUrl;

    public ApiInfo(String title, String description, String version, String termsOfServiceUrl, String email,
                   String creatorName, String license, String licenseUrl) {
        this.title = title;
        this.description = description;
        this.version = version;
        this.termsOfServiceUrl = termsOfServiceUrl;
        this.email = email;
        this.creatorName = creatorName;
        this.license = license;
        this.licenseUrl = licenseUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getLicense() {
        return license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String getVersion() {
        return version;
    }
}
