package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.json.triggers.GitTrigger;
import com.opsmx.oes.pipelinebuilder.json.triggers.JenkinsTrigger;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.List;
import java.util.Map;

public class GitTriggerPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "examples.crontrigger";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {
        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");
        final String shortWaitStagePeriod = config.getConfigData("shortWaitStagePeriod");
        final String githubBranch = config.getConfigData("githubBranch");
        final String githubProject = config.getConfigData("githubProject");
        final String githubSlug = config.getConfigData("githubSlug");
        final String githubSource = config.getConfigData("githubSource");

        Stage waitStage = Stage.builder()
                .type(StageTypes.WAIT)
                .name("Wait")
                .continuePipeline(true)
                .context(Map.of("waitTime", shortWaitStagePeriod))
                .build();

        Pipeline pipeline = Pipeline.builder()
                                    .name(pipelineName)
                                    .stages(List.of(waitStage))
                                    .trigger(GitTrigger.builder()
                                                        .source(githubSource)
                                                        .branch(githubBranch)
                                                        .project(githubProject)
                                                        .slug(githubSlug)
                                                        .enabled(true)
                                                        .build())
                                    .build();
        pipeline.setApplication(appName);
        return pipeline;
    }
}
