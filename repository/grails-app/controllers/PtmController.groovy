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
 *  This is the controller class for the Ptm domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class PtmController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Ptm.list() as XML
    }

    def rest_show = {
        def ptmInstance = Ptm.get(params.id)
        if (ptmInstance) {
            response.setCharacterEncoding("utf-8")
            render ptmInstance as XML
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
        Ptm ptmInstance = Ptm.get(params.id)
        if (!ptmInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        Ptm.withTransaction{
            status ->
            try{
                ptmInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                Ptm.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Ptm ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                Ptm.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Ptm ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        Ptm ptmInstance = Ptm.get(params.id)
        if (!ptmInstance) {
            SendNotFoundResponse()
        }

        ptmInstance.properties = params.ptmInstance

        if(request.XML.resourceSpecs.size()>0 && request.XML.resourceSpecs!=""){
            ptmInstance.resourceSpecs.clear()
            for(int i=0;i<request.XML.resourceSpecs.size();i++){
                ptmInstance.resourceSpecs.add(i,ResourceSpec.get(request.XML.resourceSpecs.getAt(i).toInteger()))
            }
        }

        if(request.XML.supportedResources.size()>0 && request.XML.supportedResources!=""){
            ptmInstance.supportedResources.clear()
            for(int i=0;i<request.XML.supportedResources.size();i++){
                ptmInstance.supportedResources.add(i,ResourceSpec.get(request.XML.supportedResources.getAt(i).toInteger()))
            }
        }

	if (request.XML.provider != "") {
		ptmInstance.provider = Organisation.get(request.xml.provider)
	}

        if (ptmInstance.validate()) {
            ptmInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render ptmInstance as XML
        } else {
            sendValidationFailedResponse(ptmInstance, 403)
        }
    }

    private def sendValidationFailedResponse(ptmInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                ptmInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        params.ptmInstance.owner=Person.get(params.ptmInstance.owner)

        def ptmInstance = new Ptm(params.ptmInstance)
                       
        if(ptmInstance.save()){
            response.setCharacterEncoding("utf-8")
            render ptmInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    ptmInstance?.errors?.fieldErrors?.each {err ->
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
        [ ptmInstanceList: Ptm.list( params ), ptmInstanceTotal: Ptm.count() ]
    }

    def show = {
        def ptmInstance = Ptm.get( params.id )

        if(!ptmInstance) {
            flash.message = "Ptm not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ ptmInstance : ptmInstance ] }
    }

    def delete = {
        def ptmInstance = Ptm.get( params.id )
        if(ptmInstance) {
            // Allows control over Spring's Transactions
            Ptm.withTransaction{
            status ->
                try {
                    ptmInstance.delete()
                    flash.message = "Ptm ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Ptm.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Ptm ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()
                    
                    Ptm.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Ptm ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "Ptm not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def ptmInstance = Ptm.get( params.id )

        if(!ptmInstance) {
            flash.message = "Ptm not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ ptmInstance : ptmInstance ]
        }
    }

    def update = {
        def ptmInstance = Ptm.get( params.id )
        if(ptmInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(ptmInstance.version > version) {
                    
                    ptmInstance.errors.rejectValue("version", "ptm.optimistic.locking.failure", "Another user has updated this Ptm while you were editing.")
                    render(view:'edit',model:[ptmInstance:ptmInstance])
                    return
                }
            }
            ptmInstance.properties = params
            if(!ptmInstance.hasErrors() && ptmInstance.save()) {
                flash.message = "Ptm ${params.id} updated"
                redirect(action:show,id:ptmInstance.id)
            }
            else {
                render(view:'edit',model:[ptmInstance:ptmInstance])
            }
        }
        else {
            flash.message = "Ptm not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def ptmInstance = new Ptm()
        ptmInstance.properties = params
        return ['ptmInstance':ptmInstance]
    }

    def save = {
        def ptmInstance = new Ptm(params)
        if(!ptmInstance.hasErrors() && ptmInstance.save()) {
            flash.message = "Ptm ${ptmInstance.id} created"
            redirect(action:show,id:ptmInstance.id)
        }
        else {
            render(view:'create',model:[ptmInstance:ptmInstance])
        }
    }
}
