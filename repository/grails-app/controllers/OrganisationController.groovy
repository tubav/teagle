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
 *  This is the controller class for the Organisation domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class OrganisationController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Organisation.list() as XML
    }

    def rest_show = {
        def organisationInstance = Organisation.get(params.id)
        if (organisationInstance) {
            response.setCharacterEncoding("utf-8")
            render organisationInstance as XML
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
        Organisation organisationInstance = Organisation.get(params.id)
        if (!organisationInstance) {
            SendNotFoundResponse()
        }
        
        // Allows control over Spring's Transactions
        Organisation.withTransaction{
            status ->
            try{
                // Need to delete any Ptms (Applications) that are associated to the Organisation first
                if(Application.countByProvider(organisationInstance)==1){
                    Application appInstance = Application.get(Application.findByProvider(organisationInstance).id)
                    appInstance.delete()
                }
                organisationInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Organisation.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Organisation ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Organisation.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Organisation ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        Organisation organisationInstance = Organisation.get(params.id)
        if (!organisationInstance) {
            SendNotFoundResponse()
        }

        organisationInstance.properties = params.organisationInstance

        //Handling multiple people entries
        if(request.XML.people.size()>0 && request.XML.people!=""){
            organisationInstance.properties.people.clear()
            for(int i=0;i<request.XML.people.size();i++){
                organisationInstance.properties.people.add(Person.get(request.XML.people.getAt(i).toInteger()))
            }
        }

        //Handling multiple hasResources entries
        if(request.XML.hasResources.size()>0 && request.XML.hasResources!=""){
            organisationInstance.properties.hasResources.clear()
            for(int i=0;i<request.XML.hasResources.size();i++){
                organisationInstance.properties.hasResources.add(i,Resource.get(request.XML.hasResources.getAt(i).toInteger()))
            }
        }
        
        if (organisationInstance.validate()) {
            organisationInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render organisationInstance as XML
        } else {
            sendValidationFailedResponse(organisationInstance, 403)
        }
    }

    private def sendValidationFailedResponse(organisationInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                organisationInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def organisationInstance = new Organisation(params.organisationInstance)

        //Handling multiple people entries
        if(request.XML.people.size()>0 && request.XML.people!=""){
            organisationInstance.people.clear()
            for(int i=0;i<request.XML.people.size();i++){
                organisationInstance.people.add(i,Person.get(request.XML.people.getAt(i).toInteger()))
            }
        }

        //Handling multiple hasResources entries
        if(request.XML.hasResources.size()>0 && request.XML.hasResources!=""){
            organisationInstance.hasResources.clear()
            for(int i=0;i<request.XML.hasResources.size();i++){
                organisationInstance.hasResources.add(i,Resource.get(request.XML.hasResources.getAt(i).toInteger()))
            }
        }

        if(organisationInstance.save()){
            response.setCharacterEncoding("utf-8")
            render organisationInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    organisationInstance?.errors?.fieldErrors?.each {err ->
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
        [ organisationInstanceList: Organisation.list( params ), organisationInstanceTotal: Organisation.count() ]
    }

    def show = {
        def organisationInstance = Organisation.get( params.id )

        if(!organisationInstance) {
            flash.message = "Organisation not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ organisationInstance : organisationInstance ] }
    }

    def delete = {
        def organisationInstance = Organisation.get( params.id )
        if(organisationInstance) {
            // Allows control over Spring's Transactions
            Organisation.withTransaction{
                status ->
                try{
                    // Need to delete any Ptms (Applications) that are associated to the Organisation first
                    if(Application.countByProvider(organisationInstance)==1){
                        Application appInstance = Application.get(Application.findByProvider(organisationInstance).id)
                        appInstance.delete()
                    }
                    organisationInstance.delete()
                    flash.message = "Organisation ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Organisation.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Organisation ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Organisation.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Organisation ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "Organisation not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def organisationInstance = Organisation.get( params.id )

        if(!organisationInstance) {
            flash.message = "Organisation not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ organisationInstance : organisationInstance ]
        }
    }

    def update = {
        def organisationInstance = Organisation.get( params.id )
        if(organisationInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(organisationInstance.version > version) {
                    
                    organisationInstance.errors.rejectValue("version", "organisation.optimistic.locking.failure", "Another user has updated this Organisation while you were editing.")
                    render(view:'edit',model:[organisationInstance:organisationInstance])
                    return
                }
            }
            organisationInstance.properties = params
            if(!organisationInstance.hasErrors() && organisationInstance.save()) {
                flash.message = "Organisation ${params.id} updated"
                redirect(action:show,id:organisationInstance.id)
            }
            else {
                render(view:'edit',model:[organisationInstance:organisationInstance])
            }
        }
        else {
            flash.message = "Organisation not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def organisationInstance = new Organisation()
        organisationInstance.properties = params
        return ['organisationInstance':organisationInstance]
    }

    def save = {
        def organisationInstance = new Organisation(params)
        if(!organisationInstance.hasErrors() && organisationInstance.save()) {
            flash.message = "Organisation ${organisationInstance.id} created"
            redirect(action:show,id:organisationInstance.id)
        }
        else {
            render(view:'create',model:[organisationInstance:organisationInstance])
        }
    }
}

