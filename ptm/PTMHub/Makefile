PTM_MODULES=ptmhub
PREFIX=../dist/

assemble:
	@rm -fr ./build ; mkdir build
	@cd src && for m in ${PTM_MODULES}; do \
		find "$$m" -name '*.py' -exec sh -c "mkdir -p ../build/lib/\`dirname \"{}\"\` && cp \"{}\" \"../build/lib/{}\"" \; ; \
	done
	@cp -a lib/* build/lib/.
	@mkdir -p build/var
	@cp -r bin build/
	@chmod +x build/bin/*
	
install: assemble
	@mkdir -p "${PREFIX}"
	@cd build && find . -exec sh -c "test -d \"{}\" && install -d \"${PREFIX}/{}\" || install \"{}\" \"${PREFIX}/{}\"" \;

dist: assemble
	@mkdir -p "${PREFIX}"
	@cp -r build/* "${PREFIX}"

all: assemble

clean:
	@rm -fr build
	@rm -fr dist
	@find . -name '*.pyc' -or -name '*.pyo' -exec rm -f {} \;
	
