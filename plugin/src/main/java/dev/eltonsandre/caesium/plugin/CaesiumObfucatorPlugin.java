package dev.eltonsandre.caesium.plugin;

import dev.eltonsandre.caesium.CaesiumConfig;
import dev.eltonsandre.caesium.MutatorConfig;
import dev.eltonsandre.caesium.MutatorRunner;
import dev.eltonsandre.caesium.PreRuntime;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.util.Strings;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSetContainer;

public class CaesiumObfucatorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var config = project.getExtensions().create("config", CaesiumConfig.class);
        var mutator = project.getExtensions().create("mutator", MutatorConfig.class);

        var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        sourceSets.all(sourceSet -> System.out.println("customTaskFor" + sourceSet.getName()));

        Configuration testConfig = project.getConfigurations().create("testConfig");
        testConfig.getDependencies().all(dependency -> {
            project.getLogger().lifecycle(" " + dependency.getName());
        });

        var obfuscateTask = project.task("caesium")
                .doLast(task -> {
                    var dependencies = project.getConfigurations()
                            .getByName("implementation")
                            .getDependencies();
                    System.out.println("dependencies: " + dependencies);

                    PreRuntime.loadJavaRuntime(config.getJdkPath());

                    if (Strings.isBlank(config.getLoggerFile())) {
                        config.setLoggerFile("log/caesium.log");
                    }
                    MainMapLookup.setMainArguments("caesium", config.getLoggerFile());
                    config.setMutator(mutator);
                    MutatorRunner.run(config);
                });

        obfuscateTask.setGroup("obfuscate");
    }

}