package org.skife.galaxy.dwarf.cli;

import com.google.common.collect.Lists;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.state.Host;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;


@Command(name = "add")
public class HostAdd implements Callable<Void>
{

    @Arguments(title = "host")
    public List<String> hosts = Lists.newArrayList();

    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, "/tmp/dwarf");
        for (String host : hosts) {
            d.addHost(new Host(host));
        }
        return null;
    }
}
