package com.opsmx.oes.examples;

import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.contexts.EvalVarsContext;
import com.opsmx.oes.pipelinebuilder.json.stages.model.EvaluateVariable;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;
import java.util.List;

public class FailureStrategiesPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "examples.failurestrategies";
    }

    @Override
    protected Pipeline buildPipeline(final String appName, final String pipelineName) {
        Stage evalVarsStage = Stage.builder()
                .type(StageTypes.EVALUATE_VARIABLES)
            .name("Show pipeline trigger")
            .contextObject(EvalVarsContext.ofVariables(List.of(
                new EvaluateVariable("foo", "123"))))
            .failPipeline(false)
            .completeOtherBranchesThenFail(true)
            .continuePipeline(false)
            .build();

        return Pipeline.builder()
            .name("Failure strategies")
            .stages(List.of(evalVarsStage))
            .build();
    }
}
