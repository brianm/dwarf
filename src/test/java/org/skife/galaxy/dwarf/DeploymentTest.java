package org.skife.galaxy.dwarf;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.fest.assertions.Assertions.assertThat;

public class DeploymentTest
{
    @Test
    public void testFoo() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(new Host("localhost"),
                                         tmpdir,
                                         Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                         "test deployment");

        Path control = tmpdir.resolve(d.getId().toString()).resolve("deploy").resolve("bin").resolve("control");

        assertThat(Files.exists(control)).isTrue();
        assertThat(Files.isExecutable(control)).isTrue();
    }
}
