package org.skife.galaxy.dwarf.cli;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.skife.cli.Option;
import org.skife.cli.OptionType;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GlobalOptions
{
    @Option(name = {"-C", "--ssh-config"},
            title = "ssh_config file",
            description = "SSH config file to use",
            configuration = "ssh_config",
            type = OptionType.GLOBAL)
    public String sshConfig = null;

    @Option(name = {"-d", "--deploy-root"},
            title = "path",
            description = "Root path for deployments on target host",
            configuration = "deploy_root")
    public String deployRoot = "/tmp/dwarf";



    public String getDeployRoot() {
        return deployRoot;
    }

    public Optional<Path> getSshConfig() {
        return Optional.fromNullable(sshConfig).transform(new Function<String, Path>()
        {
            @Override
            public Path apply(@Nullable String input)
            {
                return Paths.get(input);
            }
        });
    }
}
