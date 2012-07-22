package org.skife.galaxy.dwarf;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

public class DeploymentDescriptor
{
    private final Host host;
    private final URI bundle;
    private final Map<Path, URI> pathURIMap;
    private final String name;

    public DeploymentDescriptor(Host host, URI bundle, Map<Path, URI> pathURIMap, String name)
    {
        this.host = host;
        this.bundle = bundle;
        this.pathURIMap = pathURIMap;
        this.name = name;
    }

    public Host getHost()
    {
        return host;
    }

    public URI getBundle()
    {
        return bundle;
    }

    public Map<Path, URI> getPathURIMap()
    {
        return pathURIMap;
    }

    public String getName()
    {
        return name;
    }
}
