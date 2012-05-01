package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.cli.OptionType;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "deploy")
public class Deploy implements Callable<Void>
{
    @Option(name={"-d", "--deploy-root"})
    public String deployRoot = "/tmp/dwarf";

    @Option(name = "--name", title = "name")
    public String name = "Someone forgot to name me";


    @Option(name = "--host", required = true, type = OptionType.GLOBAL)
    public String host;

    @Arguments(title = "bundle-url", required = true)
    public URI bundle;

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, deployRoot);
        final String host = this.host;

        Host h = Iterables.find(d.getHosts(), new Predicate<Host>()
        {
            @Override
            public boolean apply(Host input)
            {
                return input.getHostname().startsWith(host);
            }
        });

        d.deploy(h, bundle , name);

        return null;
    }
}
