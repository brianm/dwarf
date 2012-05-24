package org.skife.galaxy.dwarf;

import java.util.Set;
import java.util.UUID;

public interface State
{
    public Set<Host> hosts();
    public void add(Host host);
    public void remove(Host host);

    public Set<Deployment> deployments();

    public void save(Deployment d);
//    public void remove(Deployment d);

    void saveDeploymentStatus(UUID deploymentId, DeploymentStatus status);

    DeploymentStatus statusFor(UUID deploymentId);

    void remove(Deployment d);
}
