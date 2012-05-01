package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import org.skife.ssh.Muxer;
import org.skife.ssh.SSH;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static com.google.common.io.CharStreams.readLines;

public class Deployment implements Comparable<Deployment>
{

    private final UUID   id;
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

    public static Deployment deploy(Host host, Path rootOnHost, URI bundle, String name) throws IOException
    {
        UUID id = UUID.randomUUID();

        Path bundle_tmp = Files.createTempFile("bundle", ".tmp");
        try (InputStream in = bundle.toURL().openStream()) {
            Files.copy(in, bundle_tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        try (Muxer m = Muxer.withSocketsIn(Files.createTempDirectory("dwarf-ssh").toFile())) {
            final SSH ssh = m.connect(host.getHostname()).inheritStandardErr();
            final Path work_dir = rootOnHost.resolve(id.toString());
            ssh.exec("mkdir", "-p", work_dir.toString());
            ssh.scp(bundle_tmp.toFile(), work_dir.resolve("bundle.tar.gz").toFile());
            ssh.exec("mkdir", "-p", work_dir.resolve("expand").toString());
            ssh.exec("tar", "-C", work_dir.resolve("expand").toString(),
                     "-zxvf", work_dir.resolve("bundle.tar.gz").toString());
            List<String> lines = readLines(ssh.exec("ls", work_dir.resolve("expand").toString()).getStdoutSupplier());
            if (lines.size() != 1) {
                ssh.exec("rm", "-rf", work_dir.toString());
                throw new IllegalStateException("Bundle " + bundle +
                                                " had too many files " + lines.toString() + " in root");
            }
            ssh.exec("mv",
                     work_dir.resolve("expand").resolve(lines.get(0)).toString(),
                     work_dir.resolve("deploy").toString());

            return new Deployment(id, work_dir.toString(), host.getHostname(), name);

        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void start()
    {

    }

    @Override
    public int compareTo(Deployment o)
    {
        return (this.getHost() + " " + this.getId().toString()).compareTo((o.getHost() + " " + o.getId().toString()));
    }
}
