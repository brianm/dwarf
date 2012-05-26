package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name="remove", description = "Remove a host")
public class HostRemove implements Callable<Void>
{
    @Arguments(title = "host")
    public List<String> hosts = Lists.newArrayList();

    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, "/tmp/dwarf", Optional.<Path>absent());
        for (String host : hosts) {
            d.removeHost(new Host(host));
        }
        return null;
    }

}
