Dwarf is a very small Galaxy implementation which requires no installation on the appserver end.

Commands:
  DONE:

    dwarf host add foo001.example.snv1
    dwarf host add foo002.example.snv1
    dwarf host add foo003.example.snv1 foo004.example.snv1
    dwarf host list

    dwarf deploy --host foo001 http://repo.example.colo/echo-1.2.tar.gz --name "Echo 1.2"
    dwarf start e12f
    dwarf stop e12f
    dwarf clear e12f
    dwarf list

    dwarf host remove foo002
    dwarf deploy --host foo001 path/to/deployment_descriptor.yml
    dwarf deploy --host foo001 path/to/deployment_descriptor_template.yml -t arch=x64-linux -t version=1.2

  TODO:

    dwarf search "name =~ Echo.+"

    dwarf check        # implied all
    dwarf check foo001

    dwarf exec --deployment e12f ls -l
    dwarf exec -d e12f -- ls -l

    dwarf host lock foo003
    dwarf host unlock foo003

    dwarf host set --host foo001 arch=x64-linux
    dwarf host get --host foo001 arch
    dwarf host get --host foo001


    # a possible update sequence
    # deploy new instances, but don't start them yet
    dwarf deploy --host "name =~ echo.+" -t version=1.7.43 http://repo/echo-template.yml
    # stop the old deployments
    dwarf stop "name != echo-1.7.42"
    # start new deployments
    dwarf start "name =~ echo-1.7.43"

    # alternate update command which does the above in one swell foop
    dwarf update --start --deployments "name =~ echo.+" -t version=1.7.43 http://repo/echo-template.yml

Let's see where it goes.

Deployment descriptors

    bundle: http://repo.example.com/echo-{version}-{arch}.tar.gz
    name: Echo Server
    config:
      /env/waffles.conf : http://config/waffles.conf
      /env/pancakes.conf : http://config/yummy.properties


It is looking like we are moving towards attributes on things --
probably hosts and deployments. For example a host may be attributed
with the operating system ( os=linux-x86_64 ) and a deployment with a
version ( version=1.2.3 ). You can also use this for arbitrary
grouping ( cluster=0001 ) or so on. This allows for nice filters on
set operations against either hosts or deployments (ie, upgrading a
cluster at a time, or so on). Name is just a special required
attribute in this model.


Should we support external commands? This would be pretty
straightforward to do a la ansible (in fact, we should consider
switching our remote operations to stage data and copy up scripts like
ansible does modules).


Do we want to encourage the "keep the system state in a git repo"
model where the descriptors and system state can all be checked out
and managed together? The alternative is a model where descriptors
tend to live at remote URLs and state is managed non-locally as well
(shared database, zookeeper, etc). I really like the simplicity of
shoving it all in a directory and managing it with git. This should be
doable as long as we keep the state files diff friendly -- flat text
and predictable ordering. Imagine:

    prod/dwarf.conf
    prod/echo/descriptor.yml
    prod/echo/echo-config.properties
    prod/memcached/descriptor.yml
    prod/memcached/memcached.conf
    prod/.dwarf/hosts
    prod/.dwarf/deployment_status
    prod/.dwarf/deployments

When you have a new server type you just make a directory for it, put
in the configs and descriptor, and check it in. Need to think through
things like per-env config when my brain is less fuzzy, though.

TODO:
- Host remove should warn if there are active deployments
- Search
- Check (verifies state against reality)
- Exec
- Nice error messages
- non-local storage
- store logs on host like sculptor does
- store deployment metadata with the deployment (as deployment.yml -- with absolute urls)
- set operations
- per-host configuration (such as deployment root)


# Configuration Consideration

Instead of preferring files on disk at a known location, prefer
environment variables for env configuration, and provide the file
thing as an override/escape hatch mechanism.

Consider

    dwarf deploy --host foo001 http://repo.example.colo/echo-1.2.tar.gz --name "Echo 1.2" --env ./env.yml
    
The `env.yml` file will convert into environment variables via dot
notation for hierarchy (explode on lists?). Dwarf config can specify a
default env, or possibly specify that an env is required.

This model turns the file overlay bit into the escape hatch (override
application config which is part of the bundle) and use env vars for
the actual *environment information*, as their name implies. 
