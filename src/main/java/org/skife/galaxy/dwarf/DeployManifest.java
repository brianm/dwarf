package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class DeployManifest
{
    private final Host host;
    private final URI bundle;
    private final Map<Path, URI> pathURIMap;
    private final String name;

    @JsonCreator
    public DeployManifest(@JsonProperty("host") Host host,
                          @JsonProperty("bundle") URI bundle,
                          @JsonProperty("config") Map<Path, URI> pathURIMap,
                          @JsonProperty("name") String name)
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

    public Map<Path, URI> getConfig()
    {
        return pathURIMap;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
     return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static DeployManifest figureItOut(Host host,
                                             URI uriForSomething,
                                             Optional<String> name,
                                             Map<String, String> props) throws IOException
    {
        Path thing = UriBox.copyLocally(uriForSomething);

        boolean yaml = true;
        try (InputStream in = Files.newInputStream(thing)) {
            if (in.read() == 0x1f && in.read() == 0x8b) {
                // has gzip magic bytes
                yaml = false;
            }
        }

        if (yaml) {
            try (InputStream in = Files.newInputStream(thing)) {
                String yml_raw = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
                String yml = new TemplateParser().parse(yml_raw, TemplateParser.createResolver(uriForSomething.toString(),
                                                                                               props));
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                DeploymentDescriptor dd = mapper.readValue(yml, DeploymentDescriptor.class);
                return new DeployManifest(host,
                                          uriForSomething.resolve(dd.getBundle()),
                                          dd.getConfig(uriForSomething),
                                          name.or(dd.getName()));
            }

        }
        else /* tarball */
        {
            return new DeployManifest(host,
                                      uriForSomething,
                                      Collections.<Path, URI>emptyMap(),
                                      name.or("Deployment Without a Name"));
        }
    }
}
