MODULE_NAME=repository
DIST_DIR=./dist
BUILD_DIR=./build
TARGET_DIR=./target
GRAILS=grails

all:
	@echo "Possible targets:"
	@echo " check     -  Check your environment for the needed requirements"
	@echo " clean     -  Cleanup project"
	@echo " run-test  -  Run project in developer mode (file database)"
	@echo " run-dev   -  Run project in developer mode (memory database)"
	@echo " run-prod  -  Run project in productive mode (MySQL database)"
	@echo " dist      -  Create a war file that can be deployed in a servlet container"
	@echo " upgrade   -  Upgrade to another grails version"

check:
	@[ -f "`which ${GRAILS}`" ] || (echo "Install '${GRAILS}' first and add the executable to your path."; exit 1;)


upgrade: check
	@${GRAILS} upgrade
	@${GRAILS} install-templates

assemble: check
	@rm -fr "${BUILD_DIR}"
	@mkdir -p "${BUILD_DIR}/${MODULE_NAME}"
	@${GRAILS} war repository.war
	@cp -a target web-app groovy grails-app devDB.* application.properties fixtures README.txt "${BUILD_DIR}/${MODULE_NAME}"
	@mv repository.war "${BUILD_DIR}/"

dist: assemble
	@rm -fr "${DIST_DIR}"
	@mkdir "${DIST_DIR}"
	@cp -a "${BUILD_DIR}"/* "${DIST_DIR}"

clean: check
	@rm -fr "${BUILD_DIR}" "${DIST_DIR}" "${TARGET_DIR}"
	@${GRAILS} clean

run-test: check
	@echo "Running in testing mode..."
	@grails -Dserver.host=0.0.0.0 -Dserver.port=8080 test run-app

run-dev: check
	@echo "Running in developer mode..."
	@grails -Dserver.host=0.0.0.0 -Dserver.port=8080 dev run-app

run-prod: check
	@echo "Running in productive mode..."
	@grails -Dserver.host=0.0.0.0 -Dserver.port=8080 prod run-app
