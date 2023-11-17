package com.opsmx.oes.examples;

import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.PipelineParameter;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;
import java.util.List;
import java.util.Map;

public class TutorialPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "examples.tutorial";
    }

    @Override
    protected Pipeline buildPipeline(final String appName, final String pipelineName) {
        Stage waitStage = Stage.builder()
            .type(StageTypes.WAIT)
            .name("Wait a moment")
            .context(Map.of("waitTime", 5))
            .build();

        Stage evalSumStage = Stage.builder()
            .type(StageTypes.EVALUATE_VARIABLES)
            .name("Evaluate sum")
            .parentStage(waitStage)
            .context(Map.of("variables", List.of(
                Map.of(
                    "key", "sum",
                    "value", "${ #toInt(parameters.a) + #toInt(parameters.b) }"))))
            .build();

        return Pipeline.builder()
            .parameters(List.of(
                PipelineParameter.builder()
                    .name("a")
                    .defaultValue("17")
                    .build(),
                PipelineParameter.builder()
                    .name("b")
                    .defaultValue("25")
                    .build()))
            .name("Tutorial")
            .stages(List.of(waitStage, evalSumStage))
            .build();
    }
}
