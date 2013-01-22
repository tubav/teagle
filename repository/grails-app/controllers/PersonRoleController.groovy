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
 *  This is the controller class for the PersonRole domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class PersonRoleController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render PersonRole.list() as XML
    }

    def rest_show = {
        def personRoleInstance = PersonRole.get(params.id)
        if (personRoleInstance) {
            response.setCharacterEncoding("utf-8")
            render personRoleInstance as XML
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
        PersonRole personRoleInstance = PersonRole.get(params.id)
        if (!personRoleInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        PersonRole.withTransaction{
            status ->
            try{
                personRoleInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                PersonRole.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("PersonRole ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                PersonRole.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("PersonRole ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        PersonRole personRoleInstance = PersonRole.get(params.id)
        if (!personRoleInstance) {
            SendNotFoundResponse()
        }

        personRoleInstance.properties = params.personRoleInstance

        if (personRoleInstance.validate()) {
            personRoleInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render personRoleInstance as XML
        } else {
            sendValidationFailedResponse(personRoleInstance, 403)
        }
    }

    private def sendValidationFailedResponse(personRoleInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                personRoleInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def personRoleInstance = new PersonRole(params.personRoleInstance)

        if(personRoleInstance.save()){
            response.setCharacterEncoding("utf-8")
            render personRoleInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    personRoleInstance?.errors?.fieldErrors?.each {err ->
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
        [ personRoleInstanceList: PersonRole.list( params ), personRoleInstanceTotal: PersonRole.count() ]
    }

    def show = {
        def personRoleInstance = PersonRole.get( params.id )

        if(!personRoleInstance) {
            flash.message = "PersonRole not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ personRoleInstance : personRoleInstance ] }
    }

    def delete = {
        def personRoleInstance = PersonRole.get( params.id )
        if(personRoleInstance) {
            // Allows control over Spring's Transactions
            PersonRole.withTransaction{
                status ->
                try{
                    personRoleInstance.delete()
                    flash.message = "PersonRole ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    PersonRole.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete PersonRole ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    PersonRole.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete PersonRole ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "PersonRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def personRoleInstance = PersonRole.get( params.id )

        if(!personRoleInstance) {
            flash.message = "PersonRole not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ personRoleInstance : personRoleInstance ]
        }
    }

    def update = {
        def personRoleInstance = PersonRole.get( params.id )
        if(personRoleInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(personRoleInstance.version > version) {
                    
                    personRoleInstance.errors.rejectValue("version", "personRole.optimistic.locking.failure", "Another user has updated this PersonRole while you were editing.")
                    render(view:'edit',model:[personRoleInstance:personRoleInstance])
                    return
                }
            }
            personRoleInstance.properties = params
            if(!personRoleInstance.hasErrors() && personRoleInstance.save()) {
                flash.message = "PersonRole ${params.id} updated"
                redirect(action:show,id:personRoleInstance.id)
            }
            else {
                render(view:'edit',model:[personRoleInstance:personRoleInstance])
            }
        }
        else {
            flash.message = "PersonRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def personRoleInstance = new PersonRole()
        personRoleInstance.properties = params
        return ['personRoleInstance':personRoleInstance]
    }

    def save = {
        def personRoleInstance = new PersonRole(params)
        if(!personRoleInstance.hasErrors() && personRoleInstance.save()) {
            flash.message = "PersonRole ${personRoleInstance.id} created"
            redirect(action:show,id:personRoleInstance.id)
        }
        else {
            render(view:'create',model:[personRoleInstance:personRoleInstance])
        }
    }
}
