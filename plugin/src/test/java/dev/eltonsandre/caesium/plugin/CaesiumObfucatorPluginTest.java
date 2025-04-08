package dev.eltonsandre.caesium.plugin;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CaesiumObfucatorPluginTest {

    @Test
    public void greetingTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("caesium.obfuscator");

        Assertions.assertTrue(project.getPluginManager()
                .hasPlugin("caesium.obfuscator"));

        Assertions.assertNotNull(project.getTasks().getByName("caesium"));
    }

}