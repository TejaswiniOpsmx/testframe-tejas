package com.opsmx.oes.examples;

import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.artifacts.Base64ArtifactDefinition;
import com.opsmx.oes.pipelinebuilder.json.artifacts.ExpectedArtifact;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;
import java.util.Map;
import java.util.UUID;

public class ArtifactsPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "examples.artifacts";
    }

    @Override
    protected Pipeline buildPipeline(final String appName, final String pipelineName) {
        ExpectedArtifact inlineArtifact =
            ExpectedArtifact.builder()
                .id(computeStableIdForArtifact("kubernetes-manifest")) // stable ID
                .displayName("manifest.yml")    // visible in the UI
                .useDefaultArtifact(true)       // will use the base-64 definition we provide
                .usePriorArtifact(false)
                .defaultArtifact(Base64ArtifactDefinition.builder()
                    .name("manifest.yml")
                    .id(UUID.randomUUID().toString())
                    .contents(getResourceContents(getClass().getClassLoader(), // read the file contents from the JAR's resources
                        "embeddedd-resources/manifest.yml"))
                    .build())
                .matchArtifact(Base64ArtifactDefinition.builder() // the artifact we try to match: not something that actually exists
                    .name("no-such-artifact")
                    .build())
                .build();

        Stage declareArtifactStage = Stage.builder()
            .type(StageTypes.FIND_ARTIFACT_FROM_EXECUTION)
            .name("Declare artifact containing a manifest")
            .context(Map.of("application", getApplication(),
                "pipeline", computePipelineIdForClass(this.getClass()), // self-reference
                "executionOptions", Map.of("successful", true)))
            .expectedArtifact(inlineArtifact) // expected artifact is attached here
            .build();

        return Pipeline.builder()
            .name("Artifacts demo")
            .stage(declareArtifactStage)
            .build();
    }
}
