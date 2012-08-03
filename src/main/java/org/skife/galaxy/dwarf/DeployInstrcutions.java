package org.skife.galaxy.dwarf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class DeployInstrcutions
{
    private final Host           host;
    private final URI            bundle;
    private final Map<Path, URI> pathURIMap;
    private final String         name;

    public DeployInstrcutions(Host host, URI bundle, Map<Path, URI> pathURIMap, String name)
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

    public static DeployInstrcutions figureItOut(Host host,
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
                return new DeployInstrcutions(host,
                                                  uriForSomething.resolve(dd.getBundle()),
                                                  dd.getConfig(uriForSomething),
                                                  name.or(dd.getName()));
            }

        }
        else /* tarball */
        {
            return new DeployInstrcutions(host,
                                              uriForSomething,
                                              Collections.<Path, URI>emptyMap(),
                                              name.or("Deployment Without a Name"));
        }
    }
}
