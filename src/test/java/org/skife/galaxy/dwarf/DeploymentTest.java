package org.skife.galaxy.dwarf;

import com.google.common.base.Optional;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DeploymentTest
{
    @Test
    public void testHappyPath() throws Exception
    {
        Path tmpdir = Files.createTempDirectory("dwarf-deploy");
        Deployment d = Deployment.deploy(Optional.<Path>absent(),
                                         new Host("localhost"),
                                         tmpdir,
                                         Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                         "test deployment");

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
                                             new Host("localhost"),
                                             tmpdir,
                                             Paths.get("i-do-not-exist").toUri(),
                                             "test deployment");

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
                              new Host("localhost"),
                              tmpdir,
                              Paths.get("src/test/resources/malformed.tar.gz").toUri(),
                              "test deployment");
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
                                         new Host("localhost"),
                                         tmpdir,
                                         Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                         "test deployment");

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
                                         new Host("localhost"),
                                         tmpdir,
                                         Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                         "test deployment");

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
                                         new Host("localhost"),
                                         tmpdir,
                                         Paths.get("src/test/resources/echo.tar.gz").toUri(),
                                         "test deployment");

        d.start(Optional.<Path>absent());
        assertThat(d.status(Optional.<Path>absent())).isEqualTo(DeploymentStatus.Running);

        d.stop(Optional.<Path>absent());
        assertThat(d.status(Optional.<Path>absent())).isEqualTo(DeploymentStatus.Stopped);


        FileHelper.deleteRecursively(tmpdir);
    }



}
