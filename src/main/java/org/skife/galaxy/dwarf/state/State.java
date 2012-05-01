package org.skife.galaxy.dwarf.state;

import java.util.Set;

public interface State
{
    public Set<Host> hosts();
    public void add(Host host);
    public void remove(Host host);

    public Set<Deployment> deployments();

    public void add(Deployment d);
//    public void remove(Deployment d);
}
