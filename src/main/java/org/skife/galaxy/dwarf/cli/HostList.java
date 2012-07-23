package org.skife.galaxy.dwarf.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.Host;
import org.skife.galaxy.dwarf.cli.util.TabularRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name={"list", "ls"})
public class HostList implements Callable<Void>
{
    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, "/tmp/dwarf", Optional.<Path>absent());

        List<List<String>> table = Lists.newArrayList();
        for (Host host : d.getHosts()) {
            table.add(ImmutableList.of(host.getHostname()));
        }
        TabularRenderer.render(table);
        return null;
    }
}
