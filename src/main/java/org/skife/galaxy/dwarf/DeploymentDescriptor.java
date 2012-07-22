package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DeploymentDescriptor
{
    private final URI              bundle;
    private final String           name;
    private final Map<String, URI> config;

    @JsonCreator
    public DeploymentDescriptor(@JsonProperty("bundle") URI bundle,
                                @JsonProperty("name") String name,
                                @JsonProperty("config") Map<String, URI> config)
    {
        this.bundle = bundle;
        this.name = name;
        this.config = config;
    }

    public URI getBundle()
    {
        return bundle;
    }

    public Map<Path, URI> getConfig(URI baseUri)
    {
        ImmutableMap.Builder<Path, URI> builder = ImmutableMap.builder();
        for (Map.Entry<String, URI> entry : config.entrySet()) {
            builder.put(Paths.get(entry.getKey()), baseUri.resolve(entry.getValue()));
        }
        return builder.build();
    }

    public String getName()
    {
        return name;
    }
}
