package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.skife.ssh.Muxer;
import org.skife.ssh.ProcessResult;
import org.skife.ssh.SSH;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.io.CharStreams.readLines;

public class Deployment implements Comparable<Deployment>
{
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.registerModule(new SimpleModule().addSerializer(Path.class, new ToStringSerializer())
                                                .addKeySerializer(Path.class, new JsonSerializer<Path>() {

                                                    @Override
                                                    public void serialize(Path value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
                                                    {
                                                        jgen.writeFieldName(value.toString());
                                                    }
                                                })
                                                .addDeserializer(Path.class, new JsonDeserializer<Path>()
                                                {

                                                    @Override
                                                    public Path deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
                                                    {
                                                        return Paths.get(jp.nextTextValue());
                                                    }
                                                })

                                                .addKeyDeserializer(Path.class, new KeyDeserializer()
                                                {

                                                    @Override
                                                    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException
                                                    {
                                                        return Paths.get(key);
                                                    }
                                                }));


    }

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
                                    Path rootOnHost,
                                    DeployManifest descriptor) throws IOException
    {
        UUID id = UUID.randomUUID();

        Path bundle = UriBox.copyLocally(descriptor.getBundle());
        Path json_descriptor = Files.createTempFile("foo", ".json");
        try (OutputStream out = Files.newOutputStream(json_descriptor)) {
            mapper.writeValue(out, descriptor);
        }

        try (Muxer m = Muxer.withSocketsInTempDir()) {
            final Path deploy_dir = rootOnHost.resolve(id.toString());

            SSH ssh = m.connect(descriptor.getHost().getHostname());
            if (sshConfig.isPresent()) {
                ssh = ssh.withConfigFile(sshConfig.get().toFile());
            }

            Path expanded = deploy_dir.resolve("expand");
            ssh.exec("mkdir", "-p", deploy_dir.toString()).errorUnlessExitIn(0);
            ssh.scp(json_descriptor.toFile(), deploy_dir.resolve("manifest.json").toFile());

            ssh.scp(bundle.toFile(), deploy_dir.resolve("bundle.tar.gz").toFile()).errorUnlessExitIn(0);
            ssh.exec("mkdir", "-p", expanded.toString()).errorUnlessExitIn(0);
            ssh.exec("tar", "-C", expanded.toString(),
                     "-zxf", deploy_dir.resolve("bundle.tar.gz").toString()).errorUnlessExitIn(0);

            List<String> lines = readLines(ssh.exec("ls", expanded.toString())
                                              .errorUnlessExitIn(0)
                                              .getStdoutSupplier());
            if (lines.size() != 1) {
                // grotty tarball, more then one dir at root, cleanup and fail
                ssh.exec("rm", "-rf", deploy_dir.toString()).errorUnlessExitIn(0);
                throw new IllegalStateException("Bundle " + descriptor.getBundle() +
                                                " had too many files " + lines.toString() + " in root");
            }

            Path app_root = expanded.resolve(lines.get(0));

            // copy config files into the right place
            for (Map.Entry<Path, URI> entry : descriptor.getConfig().entrySet()) {
                Path local = UriBox.copyLocally(entry.getValue());
                Path remote = app_root.resolve(
                    entry.getKey().startsWith("/") ? Paths.get("." + entry.getKey()) : entry.getKey()
                );

                // if the dir in which to put the foncig does not exist, make it
                Path remote_parent = remote.getParent();
                int parent_exists = ssh.exec("test", "-d", remote_parent.toString()).getExitCode();
                if (parent_exists != 0) {
                    ssh.exec("mkdir", "-p", remote_parent.toString());
                }

                ssh.scp(local.toFile(), remote.toFile()).errorUnlessExitIn(0);
            }

            ssh.exec("mv",
                     deploy_dir.resolve("expand").resolve(lines.get(0)).toString(),
                     deploy_dir.resolve("deploy").toString()).errorUnlessExitIn(0);

            // clean up lingering expansion directory
            ssh.exec("rm", "-rf", expanded.toString());

            // TODO store deployment state so it can be discovered

            return new Deployment(id,
                                  deploy_dir.toString(),
                                  descriptor.getHost().getHostname(),
                                  descriptor.getName());
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
