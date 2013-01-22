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
 * author: Shane Fox, Eamonn Power
 *
 * </copyright>
 *
 *  This is the controller class for the ResourceSpec domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ResourceSpecController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render ResourceSpec.list() as XML
    }

    def rest_show = {
        def resourceSpecInstance = ResourceSpec.get(params.id)
        if (resourceSpecInstance) {
            response.setCharacterEncoding("utf-8")
            render resourceSpecInstance as XML
        } else {
            SendNotFoundResponse()
        }

    }

    private def SendNotFoundResponse() {
        response.status = 404
        render contentType: "application/xml", {
            errors {
                message("item not found with id: " + params.id)
            }
        }
    }

    def rest_delete = {
        ResourceSpec resourceSpecInstance = ResourceSpec.get(params.id)
        boolean mustDelete

        if (!resourceSpecInstance) {
            SendNotFoundResponse()
        }

         // If the configParamInstance is in use by another ResourceSpec don't try to delete
        def resInst = ResourceInstance.findAllByResourceSpec(resourceSpecInstance)
        if(!resInst) {
            def orgs = Organisation.withCriteria(){
                createAlias("hasResources", "res")
                    eq('res.id', resourceSpecInstance.id)
            }

            // Remove this resourceSpec from all Organisations before deletion
            if(orgs.size() > 0){
                for(int i=0;i<orgs.size();i++){
                    if(orgs[0]!=null){
                        def org = Organisation.get(orgs[i].id)
                            org.removeFromHasResources(resourceSpecInstance)
                    }
                }
            }

            // Allows control over Spring's Transactions
            ResourceSpec.withTransaction{
                status ->
                try{
                    resourceSpecInstance.delete(flush:true);
                    response.status = 204
                    render ""
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ResourceSpec.withSession { session ->
                        session.clear()
                    }
                    response.status = 405
                    render contentType: "text/xml", {
                        errors {
                            message("ResourceSpec ${params.id} could not be deleted due to a foreign key constraint.")
                        }
                    }
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ResourceSpec.withSession { session ->
                        session.clear()
                    }
                    response.status = 405
                    render contentType: "text/xml", {
                        errors {
                            message("ResourceSpec ${params.id} could not be deleted due to a foreign key constraint.")
                        }
                    }
                }
            }
        }else{
            response.status = 405
                    render contentType: "text/xml", {
                        errors {
                            message("Could not delete ResourceSpec ${params.id} due to an integrity constraint. ResourceInstance ${resInst.id} has an association.")
                        }
                    }
        }
    }

    def rest_update = {
        ResourceSpec resourceSpecInstance = ResourceSpec.get(params.id)
        def configParamInstance

        if (!resourceSpecInstance) {
            SendNotFoundResponse()
        }

        if(params.resourceSpecInstance.configurationParameters!=null){
            // The old configParam to be removed later
            configParamInstance = resourceSpecInstance.configurationParameters
        }

        params.resourceSpecInstance.configurationParameters = ConfigParam.get(params.resourceSpecInstance.configurationParameters)

        resourceSpecInstance.properties = params.resourceSpecInstance

        if(request.XML.keywords.size()>0 && request.XML.keywords!=""){
            resourceSpecInstance.keywords.clear()
            for(int i=0;i<request.XML.keywords.size();i++){
                resourceSpecInstance.keywords.add(i,Keyword.get(request.XML.keywords.getAt(i).toInteger()))
            }
        }	

	ResourceSpec inherits = ResourceSpec.get(params.resourceSpecInstance.inherits)
        if (inherits) {
	        params.resourceSpecInstance.inherits = inherits
	}
	
        if (resourceSpecInstance.validate()) {
            resourceSpecInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render resourceSpecInstance as XML
        } else {
	    println "error"
	println(request.XML.inherits)
	println(resourceSpecInstance.inherits)
            sendValidationFailedResponse(resourceSpecInstance, 400)
        }
        //Remove the old configParams if they have been modified
        if(configParamInstance!=params.resourceSpecInstance.configurationParameters){
            removeAssociations(configParamInstance)
        }
    }

    private def sendValidationFailedResponse(resourceSpecInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                resourceSpecInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        ConfigParam configParamInstance = ConfigParam.get(params.resourceSpecInstance.configurationParameters)
        if (!configParamInstance) {
            SendNotFoundResponse()
        }
        params.resourceSpecInstance.configurationParameters = configParamInstance

        def resourceSpecInstance = new ResourceSpec(params.resourceSpecInstance)

        if(request.XML.keywords.size()>0 && request.XML.keywords!=""){
            resourceSpecInstance.keywords.clear()
            for(int i=0;i<request.XML.keywords.size();i++){
                resourceSpecInstance.keywords.add(i,Keyword.get(request.XML.keywords.getAt(i).toInteger()))
            }
        }
	
	/*
	
        if(request.XML.keywords!=""){
            request.XML.keywords.each{
                def keyword = Keyword.get(it.toString())
                if (!keyword) {
                    SendNotFoundResponse()
                }else{
                    resourceSpecInstance.addToKeywords(keyword)
                }
            }
        }
*/
        if(resourceSpecInstance.save()){
            response.setCharacterEncoding("utf-8")
            render resourceSpecInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    resourceSpecInstance?.errors?.fieldErrors?.each {err ->
                        field(err.field)
                        message(g.message(error: err))
                    }
                }
            }
        }
    }

    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ resourceSpecInstanceList: ResourceSpec.list( params ), resourceSpecInstanceTotal: ResourceSpec.count() ]
    }

    def show = {
        def resourceSpecInstance = ResourceSpec.get( params.id )

        if(!resourceSpecInstance) {
            flash.message = "ResourceSpec not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ resourceSpecInstance : resourceSpecInstance ] }
    }

    def delete = {
        def resourceSpecInstance = ResourceSpec.get( params.id )
        boolean DeletePtmInfo

        // Find any associations with ResourceInstances
        def resInst = ResourceInstance.findAllByResourceSpec(resourceSpecInstance)
        if(!resInst) {
            if(resourceSpecInstance) {
                def orgs = Organisation.withCriteria(){
                    createAlias("hasResources", "res")
                        eq('res.id', resourceSpecInstance.id)
                }
                // Remove this resourceSpec from all Organisations before deletion
                if(orgs.size() > 0){
                    for(int i=0;i<orgs.size();i++){
                        if(orgs[0]!=null){
                            def org = Organisation.get(orgs[i].id)
                            org.removeFromHasResources(resourceSpecInstance)
                        }
                    }
                }

                // Find any associations with PtmInfo
                def ptmInfos = PtmInfo.withCriteria(){
                    createAlias("resourceSpecs", "resSpecs")
                        eq('resSpecs.id', resourceSpecInstance.id)
                }
                if(ptmInfos.size() > 0){
                    for(int i=0;i<ptmInfos.size();i++){
                        if(ptmInfos[0]!=null){
                            def ptmInfo = PtmInfo.get(ptmInfos[i].id)
                            // Find the associated Ptm
                            def ptm = Ptm.findAllByDescribedByPtmInfo(ptmInfo)
                        }
                    }
                }

                // Allows control over Spring's Transactions
                ResourceSpec.withTransaction{
                    status ->
                        try{
                            resourceSpecInstance.delete()
                            flash.message = "ResourceSpec ${params.id} deleted"
                            redirect(action:list)
                        }
                        catch(org.springframework.dao.DataIntegrityViolationException e) {
                            // Need to roll back the transaction so associations are not deleted
                            status.setRollbackOnly()

                            ResourceSpec.withSession { session ->
                                session.clear()
                            }
                            flash.message = "Could not delete ResourceSpec ${params.id} due to an integrity constraint."
                        }
                        catch(org.hibernate.exception.ConstraintViolationException e){
                            // Need to roll back the transaction so associations are not deleted
                            status.setRollbackOnly()

                            ResourceSpec.withSession { session ->
                                session.clear()
                            }
                            flash.message = "Could not delete ResourceSpec ${params.id} due to an integrity constraint"
                        }
                    }
            }
            else {
                flash.message = "ResourceSpec not found with id ${params.id}"
                redirect(action:list)
            }
        }else{
             flash.message = "Could not delete ResourceSpec ${params.id} due to an integrity constraint. ResourceInstance ${resInst.id} has an association."
                redirect(action:list)
        }
    }

    def edit = {
        def resourceSpecInstance = ResourceSpec.get( params.id )

        if(!resourceSpecInstance) {
            flash.message = "ResourceSpec not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ resourceSpecInstance : resourceSpecInstance ]
        }
    }

    def update = {
        def resourceSpecInstance = ResourceSpec.get( params.id )

        // The old configParam to be removed later
        def configParamInstance = resourceSpecInstance.configurationParameters

        if(resourceSpecInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(resourceSpecInstance.version > version) {
                    resourceSpecInstance.errors.rejectValue("version", "resourceSpec.optimistic.locking.failure", "Another user has updated this ResourceSpec while you were editing.")
                    render(view:'edit',model:[resourceSpecInstance:resourceSpecInstance])
                    return
                }
            }
            // Update resourceSpecInstance to the new configurationParameters
            resourceSpecInstance.configurationParameters = ConfigParam.get(params.configurationParameters.id)
            resourceSpecInstance.properties = params

            if(!resourceSpecInstance.hasErrors() && resourceSpecInstance.save()) {
                flash.message = "ResourceSpec ${params.id} updated"
                redirect(action:show,id:resourceSpecInstance.id)
                // Remove the now unused ConfigParams if they have been updated
                if(configParamInstance!=resourceSpecInstance.configurationParameters){
                    removeAssociations(configParamInstance)
                }
            }
            else {
                render(view:'edit',model:[resourceSpecInstance:resourceSpecInstance])
            }
        }
        else {
            flash.message = "ResourceSpec not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def resourceSpecInstance = new ResourceSpec()
        resourceSpecInstance.properties = params
        return ['resourceSpecInstance':resourceSpecInstance]
    }

    def save = {
        def resourceSpecInstance = new ResourceSpec(params)
        if(!resourceSpecInstance.hasErrors() && resourceSpecInstance.save()) {
            flash.message = "ResourceSpec ${resourceSpecInstance.id} created"
            redirect(action:show,id:resourceSpecInstance.id)
        }
        else {
            render(view:'create',model:[resourceSpecInstance:resourceSpecInstance])
        }
    }

    def removeAssociations(configParamInstance){
        // If the configParamInstance is in use by a Configuration don't try to delete
        def configs = Configuration.withCriteria(){
                createAlias("configurationParamComposite", "cpc")
                    eq('cpc.id', configParamInstance.id)
        }
        // If the configParamInstance is in use by a ConfigParamComp don't try to delete
        def comps = ConfigParamComposite.withCriteria(){
                createAlias("configParams", "cp")
                    eq('cp.id', configParamInstance.id)
        }
	// If the configParamInstance is in use by another ResourceSpec don't try to delete
        def resSpec = ResourceSpec.findAllByConfigurationParameters(configParamInstance)

        if(configs.size() == 0 && comps.size() == 0 && resSpec.size()<=1){
            ConfigParam.withTransaction{
                status ->
                try {
                    configParamInstance.delete(flush: true)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()
                    ConfigParam.withSession { session ->
                        session.clear()
                    }
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()
                    ConfigParamComposite.withSession { session ->
                        session.clear()
                    }
                }
             }
        }
    }

}

