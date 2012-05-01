package org.skife.galaxy.dwarf.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = {"search", "list", "find", "ls"})
public class Search implements Callable<Void>
{
    private static final ObjectMapper mapper = new ObjectMapper();

    @Arguments
    public List<String> args = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));

        if (args.isEmpty()) {
            // show all
            for (Deployment deployment : state.deployments()) {
                System.out.println(mapper.writeValueAsString(deployment));
            }
        }
        else {
            throw new UnsupportedOperationException("Not Yet Implemented!");
        }
        return null;
    }
}
