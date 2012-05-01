package org.skife.galaxy.dwarf.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.state.Host;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name={"list", "ls"})
public class HostList implements Callable<Void>
{
    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, "/tmp/dwarf");

        for (Host host : d.getHosts()) {
            System.out.println(mapper.writeValueAsString(host));
        }
        return null;
    }
}
