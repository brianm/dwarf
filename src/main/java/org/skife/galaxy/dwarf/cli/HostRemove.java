package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "remove", description = "Remove a host")
public class HostRemove implements Callable<Void>
{
    @Option(name = "--force", description = "force host removal even if there are deployments present")
    public boolean force = false;

    @Arguments(title = "host")
    public List<String> hosts = Lists.newArrayList();

    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, "/tmp/dwarf", Optional.<Path>absent());

        OUTER:
        for (String host : hosts) {

            for (Deployment deployment : state.deployments()) {
                if ((!force) && host.equalsIgnoreCase(deployment.getHost())) {
                    System.err.printf("refusing to remove %s -- it has a deployment\n", host);
                    continue OUTER;
                }
            }

            d.removeHost(new Host(host));
        }
        return null;
    }

}
