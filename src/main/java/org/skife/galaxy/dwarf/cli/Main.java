package org.skife.galaxy.dwarf.cli;

import org.skife.cli.Cli;
import org.skife.cli.Help;

import java.io.File;
import java.util.concurrent.Callable;

import static org.skife.cli.config.PropertiesConfiguration.fromProperties;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Cli.CliBuilder<Callable> builder = Cli.buildCli("dwarf", Callable.class)
            .withConfiguration(fromProperties(new File("/etc/dwarf.conf"),
                                              new File("dwarf.conf")))  // same dir overrides /etc
            .withDescription("A Small Galaxy Implementation")
            .withCommand(Deploy.class)
            .withCommand(Search.class)
            .withCommand(Start.class)
            .withCommand(Stop.class)
            .withCommand(Clear.class)
            .withCommand(Refresh.class)
            .withCommand(Help.class)
            .withDefaultCommand(Help.class);

        builder.withGroup("host")
               .withDescription("Operations on the hosts this dwarf mine knows about ;-)")
               .withDefaultCommand(HostList.class)
               .withCommand(Help.class)
               .withCommand(HostList.class)
               .withCommand(HostAdd.class)
               .withCommand(HostRemove.class);

        try {
            builder.build().parse(args).call();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            Runtime.getRuntime().exit(1);
        }
    }
}
