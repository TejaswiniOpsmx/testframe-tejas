/*
 * Copyright 2023 Apple, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.oes.pipelinebuilder.json.triggers;

import java.util.List;
import java.util.Objects;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Actions on github repository will trigger a pipeline.
 * @see <a href="https://spinnaker.io/docs/guides/user/pipeline/triggers/github/">Github</a>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class GitTrigger extends Trigger {
    private final TriggerType type = TriggerType.GIT;

    /** Determines the source eg. github, stash, bitbucket, gitlab */
    private final String source;
    /** Determines the source branch */
    private final String branch;
    /** Determines the source id */
    private final String project;
    /** Determines the source repository */
    private final String slug;

    @Builder
    public GitTrigger(String id, String source, String branch, String project, String slug,
                      Boolean enabled, String runAsUser, List<String> expectedArtifactIds) {
        super(id, enabled, runAsUser, expectedArtifactIds);
        this.source = Objects.requireNonNull(source);
        this.branch = Objects.requireNonNull(branch);
        this.project = Objects.requireNonNull(project);
        this.slug = Objects.requireNonNull(slug);
    }
}
