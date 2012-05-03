package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.cli.OptionType;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.cli.util.DeploymentRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "deploy")
public class Deploy implements Callable<Void>
{
    @Option(name = {"-d", "--deploy-root"},
            title = "path",
            description = "Root path for deployments on target host",
            configuration = "deploy_root")
    public String deployRoot = "/tmp/dwarf";

    @Option(name={"-C", "--ssh-config"},
            title="ssh_config file",
            description="SSH config file to use",
            configuration = "ssh_config")
    public String sshConfig = null;

    @Option(name = "--name", title = "name")
    public String name = "Someone forgot to name me";

    @Option(name = "--host", required = true)
    public String host;

    @Arguments(title = "bundle-url", required = true)
    public URI bundle;

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, deployRoot, Optional.fromNullable(sshConfig).transform(new Function<String, Path>()
        {
            @Override
            public Path apply(@Nullable String input)
            {
                return Paths.get(input);
            }
        }));

        final String host = this.host;

        Host h = Iterables.find(d.getHosts(), new Predicate<Host>()
        {
            @Override
            public boolean apply(Host input)
            {
                return input.getHostname().startsWith(host);
            }
        });

        Deployment dep = d.deploy(h, bundle, name);

        new DeploymentRenderer(ImmutableSet.of(dep), state).renderTsv(System.out);

        return null;
    }
}
