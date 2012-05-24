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

  TODO:
    dwarf search name =~ Echo.+

    dwarf host remove foo002

    dwarf check        # implied all
    dwarf check foo001

    dwarf exec --deployment e12f ls -l
    dwarf exec -d e12f -- ls -l

    dwarf host lock foo003
    dwarf host unlock foo003

Let's see where it goes.

TODO
- [ ] BUG: need to eagerly consume stdout/stderr on remote commands
- [ ] Host remove should warn if there are active deployments
- [ ] Search
- [ ] Check (verifies state against reality)
- [ ] Exec
- [ ] Nice error messages