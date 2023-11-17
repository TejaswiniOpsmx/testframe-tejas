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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * Spinnaker will poll a jenkins account for any updates to a jenkins job, and
 * if there are, it will pull those down.
 *
 * To enable this feature in Spinnaker, the --track-digests flag must
 * be set to true.
 * @see <a href="https://spinnaker.io/docs/reference/halyard/commands/#hal-config-ci-jenkins">Spinnaker Parameters</a>
 */
@Getter
@JsonInclude(value = Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class JenkinsTrigger extends Trigger {
    private final TriggerType type = TriggerType.JENKINS;
    /** Jenkins account to look into */
    private final String master;
    /** The jenkins job to look into. */
    private final String job;

    @Builder
    public JenkinsTrigger(String id, String runAsUser, Boolean enabled, List<String> expectedArtifactIds, // parent parameters first
                          String master, String job) {
        super(id, enabled, runAsUser, expectedArtifactIds);

        this.master = Objects.requireNonNull(master, "Jenkins trigger needs a master");
        this.job = Objects.requireNonNull(job, "Jenkins trigger needs a job");
    }
}
