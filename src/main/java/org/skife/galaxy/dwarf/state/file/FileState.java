package org.skife.galaxy.dwarf.state.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.State;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class FileState implements State
{

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

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
            Set<Host> rs = mapper.readValue(sup, new TypeReference<Set<Host>>(){});
            sup.close();
            return Sets.newTreeSet(rs) ;
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private synchronized void _saveHosts(Set<Host> current) {
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

            Reader sup = Files.newBufferedReader(deployments, Charsets.UTF_8);
            Set<Deployment> rs = mapper.readValue(sup, new TypeReference<Set<Deployment>>(){});
            sup.close();
            return Sets.newTreeSet(rs) ;
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private synchronized void _saveDeployments(Set<Deployment> current) {
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
    public synchronized void add(Deployment d)
    {
        Set<Deployment> deployments = Sets.newTreeSet();
        deployments.add(d);
        deployments.addAll(deployments());
        _saveDeployments(deployments);

    }
}
