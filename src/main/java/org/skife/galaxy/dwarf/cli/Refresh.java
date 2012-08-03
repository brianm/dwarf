package org.skife.galaxy.dwarf.cli;

import com.google.common.collect.Sets;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.cli.util.DeploymentRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = {"refresh", "status"})
public class Refresh implements Callable<Void>
{
    @Inject
    public GlobalOptions global = new GlobalOptions();

    @Arguments
    Set<String> deps = Sets.newLinkedHashSet();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, global.getDeployRoot(), global.getSshConfig());

        Set<Deployment> all_deployments = d.getDeployments();
        Set<Deployment> to_refresh = Sets.newTreeSet();

        if (deps.isEmpty()) {
            to_refresh.addAll(state.deployments());
        }
        else {

            for (String dep : deps) {
                Set<Deployment> local = Sets.newHashSet();
                for (Deployment deployment : all_deployments) {
                    if (deployment.getId().toString().startsWith(dep)) {
                        if (!local.add(deployment)) {
                            System.err.printf("ERROR: deployment prefix %s matches more then one deployment\n", dep);
                            Runtime.getRuntime().exit(1);
                        }
                        else {
                            to_refresh.add(deployment);
                        }
                    }
                }
            }
        }

        for (Deployment deployment : to_refresh) {
            d.status(deployment);
        }

        new DeploymentRenderer(to_refresh, state).render();

        return null;
    }

}
