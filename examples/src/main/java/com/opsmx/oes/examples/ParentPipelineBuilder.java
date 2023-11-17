package com.opsmx.oes.examples;

import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.contexts.RunPipelineContext;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

public class ParentPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "examples.parent";
    }

    @Override
    protected Pipeline buildPipeline(final String appName, final String pipelineName) {
        Stage runPipelineStage = Stage.builder()
            .type(StageTypes.PIPELINE)
            .name("Run child pipeline")
            .contextObject(RunPipelineContext.builder()
                .application(getApplication())
                .pipelineId(computePipelineIdForClass(TutorialPipelineBuilder.class))
                .build())
            .build();

        return Pipeline.builder()
            .name("Parent")
            .stage(runPipelineStage)
            .build();
    }
}
