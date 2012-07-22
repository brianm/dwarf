package org.skife.galaxy.dwarf;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DeploymentTest
{
    @Test
    public void testHappyPath() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         tmpdir,
                                         new DeploymentDescriptor(new Host("localhost"),
                                                                  Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                                                  Collections.<Path, URI>emptyMap(),
                                                                  "test deployment"));

        Path control = tmpdir.resolve(d.getId().toString()).resolve("deploy").resolve("bin").resolve("control");

        assertThat(Files.exists(control)).isTrue();
        assertThat(Files.isExecutable(control)).isTrue();
        FileHelper.deleteRecursively(tmpdir);
    }

    @Test(expected = FileNotFoundException.class)
    public void testBundleDoesNotExist() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");

        try {
            Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                             tmpdir,
                                             new DeploymentDescriptor(
                                                 new Host("localhost"),
                                                 Paths.get("i-do-not-exist").toUri(),
                                                 Collections.<Path, URI>emptyMap(),
                                                 "test deployment"));

        }
        finally {
            FileHelper.deleteRecursively(tmpdir);
        }
    }

    @Test
    public void testMalformedBundleTooManyDirs() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmpdir)) {
            assertThat(stream.iterator().hasNext()).isFalse();
        }

        try {
            Deployment.deploy(Optional.<Path>absent(),
                              tmpdir,
                              new DeploymentDescriptor(
                                  new Host("localhost"),
                                  Paths.get("src/test/resources/malformed.tar.gz").toUri(),
                                  Collections.<Path, URI>emptyMap(),
                                  "test deployment"));
            fail("should have raised an exception");
        }
        catch (Exception e) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmpdir)) {
                assertThat(stream.iterator().hasNext()).isFalse();
            }
        }

        FileHelper.deleteRecursively(tmpdir);

    }

    @Test
    public void testStart() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         tmpdir,
                                         new DeploymentDescriptor(
                                             new Host("localhost"),
                                             Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                             Collections.<Path, URI>emptyMap(),
                                             "test deployment"));

        d.start(Optional.<Path>absent());

        Path pidish = tmpdir.resolve(d.getId().toString()).resolve("deploy/running");
        assertThat(Files.exists(pidish)).isTrue();


        FileHelper.deleteRecursively(tmpdir);

    }

    @Test
    public void testStop() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         tmpdir,
                                         new DeploymentDescriptor(
                                             new Host("localhost"),
                                             Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                             Collections.<Path, URI>emptyMap(),
                                             "test deployment"));

        d.start(Optional.<Path>absent());

        Path pidish = tmpdir.resolve(d.getId().toString()).resolve("deploy/running");
        assertThat(Files.exists(pidish)).isTrue();

        d.stop(Optional.<Path>absent());
        assertThat(Files.exists(pidish)).isFalse();

        FileHelper.deleteRecursively(tmpdir);
    }

    @Test
    public void testStatus() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         tmpdir,
                                         new DeploymentDescriptor(
                                             new Host("localhost"),
                                             Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                             Collections.<Path, URI>emptyMap(),
                                             "test deployment"));

        d.start(Optional.<Path>absent());
        assertThat(d.status(Optional.<Path>absent())).isEqualTo(DeploymentStatus.running);             rt

        d.stop(Optional.<Path>absent());
        assertThat(d.status(Optional.<Path>absent())).isEqualTo(DeploymentStatus.stopped);

        FileHelper.deleteRecursively(tmpdir);
    }


    @Test
    public void testDeploymentWithConfig() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Map<Path, URI> config = ImmutableMap.of(Paths.get("/etc/runtime.properties"),
                                                Paths.get("src/test/resources/runtime.properties").toUri());
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         tmpdir,
                                         new DeploymentDescriptor(new Host("localhost"),
                                                                  Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                                                  config,
                                                                  "test deployment"));

        Path cfg = Paths.get(d.getDirectory()).resolve("deploy/etc/runtime.properties");
        assertThat(Files.exists(cfg)).isTrue();

    }
}
