package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.dwarf.DeployInstrcutions;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.cli.util.DeploymentRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "deploy")
public class Deploy implements Callable<Void>
{
    @Inject
    public GlobalOptions global = new GlobalOptions();

    @Option(name = "--name", title = "name")
    public String name; // = "Someone forgot to name me";

    @Option(name = "--host", required = true)
    public String host;

    @Arguments(title = "uri", required = true)
    public URI bundle;

    @Option(name = {"--template-var", "-t"})
    public List<String> pairs = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        URI base = URI.create("file://" + new File(".").getAbsolutePath());
        bundle = base.resolve(bundle);

        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, global.getDeployRoot(), global.getSshConfig());

        final String hostPrefix = this.host;

        Host host = Iterables.find(d.getHosts(), new Predicate<Host>()
        {
            @Override
            public boolean apply(Host input)
            {
                return input.getHostname().startsWith(hostPrefix);
            }
        });

        Map<String, String> props = Maps.newLinkedHashMap();
        Splitter eqs = Splitter.on("=").trimResults();
        for (String pair : pairs) {
            Iterator<String> it = eqs.split(pair).iterator();
            props.put(it.next(), it.next());
        }

        DeployInstrcutions dd = DeployInstrcutions.figureItOut(host, bundle, Optional.fromNullable(name), props);

        Deployment dep = d.deploy(dd);

        new DeploymentRenderer(ImmutableSet.of(dep), state).render();

        return null;
    }
}
