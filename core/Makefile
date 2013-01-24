help:
	@echo "Possible targets:"
	@echo " check      -  Check your environment for the needed requirements"
	@echo " clean-all  -  Cleanup project"
	@echo " compile    -  Collect all needed files and put it under the folder dist"
	@echo " run        -  Run Teagle"
	@echo " test       -  Run some tests"
	@echo " manage     -  Manage Teagle"

include common.mk

ifeq (manage,$(firstword $(MAKECMDGOALS)))
  RUN_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  $(eval $(RUN_ARGS):;@:)
endif

compile:
	@cd TeagleOE; make dist
	@cd TeagleGW; make dist
	@cd TeagleRP; make dist
	@cd Teagle; make dist
	@cd Djeagle; make dist

clean-all:
	@cd TeagleOE; make clean
	@cd TeagleGW; make clean
	@cd TeagleRP; make clean
	@cd Teagle; make clean
	@cd Djeagle; make clean

test: check
	@TEAGLE_HOME=./dist/ PYTHONPATH=./dist/lib python ./dist/lib/djeagle/manage.py test

collect:
	@echo "Copying files..."; cp -r Teagle/dist/lib/* TeagleOE/dist/lib/* TeagleRP/dist/lib/* TeagleGW/dist/lib/* ../lib/ngniutils Djeagle/dist/lib/

run: check compile collect
	@echo "Running server..."; cd Djeagle/dist/lib/djeagle; PYTHONPATH=.. python runserver_mt.py runserver

manage: check compile collect
	@echo "Running manage script..."; cd Djeagle/dist/lib/djeagle; PYTHONPATH=.. python manage.py $(RUN_ARGS)

