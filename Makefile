build: setup
	GOPATH=$(PWD):$(PWD)/ext ; go install dwarf/dwarf

clean:
	rm -rf bin
	rm -rf pkg

setup:
	GOPATH=$(PWD)/ext ; go get code.google.com/p/goconf/conf

retag:
	rm -f TAGS
	find $(GOROOT)src/pkg -type f -name '*.go' | xargs ctags -e -a
	find src -type f -name '*.go' | xargs ctags -e -a
	find ext -type f -name '*.go' | xargs ctags -e -a

tags: retag

doc-server:
	GOPATH=$(PWD):$(PWD)/ext ; godoc -http=:6060
