package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.skife.ssh.Muxer;
import org.skife.ssh.ProcessResult;
import org.skife.ssh.SSH;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static com.google.common.io.CharStreams.readLines;

public class Deployment implements Comparable<Deployment>
{
    private final UUID id;
    private final String directory;
    private final String name;
    private final String host;

    @JsonCreator
    public Deployment(@JsonProperty("id") UUID id,
                      @JsonProperty("directory") String directory,
                      @JsonProperty("host") String host,
                      @JsonProperty("name") String name)
    {
        this.id = id;
        this.host = host;
        this.directory = directory;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public UUID getId()
    {
        return id;
    }

    public String getDirectory()
    {
        return directory;
    }

    public String getHost()
    {
        return host;
    }

    @Override
    public boolean equals(Object other)
    {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static Deployment deploy(Optional<Path> sshConfig,
                                    Host host,
                                    Path rootOnHost,
                                    URI bundle,
                                    String name) throws IOException
    {
        UUID id = UUID.randomUUID();

        Path bundle_tmp = Files.createTempFile("bundle", ".tmp");
        try (InputStream in = bundle.toURL().openStream()) {
            Files.copy(in, bundle_tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        try (Muxer m = Muxer.withSocketsInTempDir()) {
            final Path work_dir = rootOnHost.resolve(id.toString());

            SSH ssh = m.connect(host.getHostname());
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }

            ssh.exec("mkdir", "-p", work_dir.toString()).errorUnlessExitIn(0);
            ssh.scp(bundle_tmp.toFile(), work_dir.resolve("bundle.tar.gz").toFile()).errorUnlessExitIn(0);
            ssh.exec("mkdir", "-p", work_dir.resolve("expand").toString()).errorUnlessExitIn(0);
            ssh.exec("tar", "-C", work_dir.resolve("expand").toString(),
                     "-zxf", work_dir.resolve("bundle.tar.gz").toString()).errorUnlessExitIn(0);

            List<String> lines = readLines(ssh.exec("ls", work_dir.resolve("expand").toString())
                                              .errorUnlessExitIn(0)
                                              .getStdoutSupplier());
            if (lines.size() != 1) {
                // grotty tarball, more then one dir at root, cleanup and fail
                ssh.exec("rm", "-rf", work_dir.toString()).errorUnlessExitIn(0);
                throw new IllegalStateException("Bundle " + bundle +
                                                " had too many files " + lines.toString() + " in root");
            }
            ssh.exec("mv",
                     work_dir.resolve("expand").resolve(lines.get(0)).toString(),
                     work_dir.resolve("deploy").toString()).errorUnlessExitIn(0);

            return new Deployment(id, work_dir.toString(), host.getHostname(), name);

        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void start(Optional<Path> sshConfig)
    {
        try {
            SSH ssh = SSH.toHost(host);
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }
            Path control = Paths.get(directory).resolve("deploy").resolve("bin").resolve("control");
            ssh.exec(control.toString(), "start").errorUnlessExitIn(0);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int compareTo(Deployment o)
    {
        return (this.getHost() + " " + this.getId().toString()).compareTo((o.getHost() + " " + o.getId().toString()));
    }

    public void stop(Optional<Path> sshConfig)
    {
        try {
            SSH ssh = SSH.toHost(host);
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }
            Path control = Paths.get(directory).resolve("deploy").resolve("bin").resolve("control");
            ssh.exec(control.toString(), "stop").errorUnlessExitIn(0);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void clear(Optional<Path> sshConfig)
    {
        try {
            SSH ssh = SSH.toHost(host);
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }
            ssh.exec("rm", "-rf", Paths.get(directory).toAbsolutePath().toString()).errorUnlessExitIn(0);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public DeploymentStatus status(Optional<Path> sshConfig)
    {
        try {
            SSH ssh = SSH.toHost(host);
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }
            Path control = Paths.get(directory).resolve("deploy").resolve("bin").resolve("control");
            ProcessResult pr = ssh.exec(control.toString(), "status");

            switch (pr.getExitCode()) {
                case 0:
                    return DeploymentStatus.running;
                case 1:
                    return DeploymentStatus.dead;
                case 2:
                    return DeploymentStatus.dead;
                case 3:
                    return DeploymentStatus.stopped;
                default:
                    return DeploymentStatus.unknown;
            }

        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
