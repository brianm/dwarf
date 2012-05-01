package org.skife.galaxy.dwarf;

import org.skife.galaxy.dwarf.state.Deployment;
import org.skife.galaxy.dwarf.state.Host;
import org.skife.galaxy.dwarf.state.State;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Dwarf
{
    private final State state;
    private final String deployRoot;

    public Dwarf(State state, String deployRoot)
    {
        this.state = state;
        this.deployRoot = deployRoot;
    }

    public void addHost(Host host)
    {
        state.add(host);
    }

    public Set<Host> getHosts()
    {
        return state.hosts();
    }

    public void deploy(Host h, URI bundle, String name) throws IOException
    {
        Deployment d = Deployment.deploy(h, Paths.get(deployRoot), bundle, name);
        state.add(d);
    }
}
