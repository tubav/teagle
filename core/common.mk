BUILD_DIR=./build
DIST_DIR=./dist

assemble: clean
	@echo "Assebmling..."
	@mkdir -p "${BUILD_DIR}/lib"
	@cp -a src/* "${BUILD_DIR}/lib"
	@test -d "lib" && ( test -z "`ls -A lib/`" || cp -a lib/* "${BUILD_DIR}/lib" ) || true
	@for d in bin libexec res share etc static; do \
		test '!' -d "$$d" || cp -a "$$d" "${BUILD_DIR}"; \
	done

#all: assemble

dist: assemble
	@echo "Distribute..."
	@mkdir ${DIST_DIR}
	@mv "${BUILD_DIR}"/* ${DIST_DIR}

clean:
	@echo "Cleaning..."
	@rm -fr ${BUILD_DIR}
	@rm -fr ${DIST_DIR}
	@find . '(' -name '*.pyc' -or -name '*.pyo' ')' -exec rm -f {} \;
	
check:
	@echo "Checking environment..."
	@[ -f "`which python`" ] || (echo "Install 'python' first and add the executable to your path."; exit 1;)
	@[ ! "`python -c 'import django' 2>&1`" ] || (echo "Install python module 'django' first, e.g. by running 'pip install django'."; exit 1;)
#	@[ ! "`python -c 'import soaplib.serializers.primitive' 2>&1`" ] || (echo "Install python module 'Soaplib v1.0' first, e.g. by running 'pip install -I soaplib==1.0'."; exit 1;)
#	@[ ! "`python -c 'import pytz' 2>&1`" ] || (echo "Install python module 'pytz' first, e.g. by running 'pip install pytz'."; exit 1;)

