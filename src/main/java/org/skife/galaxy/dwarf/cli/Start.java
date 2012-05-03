package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.galaxy.dwarf.Deployment;
import org.skife.galaxy.dwarf.Dwarf;
import org.skife.galaxy.dwarf.cli.util.DeploymentRenderer;
import org.skife.galaxy.dwarf.state.file.FileState;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "start")
public class Start implements Callable<Void>
{

    @Option(name = {"-d", "--deploy-root"},
            title = "path",
            description = "Root path for deployments on target host",
            configuration = "deploy_root")
    public String deployRoot = "/tmp/dwarf";


    @Option(name = {"-C", "--ssh-config"},
            title = "ssh_config file",
            description = "SSH config file to use",
            configuration = "ssh_config")
    public String sshConfig = null;

    @Arguments
    Set<String> deps = Sets.newLinkedHashSet();

    @Override
    public Void call() throws Exception
    {
        FileState state = new FileState(Paths.get(".dwarf"));
        Dwarf d = new Dwarf(state, deployRoot, Optional.fromNullable(sshConfig).transform(new Function<String, Path>()
        {
            @Override
            public Path apply(@Nullable String input)
            {
                return Paths.get(input);
            }
        }));

        Set<Deployment> all_deployments = d.getDeployments();
        Set<Deployment> to_start = Sets.newTreeSet();

        for (String dep : deps) {
            Set<Deployment> local = Sets.newHashSet();
            for (Deployment deployment : all_deployments) {
                if (deployment.getId().toString().startsWith(dep)) {
                    if (!local.add(deployment)) {
                        System.err.printf("ERROR: deployment prefix %s matches more then one deployment\n", dep);
                        Runtime.getRuntime().exit(1);
                    }
                    else {
                        to_start.add(deployment);
                    }
                }
            }
        }

        for (Deployment deployment : to_start) {
            d.start(deployment);
        }

        new DeploymentRenderer(to_start, state).renderTsv(System.out);

        return null;
    }
}
