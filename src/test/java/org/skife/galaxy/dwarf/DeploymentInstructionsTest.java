package org.skife.galaxy.dwarf;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;

public class DeploymentInstructionsTest
{
    @Test
    public void testFigureItOutWithTarbell() throws Exception
    {
        URI bundle = Paths.get("src/test/resources/echo.tar.gz").toUri();
        DeploymentInstructions dd = DeploymentInstructions.figureItOut(new Host("localhost"),
                                                                       bundle,
                                                                       Optional.of("thingamajig"),
                                                                       Collections.<String, String>emptyMap());
        assertThat(dd.getName()).isEqualTo("thingamajig");
        assertThat(dd.getBundle()).isEqualTo(bundle);
    }

    @Test
    public void testFigureItOutWithYaml() throws Exception
    {
        URI bundle = Paths.get("src/test/resources/echo.tar.gz").toUri();
        URI yaml = Paths.get("src/test/resources/echo-descriptor.yml").toUri();
        DeploymentInstructions dd = DeploymentInstructions.figureItOut(new Host("localhost"),
                                                                       yaml,
                                                                       Optional.<String>absent(),
                                                                       Collections.<String, String>emptyMap());
        assertThat(dd.getName()).isEqualTo("Echo Server");
        assertThat(UriBox.BASE.resolve(dd.getBundle())).isEqualTo(bundle);
        assertThat(dd.getConfig()).isEqualTo(ImmutableMap.of(Paths.get("/etc/runtime.properties"),
                                                             yaml.resolve(new URI("runtime.properties"))));
    }

    @Test
    public void testFigureItOutWithYamlOverrideName() throws Exception
    {
        URI bundle = Paths.get("src/test/resources/echo.tar.gz").toUri();
        URI yaml = Paths.get("src/test/resources/echo-descriptor.yml").toUri();
        DeploymentInstructions dd = DeploymentInstructions.figureItOut(new Host("localhost"),
                                                                       yaml,
                                                                       Optional.of("thingamajig"),
                                                                       Collections.<String, String>emptyMap());
        assertThat(dd.getName()).isEqualTo("thingamajig");
        assertThat(UriBox.BASE.resolve(dd.getBundle())).isEqualTo(bundle);
        assertThat(dd.getConfig()).isEqualTo(ImmutableMap.of(Paths.get("/etc/runtime.properties"),
                                                             yaml.resolve(new URI("runtime.properties"))));
    }
}
