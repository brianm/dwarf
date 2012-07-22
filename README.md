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
    dwarf deploy --host foo001 path/to/deployment_descriptor_template.yml -p arch=x64-linux -p version=1.2

  TODO:

    dwarf search name =~ Echo.+

    dwarf check        # implied all
    dwarf check foo001

    dwarf exec --deployment e12f ls -l
    dwarf exec -d e12f -- ls -l

    dwarf host lock foo003
    dwarf host unlock foo003

    dwarf host set --host foo001 arch=x64-linux
    dwarf host get --host foo001 arch
    dwarf host get --host foo001

Let's see where it goes.

Deployment descriptors

    bundle: http://repo.example.com/echo-{version}-{arch}.tar.gz
    name: Echo Server
    config:
      /env/waffles.conf : http://config/waffles.conf
      /env/pancakes.conf : http://config/yummy.properties

TODO
- [ ] Add deployment configuration mechanism
- [ ] Host remove should warn if there are active deployments
- [ ] Search
- [ ] Check (verifies state against reality)
- [ ] Exec
- [ ] Nice error messages
- [ ] non-sqlite storage
- [ ] store logs on host like sculptor does
