Dwarf is a very small Galaxy implementation which requires no installation on the appserver end.

    dwarf host add foo001.example.snv1
    dwarf host add foo002.example.snv1
    dwarf host add foo003.example.snv1 foo004.example.snv1
    dwarf host remove foo002
    dwarf host list

    dwarf --host foo001 deploy http://repo.example.colo/echo-1.2.tar.gz --name "Echo 1.2"
    dwarf start e12f
    dwarf stop e12f
    dwarf clear e12f
    dwarf list

    dwarf search name =~ Echo.+

    dwarf check
    dwarf check foo001

    dwarf exec --deployment e12f ls -l
    dwarf exec -d e12f -- ls -l

    dwarf host lock foo003
    dwarf host unlock foo003

Let's see where it goes.

TODO
- [ ] Host remove should warn if there are active deployments
- [ ] Deployments
- [ ] Search
- [ ] Check (verifies state against reality)
- [ ] Exec