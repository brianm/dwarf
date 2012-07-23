package org.skife.galaxy.dwarf.cli;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.cli.util.DeploymentRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = {"search", "list", "find", "ls"})
public class Search implements Callable<Void>
{
    @Arguments
    public List<String> args = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));

        if (args.isEmpty()) {
            // show all
            new DeploymentRenderer(state.deployments(), state).render();
        }
        else {
            throw new UnsupportedOperationException("Not Yet Implemented!");
        }
        return null;
    }

    static Map<UUID, String> minuuid(Set<UUID> deps)
    {
        int length = -1;
        for (int i = 4; length < 0 && i < UUID.randomUUID().toString().length(); i++) {
            Set<String> prefixes = Sets.newHashSet();
            boolean collision = false;
            for (UUID dep : deps) {
                if (!prefixes.add(dep.toString().substring(0, i))) {
                    // already contained, bummer
                    collision = true;
                }
            }
            if (!collision) {
                length = i;
                break;
            }
        }

        Map<UUID, String> rs = Maps.newHashMap();
        for (UUID dep : deps) {
            rs.put(dep, dep.toString().substring(0, length));
        }

        return rs;
    }
}
