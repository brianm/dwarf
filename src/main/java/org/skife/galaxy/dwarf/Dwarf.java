package org.skife.galaxy.dwarf;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Dwarf
{
    private final State state;
    private final String deployRoot;
    private final Optional<Path> sshConfig;

    public Dwarf(State state, String deployRoot, Optional<Path> sshConfig)
    {
        this.state = state;
        this.deployRoot = deployRoot;
        this.sshConfig = sshConfig;
    }

    public void addHost(Host host)
    {
        state.add(host);
    }

    public void removeHost(Host host)
    {
        state.remove(host);
    }


    public Set<Host> getHosts()
    {
        return state.hosts();
    }

    public Deployment deploy(DeploymentDescriptor descriptor) throws IOException
    {
        Deployment d = Deployment.deploy(sshConfig,
                                         Paths.get(deployRoot),
                                         descriptor);
        state.save(d);
        state.saveDeploymentStatus(d.getId(), DeploymentStatus.stopped);
        return d;
    }

    public Set<Deployment> getDeployments() {
        return state.deployments();
    }

    public void start(Deployment d)
    {
        d.start(sshConfig);
        state.saveDeploymentStatus(d.getId(), DeploymentStatus.running);
    }

    public void clear(Deployment d)
    {
        stop(d);
        d.clear(sshConfig);
        state.remove(d);
    }

    public void stop(Deployment d)
    {
        d.stop(sshConfig);
        state.saveDeploymentStatus(d.getId(), DeploymentStatus.stopped);
    }

    public DeploymentStatus status(Deployment d)
    {
        DeploymentStatus statsu = d.status(sshConfig);
        state.saveDeploymentStatus(d.getId(), statsu);
        return statsu;
    }
}
