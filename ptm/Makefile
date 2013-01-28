MODULE_NAME=ptm
DIST_DIR="`pwd`/dist"
BUILD_DIR="`pwd`/build"
TARGET_DIR="`pwd`/target"
PTM_PORT=8010
PTM_NAME="avptm"
RA_SAMPLE_PORT=8011
RA_SAMPLE=SimpleTestAdapter.SimpleTestAdapter.SimpleTestAdapter

help:
	@echo "Possible targets:"
	@echo " check          -  Check your environment for the needed requirements"
	@echo " clean          -  Cleanup project"
	@echo " compile        -  Collect all needed files and put it under the folder dist"
	@echo " run-ptm        -  Run PTM"
	@echo " run-ra-sample  -  Run sample RA"

check:
	@[ -f "`which python`" ] || (echo "Install 'python' first and add the executable to your path."; exit 1;)
	@[ ! "`python -c 'import werkzeug' 2>&1`" ] || (echo "Install python module 'werkzeug' first, e.g. by running 'pip install werkzeug'."; exit 1;)
	@[ ! "`python -c 'import mako' 2>&1`" ] || (echo "Install python module 'mako' first, e.g. by running 'pip install mako'."; exit 1;)

compile:
	@echo "Setting up..."; mkdir -p "${DIST_DIR}"
	@echo "Installing PTMHub..."; cd PTMHub; make dist; cd ..
	@echo "Installing PyPTM..."; cd pyptm; make dist; cd ..
	@echo "Installing HTML frontend..."; mkdir -p "${DIST_DIR}/lib/HTMLFrontend"; cp -r HTMLFrontend/templates HTMLFrontend/src/HTMLFrontend/* "${DIST_DIR}/lib/HTMLFrontend"
	@echo "Installing other libs..."; mkdir -p "${DIST_DIR}/lib"; cp -r ../lib/* "${DIST_DIR}/lib/"

run-ptm: check compile
	@echo "Running PTM..."
	@PTM_HOME="${DIST_DIR}" "${DIST_DIR}/bin/ptmhub" "-p ${PTM_PORT}" -x "${PTM_NAME}"

run-ra-sample: check compile
	@echo "Running Sample RA..."
	@PTM_HOME="${DIST_DIR}/" "${DIST_DIR}/bin/rarunner" "${RA_SAMPLE}" "-r http://localhost:${PTM_PORT}" "-p ${RA_SAMPLE_PORT}"
	@ "-p ${PORT}"

clean:
	@rm -fr "${BUILD_DIR}" "${DIST_DIR}" "${TARGET_DIR}"
