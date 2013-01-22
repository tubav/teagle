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
 *  This is the controller class for the OrganisationRole domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class OrganisationRoleController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render OrganisationRole.list() as XML
    }

    def rest_show = {
        def organisationRoleInstance = OrganisationRole.get(params.id)
        if (organisationRoleInstance) {
            response.setCharacterEncoding("utf-8")
            render organisationRoleInstance as XML
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
        OrganisationRole organisationRoleInstance = OrganisationRole.get(params.id)
        if (!organisationRoleInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        OrganisationRole.withTransaction{
            status ->
            try{
                organisationRoleInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                OrganisationRole.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("OrganisationRole ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                OrganisationRole.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("OrganisationRole ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        OrganisationRole organisationRoleInstance = OrganisationRole.get(params.id)
        if (!organisationRoleInstance) {
            SendNotFoundResponse()
        }

        organisationRoleInstance.properties = params.organisationRoleInstance

        if (organisationRoleInstance.validate()) {
            organisationRoleInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render organisationRoleInstance as XML
        } else {
            sendValidationFailedResponse(organisationRoleInstance, 403)
        }
    }

    private def sendValidationFailedResponse(organisationRoleInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                organisationRoleInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def organisationRoleInstance = new OrganisationRole(params.organisationRoleInstance)

        if(organisationRoleInstance.save()){
            response.setCharacterEncoding("utf-8")
            render organisationRoleInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    organisationRoleInstance?.errors?.fieldErrors?.each {err ->
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
        [ organisationRoleInstanceList: OrganisationRole.list( params ), organisationRoleInstanceTotal: OrganisationRole.count() ]
    }

    def show = {
        def organisationRoleInstance = OrganisationRole.get( params.id )

        if(!organisationRoleInstance) {
            flash.message = "OrganisationRole not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ organisationRoleInstance : organisationRoleInstance ] }
    }

    def delete = {
        def organisationRoleInstance = OrganisationRole.get( params.id )
        if(organisationRoleInstance) {
            // Allows control over Spring's Transactions
            OrganisationRole.withTransaction{
                status ->
                try{
                    organisationRoleInstance.delete()
                    flash.message = "OrganisationRole ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    OrganisationRole.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete OrganisationRole ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    OrganisationRole.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete OrganisationRole ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "OrganisationRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def organisationRoleInstance = OrganisationRole.get( params.id )

        if(!organisationRoleInstance) {
            flash.message = "OrganisationRole not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ organisationRoleInstance : organisationRoleInstance ]
        }
    }

    def update = {
        def organisationRoleInstance = OrganisationRole.get( params.id )
        if(organisationRoleInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(organisationRoleInstance.version > version) {
                    
                    organisationRoleInstance.errors.rejectValue("version", "organisationRole.optimistic.locking.failure", "Another user has updated this OrganisationRole while you were editing.")
                    render(view:'edit',model:[organisationRoleInstance:organisationRoleInstance])
                    return
                }
            }
            organisationRoleInstance.properties = params
            if(!organisationRoleInstance.hasErrors() && organisationRoleInstance.save()) {
                flash.message = "OrganisationRole ${params.id} updated"
                redirect(action:show,id:organisationRoleInstance.id)
            }
            else {
                render(view:'edit',model:[organisationRoleInstance:organisationRoleInstance])
            }
        }
        else {
            flash.message = "OrganisationRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def organisationRoleInstance = new OrganisationRole()
        organisationRoleInstance.properties = params
        return ['organisationRoleInstance':organisationRoleInstance]
    }

    def save = {
        def organisationRoleInstance = new OrganisationRole(params)
        if(!organisationRoleInstance.hasErrors() && organisationRoleInstance.save()) {
            flash.message = "OrganisationRole ${organisationRoleInstance.id} created"
            redirect(action:show,id:organisationRoleInstance.id)
        }
        else {
            render(view:'create',model:[organisationRoleInstance:organisationRoleInstance])
        }
    }
}
