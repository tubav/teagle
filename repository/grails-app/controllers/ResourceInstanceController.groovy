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
 *  This is the controller class for the ResourceInstance domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ResourceInstanceController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render ResourceInstance.list() as XML
    }

    def rest_show = {
        def resourceInstanceInstance = ResourceInstance.get(params.id)
        if (resourceInstanceInstance) {
            response.setCharacterEncoding("utf-8")
            render resourceInstanceInstance as XML
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
        ResourceInstance resourceInstanceInstance = ResourceInstance.get(params.id)
        boolean mustDelete

        if (!resourceInstanceInstance) {
            SendNotFoundResponse()
        }

        // Allows control over Spring's Transactions
        ResourceInstance.withTransaction{
            status ->
            try{
                // Need to remove the association with Vcts before deleting
                removeFromProvidesResourcesAssociations(resourceInstanceInstance)

                resourceInstanceInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                ResourceInstance.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ResourceInstance ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                ResourceInstance.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ResourceInstance ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
      ResourceInstance.withTransaction{
        ResourceSpec resSpecification = ResourceSpec.get(params.resourceInstanceInstance.resourceSpec)
        if (!resSpecification) {
            SendNotFoundResponse()
        }

        params.resourceInstanceInstance.resourceSpec = resSpecification
	
	ResourceInstance parentInstance = ResourceInstance.get(params.resourceInstanceInstance.parentInstance)
	if (parentInstance) {
		params.resourceInstanceInstance.parentInstance = parentInstance
	}

        ResourceInstance resourceInstanceInstance = ResourceInstance.get(params.id)
        if (!resourceInstanceInstance) {
            SendNotFoundResponse()
        }
        
        resourceInstanceInstance.properties = params.resourceInstanceInstance

        //Handling multiple configurationData entries
        if(request.XML.configurationData.size()>0 && request.XML.configurationData!=""){
            resourceInstanceInstance.properties.configurationData.clear()
            for(int i=0;i<request.XML.configurationData.size();i++){
                resourceInstanceInstance.properties.configurationData.add(i,ConfigurationBase.get(request.XML.configurationData.getAt(i).toInteger()))
            }
        }

        if (resourceInstanceInstance.validate()) {
            resourceInstanceInstance.save(flush: true);
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render resourceInstanceInstance as XML
        } else {
            sendValidationFailedResponse(resourceInstanceInstance, 403)
        }
}
    }

    private def sendValidationFailedResponse(resourceInstanceInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                resourceInstanceInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {

      ResourceInstance.withTransaction{
        ResourceSpec resSpecification = ResourceSpec.get(params.resourceInstanceInstance.resourceSpec)
        if (!resSpecification) {
            SendNotFoundResponse()
        }
        params.resourceInstanceInstance.resourceSpec = resSpecification

	ResourceInstance parentInstance = ResourceInstance.get(params.resourceInstanceInstance.parentInstance)
	if (parentInstance) {
		params.resourceInstanceInstance.parentInstance = parentInstance
	}

        def resourceInstanceInstance = new ResourceInstance(params.resourceInstanceInstance)

        //Handling multiple configurationData entries
	println "data: " + request.XML.configurationData
	println "datasize: " + request.XML.configurationData.size()
        if(request.XML.configurationData.size()>0 && request.XML.configurationData!=""){
	    println "adding data"
            resourceInstanceInstance.configurationData.clear()
            for(int i=0;i<request.XML.configurationData.size();i++){
	        println "adding " + request.XML.configurationData.getAt(i).toInteger()
                resourceInstanceInstance.configurationData.add(i,ConfigurationBase.get(request.XML.configurationData.getAt(i).toInteger()))
            }
        }
	print "instancedata now: " + resourceInstanceInstance.configurationData

        if(resourceInstanceInstance.save(flush:true, validate:false)){
            response.setCharacterEncoding("utf-8")
            render resourceInstanceInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    resourceInstanceInstance?.errors?.fieldErrors?.each {err ->
                        field(err.field)
                        message(g.message(error: err))
                    }
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
        [ resourceInstanceInstanceList: ResourceInstance.list( params ), resourceInstanceInstanceTotal: ResourceInstance.count() ]
    }

    def show = {
        def resourceInstanceInstance = ResourceInstance.get( params.id )

        if(!resourceInstanceInstance) {
            flash.message = "ResourceInstance not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ resourceInstanceInstance : resourceInstanceInstance ] }
    }

    def delete = {
        def resourceInstanceInstance = ResourceInstance.get( params.id )
        boolean mustDelete
        if(resourceInstanceInstance) {
            // Allows control over Spring's Transactions
            ResourceInstance.withTransaction{
                status ->
                try{
                    // Need to remove the association with Vcts before delete
                    removeFromProvidesResourcesAssociations(resourceInstanceInstance)
                    
                    resourceInstanceInstance.delete()
                    flash.message = "ResourceInstance ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ResourceInstance.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete ResourceInstance ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ResourceInstance.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete ResourceInstance ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "ResourceInstance not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def resourceInstanceInstance = ResourceInstance.get( params.id )

        if(!resourceInstanceInstance) {
            flash.message = "ResourceInstance not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ resourceInstanceInstance : resourceInstanceInstance ]
        }
    }

    def update = {
        def resourceInstanceInstance = ResourceInstance.get( params.id )
        if(resourceInstanceInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(resourceInstanceInstance.version > version) {
                    
                    resourceInstanceInstance.errors.rejectValue("version", "resourceInstance.optimistic.locking.failure", "Another user has updated this ResourceInstance while you were editing.")
                    render(view:'edit',model:[resourceInstanceInstance:resourceInstanceInstance])
                    return
                }
            }

            resourceInstanceInstance.properties = params
            if(!resourceInstanceInstance.hasErrors() && resourceInstanceInstance.save(flush: true)) {
                flash.message = "ResourceInstance ${params.id} updated"
                redirect(action:show,id:resourceInstanceInstance.id)
            }
            else {
                render(view:'edit',model:[resourceInstanceInstance:resourceInstanceInstance])
            }
        }
        else {
            flash.message = "ResourceInstance not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def resourceInstanceInstance = new ResourceInstance()
        resourceInstanceInstance.properties = params
        return ['resourceInstanceInstance':resourceInstanceInstance]
    }

    def save = {
        def resourceInstanceInstance = new ResourceInstance(params)
        if(!resourceInstanceInstance.hasErrors() && resourceInstanceInstance.save(flush: true)) {
            flash.message = "ResourceInstance ${resourceInstanceInstance.id} created"
            redirect(action:show,id:resourceInstanceInstance.id)
        }
        else {
            render(view:'create',model:[resourceInstanceInstance:resourceInstanceInstance])
        }
    }

     /**
     * Removes a resourceInstacne from all Vcts that it's associated with.
     */
    def removeFromProvidesResourcesAssociations(resourceInstanceInstance){
        def vcts = Vct.withCriteria(){
                createAlias("providesResources", "pr")
                    eq('pr.id', resourceInstanceInstance.id)
        }
        
        vcts.each{
            it.removeFromProvidesResources(resourceInstanceInstance)
        }
    }
}
