package org.skife.galaxy.dwarf.cli.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.State;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeploymentRenderer
{
    private final Iterable<Deployment> deployments;
    private final State                state;

    public DeploymentRenderer(Iterable<Deployment> deployments, State state)
    {
        this.deployments = deployments;
        this.state = state;
    }

    public void renderTsv(OutputStream out) throws IOException
    {
        Writer w = new OutputStreamWriter(out);
        Set<UUID> uuids = Sets.newHashSet();
        for (Deployment dep : deployments) {
            uuids.add(dep.getId());
        }
        Map<UUID, String> minimal_uuids = minuuid(uuids);
        for (Deployment deployment : deployments) {
            w.write(String.format("%s\t%s\t%s\t%s\n",
                                  minimal_uuids.get(deployment.getId()),
                                  deployment.getName(),
                                  state.statusFor(deployment.getId()),
                                  deployment.getHost()));
        }
        w.flush();

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
