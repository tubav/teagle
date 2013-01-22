/**
 * </copyright>
 *
 * 2008-2010 © Waterford Institute of Technology (WIT),
 *              TSSG, EU FP7 ICT Panlab.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * author: Shane Fox
 *
 * </copyright>
 *
 * A fixture to load a sample Person and create Person Roles
 */

include "states"
 
fixture{
    ConfigParamAtomicTestA1(ConfigParamAtomic){
        commonName="test_a_1"
        description="test_a_1"
	defaultParamValue="defval_a_1"
	configParamType="string"
    }

    ConfigParamAtomicTestA2(ConfigParamAtomic){
        commonName="test_a_2"
        description="test_a_2"
	defaultParamValue="defval_a_2"
	configParamType="string"
    }

    ConfigParamAtomicTestB1(ConfigParamAtomic){
        commonName="test_b_1"
        description="test_b_1"
	defaultParamValue="defval_b_1"
	configParamType="string"
    }

    ConfigParamAtomicTestB2(ConfigParamAtomic){
        commonName="test_b_2"
        description="test_b_2"
	defaultParamValue="defval_b_2"
	configParamType="string"
    }

    ConfigParamCompositeTestA(ConfigParamComposite){
	commonName="testa"
	description="testa"
	configParams=[ConfigParamAtomicTestA1,ConfigParamAtomicTestA2]
    }

    ConfigParamCompositeTestB(ConfigParamComposite){
	commonName="testb"
	description="testb"
	configParams=[ConfigParamAtomicTestB1,ConfigParamAtomicTestB2]
    }

    ResourceSpecTestA(ResourceSpec){
        commonName = "TestA"
	description="testa"
	configurationParameters=ConfigParamCompositeTestA
    }
    ResourceSpecTestB(ResourceSpec){
        commonName = "TestB"
	description = "testb"
	configurationParameters=ConfigParamCompositeTestB
	inherits = ResourceSpecTestA
    }

    ConfigParamAtomicSimple_Data(ConfigParamAtomic){
        commonName="data"
        description="simple_data"
	defaultParamValue="abc"
	configParamType="string"
    }

    ConfigParamCompositeSimple(ConfigParamComposite){
	commonName="simple"
	description="simple"
	configParams=[ ConfigParamAtomicSimple_Data ]
    }

    ResourceSpecSimple(ResourceSpec){
        commonName = "Simple"
	description="simple"
	configurationParameters=ConfigParamCompositeSimple
    }

    ConfigParamCompositeSimpleTest(ConfigParamComposite){
	commonName="simpletest"
	description="simpletest"
	configParams=[]
    }

    ResourceSpecSimpleTest(ResourceSpec){
        commonName = "SimpleTest"
	description="simpletest"
	configurationParameters=ConfigParamCompositeSimpleTest
    }

    ConfigParamAtomicLevel0Key(ConfigParamAtomic){
        commonName="key"
        description="level0_key"
	defaultParamValue="default_value_for_key"
	configParamType="string"
    }

    ConfigParamCompositeLevel0(ConfigParamComposite){
	commonName="level0"
	description="level0"
	configParams=[ ConfigParamAtomicLevel0Key ]
    }

    ResourceSpecLevel0(ResourceSpec){
        commonName = "level0"
	description="level0"
	configurationParameters=ConfigParamCompositeLevel0
    }

    ConfigParamCompositeLevel1(ConfigParamComposite){
	commonName="level1"
	description="level1"
	configParams=[]
    
    }
   
    ResourceSpecLevel1(ResourceSpec){
        commonName = "level1"
	description="level1"
	configurationParameters=ConfigParamCompositeLevel1
    }

    ConfigletLevel0_0_Key(Configlet){
	commonName = "key"
	description = "desc"
	paramValue = "fixture_value"
	configParamAtomic = ConfigParamAtomicLevel0Key
    }

    ResourceInstanceLevel0_0(ResourceInstance){
	commonName = "testptm./level0-0"
	description = "level0-0"
	resourceSpec = ResourceSpecLevel0
	configurationData = [ ConfigletLevel0_0_Key  ]
	state = ResourceInstanceStateProvisioned
	shared = true
    }
    
    ConfigParamAtomicPerson_Hometown(ConfigParamAtomic){
        commonName="hometown"
        description="hometown"
	defaultParamValue=""
	configParamType="string"
    }
    
    ConfigParamAtomicPerson_Age(ConfigParamAtomic){
        commonName="age"
        description="age"
	defaultParamValue=""
	configParamType="integer"
    }
    
    ConfigParamAtomicPerson_Height(ConfigParamAtomic){
        commonName="height"
        description="height"
	defaultParamValue=""
	configParamType="float"
    }

    ConfigParamAtomicPerson_BFF(ConfigParamAtomic){
        commonName="bff"
        description="best friend (forever)"
	defaultParamValue=""
	configParamType="reference"
    }
    
    ConfigParamCompositePerson(ConfigParamComposite){
	commonName="person"
	description="person"
	configParams=[ ConfigParamAtomicPerson_Hometown, ConfigParamAtomicPerson_Age, ConfigParamAtomicPerson_Height, ConfigParamAtomicPerson_BFF ]
    }
    
    ResourceSpecPerson(ResourceSpec){
        commonName = "Person"
	description="person"
	configurationParameters=ConfigParamCompositePerson
    }

    ConfigParamCompositeEpcEnabler(ConfigParamComposite){
	commonName="EpcEnablerConf"
	description="EpcEnablerConf"
	configParams=[]
    }

    ResourceSpecEpcEnabler(ResourceSpec){
        commonName = "epcenabler"
	description="EPC Enabler"
        configurationParameters=ConfigParamCompositeEpcEnabler
    }

    ConfigParamCompositePdngw(ConfigParamComposite){
	commonName="PdngwConf"
	description="PdngwConf"
	configParams=[]
    }

    ResourceSpecPdngw(ResourceSpec){
        commonName = "pdngw"
	description="PDN Gateway"
        configurationParameters=ConfigParamCompositePdngw
    }

    ConfigParamCompositeEpdg(ConfigParamComposite){
	commonName="EpdgConf"
	description="EpdgwConf"
	configParams=[]
    }

    ResourceSpecEpdg(ResourceSpec){
        commonName = "epdg"
	description="EPD Gateway"
        configurationParameters=ConfigParamCompositeEpdg
    }

    ConfigParamCompositeSgw(ConfigParamComposite){
	commonName="SgwConf"
	description="SgwConf"
	configParams=[]
    }

    ResourceSpecSgw(ResourceSpec){
        commonName = "sgw"
	description="S Gateway"
        configurationParameters=ConfigParamCompositeSgw
    }

    ConfigParamCompositeSwitch(ConfigParamComposite){
	commonName="SwitchConf"
	description="SwitchConf"
	configParams=[]
    }

    ResourceSpecSwitch(ResourceSpec){
        commonName = "switch"
	description="Switch"
        configurationParameters=ConfigParamCompositeSwitch
    }

    ConfigParamAtomicPtProbe_Target(ConfigParamAtomic){
        commonName="target"
        description="target"
        defaultParamValue=""
        configParamType="reference"
    }

    ConfigParamCompositePtProbe(ConfigParamComposite){
        commonName="PtProbeConf"
        description="PtProbeConf"
        configParams=[ConfigParamAtomicPtProbe_Target]
    }

    ResourceSpecPtProbe(ResourceSpec){
        commonName = "ptprobe"
        description="ptprobe"
        configurationParameters=ConfigParamCompositePtProbe
    }


    OrganisationTest(Organisation)
    {
	name = "testorg"
	people = []
    }

    PtmEPC(Ptm){
	commonName = "ptmepc"
	url = "http://localhost:8010/rest"
	legacyUrl = "http://localhost:8010/soap"
	description= "EPC PTM"
	provider = OrganisationTest
	resourceSpecs = [ResourceSpecEpcEnabler, ResourceSpecPdngw, ResourceSpecEpdg, ResourceSpecSgw, ResourceSpecSwitch, ResourceSpecPtProbe]
    }

/**    PtmTest(Ptm){
	commonName = "testptm"
	url = "http://localhost:7000/rest"
	legacyUrl = "http://localhost:7000/soap"
	description= "test"
	provider = OrganisationTest
	resourceSpecs = [ResourceSpecTestA, ResourceSpecTestB, ResourceSpecSimpleTest, ResourceSpecLevel0, ResourceSpecLevel1, ResourceSpecPerson, ResourceSpecSimple ]
    }
*/

/**
    PtmSecureTest(Ptm){
	commonName = "secureptm"
	url = "https://localhost:8443/rest"
	legacyUrl = "https://localhost:8443/soap"
	description= "httpstest"
	provider = OrganisationTest
	resourceSpecs = [ResourceSpecTestA, ResourceSpecTestB, ResourceSpecSimpleTest, ResourceSpecLevel0, ResourceSpecLevel1, ResourceSpecPerson ]
    }
*/
}
