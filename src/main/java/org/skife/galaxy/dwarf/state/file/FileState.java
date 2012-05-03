package org.skife.galaxy.dwarf.state.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.DeploymentStatus;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.State;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FileState implements State
{

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Path dataDirectory;

    public FileState(Path dataDirectory) throws IOException
    {
        this.dataDirectory = dataDirectory;
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
    }

    @Override
    public synchronized Set<Host> hosts()
    {
        try {
            Path hosts = dataDirectory.resolve("hosts");
            if (!hosts.toFile().exists()) {
                return Sets.newTreeSet();
            }


            Reader sup = Files.newBufferedReader(hosts, Charsets.UTF_8);
            Set<Host> rs = mapper.readValue(sup, new TypeReference<Set<Host>>()
            {
            });
            sup.close();
            return Sets.newTreeSet(rs);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private synchronized void _saveHosts(Set<Host> current)
    {
        Path hosts = dataDirectory.resolve("hosts");
        try {
            Path tmp = Files.createTempFile("dwarf", "tmp");
            mapper.writeValue(tmp.toFile(), current);
            Files.move(tmp, hosts, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }


    @Override
    public synchronized void add(Host host)
    {
        Set<Host> hosts = Sets.newTreeSet();
        hosts.add(host);
        hosts.addAll(hosts());
        _saveHosts(hosts);
    }

    @Override
    public synchronized void remove(Host host)
    {
        Set<Host> hosts = hosts();
        hosts.remove(host);
        _saveHosts(hosts);
    }

    @Override
    public synchronized Set<Deployment> deployments()
    {
        try {
            Path deployments = dataDirectory.resolve("deployments");
            if (!deployments.toFile().exists()) {
                return Sets.newTreeSet();
            }

            try (Reader sup = Files.newBufferedReader(deployments, Charsets.UTF_8)) {
                List<Deployment> rs = mapper.readValue(sup, new TypeReference<List<Deployment>>(){});
                return Sets.newTreeSet(rs);
            }

        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private synchronized void _saveDeployments(Set<Deployment> current)
    {
        Path hosts = dataDirectory.resolve("deployments");
        try {
            Path tmp = Files.createTempFile("dwarf", "tmp");
            mapper.writeValue(tmp.toFile(), current);
            Files.move(tmp, hosts, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }


    @Override
    public synchronized void save(Deployment d)
    {
        Set<Deployment> deployments = Sets.newTreeSet();
        deployments.add(d);
        deployments.addAll(deployments());
        _saveDeployments(deployments);

    }

    @Override
    public synchronized void saveDeploymentStatus(UUID deploymentId, DeploymentStatus status)
    {
        Path status_path = dataDirectory.resolve("deployment_status");
        Map<UUID, DeploymentStatus> stati = Maps.newHashMap();

        try {
            Splitter tab = Splitter.on("\t");
            if (Files.exists(status_path)) {
                for (String line : Files.readAllLines(status_path, Charsets.UTF_8)) {
                    Iterator<String> bits = tab.split(line).iterator();
                    stati.put(UUID.fromString(bits.next()), DeploymentStatus.valueOf(bits.next()));
                }
            }

            Path tmp = Files.createTempFile("dwarf", "tmp");
            stati.put(deploymentId, status);
            List<String> lines = Lists.newArrayListWithExpectedSize(stati.size());
            for (Map.Entry<UUID, DeploymentStatus> entry : stati.entrySet()) {
                lines.add(entry.getKey().toString() + "\t" + entry.getValue().name());
            }
            Files.write(tmp, lines, Charsets.UTF_8);
            Files.move(tmp, status_path, StandardCopyOption.REPLACE_EXISTING);

        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public synchronized DeploymentStatus statusFor(UUID deploymentId)
    {
        Path status_path = dataDirectory.resolve("deployment_status");
        Map<UUID, DeploymentStatus> stati = Maps.newHashMap();

        try {
            Splitter tab = Splitter.on("\t");
            if (Files.exists(status_path)) {
                for (String line : Files.readAllLines(status_path, Charsets.UTF_8)) {
                    Iterator<String> bits = tab.split(line).iterator();
                    stati.put(UUID.fromString(bits.next()), DeploymentStatus.valueOf(bits.next()));
                }
            }

            Path tmp = Files.createTempFile("dwarf", "tmp");
            return stati.get(deploymentId);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
