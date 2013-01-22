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
 *  This is the controller class for the Person domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class PersonController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Person.list() as XML
    }

    def rest_show = {
        def personInstance = Person.get(params.id)
        if (personInstance) {
            response.setCharacterEncoding("utf-8")
            render personInstance as XML
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
        Person personInstance = Person.get(params.id)
        if (!personInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        Person.withTransaction{
            status ->
            try{
                deleteVcts(personInstance)
                removeFromPeopleAssociations(personInstance)
                personInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Person.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Person ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Person.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Person ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
         //If no person role defined, set to Panlab Customer
        if(params.personRoles==null){
            params.personRoles=PersonRole.findByName(grailsApplication.config.defaultRole)
        }

        Person personInstance = Person.get(params.id)

        if (!personInstance) {
            SendNotFoundResponse()
        }

        // As Person is in a bi-directional relationship with organisation, need to remove the associations before updating.
	/*
        def orgs = []
        personInstance.organisations.each{
            orgs << it
        }
        orgs.each {
            removeFromPeopleAssociations2(it,personInstance)
        }
	*/
	// Encoding the password if it has changed otherwise leave alone
        if(params.personInstance.password == null || params.personInstance.password == ""){
            params.personInstance.password = personInstance.password
        }else{
            if(!(params.personInstance.password.size() < 4) && (params.personInstance.password != personInstance.password) && (!isHashed(params.personInstance.password))){
                params.personInstance.password = params.personInstance.password.encodeAsPassword()
            }else if(isHashed(params.personInstance.password)){
                personInstance.password = params.personInstance.password
            }else{
                render contentType: "text/xml", {
                    errors {
                        message("Password cannot be less than 4 characters long or null.")
                    }
                }
            }
        }
        personInstance.properties = params.personInstance

	println "setting roles: " + request.XML.personRoles.size()
        if(request.XML.personRoles.size()>0 && request.XML.personRoles!=""){
            personInstance.properties.personRoles.clear()
            for(int i=0;i<request.XML.personRoles.size();i++){
	        println PersonRole.get(request.XML.personRoles.getAt(i).toInteger())
                personInstance.properties.personRoles.add(PersonRole.get(request.XML.personRoles.getAt(i).toInteger()))
            }
        }


        if (personInstance.validate()) {
            personInstance.save(flush: true)
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render personInstance as XML
        } else {
            sendValidationFailedResponse(personInstance, 403)
        }
    }

    private def sendValidationFailedResponse(personInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                personInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def personInstance = new Person(params.personInstance)

        //If no person role defined, set to Panlab Customer
        println "params.personInstance "+params.personInstance

        if(request.XML.personRoles.size()>0 && request.XML.personRoles!=""){
            personInstance.properties.personRoles.clear()
            for(int i=0;i<request.XML.personRoles.size();i++){
                personInstance.properties.personRoles.add(PersonRole.get(request.XML.personRoles.getAt(i).toInteger()))
            }
        }
	else

	        if(params.personInstance.personRoles==null){
       	     params.personInstance.personRoles=PersonRole.findByName(grailsApplication.config.defaultRole)
       	 }

	// Encoding the password if not already hashed.
        if(params.personInstance.password != null){
            if(isHashed(params.personInstance.password)){
                personInstance.password = params.personInstance.password
            }else{
                if(!(params.personInstance.password.size() < 4)){
                       personInstance.password = params.personInstance.password.encodeAsPassword()
                }else{
                    render contentType: "text/xml", {
                        errors {
                            message("Password cannot be less than 4 characters long.")
                        }
                    }
                }
            }
        }else{
            render contentType: "text/xml", {
                errors {
                    message("Password cannot be null or less than 4 characters long.")
                }
            }
        }

        if(personInstance.save(flush: true)){
            response.setCharacterEncoding("utf-8")
            render personInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    personInstance?.errors?.fieldErrors?.each {err ->
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
        [ personInstanceList: Person.list( params ), personInstanceTotal: Person.count() ]
    }

    def show = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ personInstance : personInstance ] }
    }

    def delete = {
        def personInstance = Person.get( params.id )
        if(personInstance) {
            // Allows control over Spring's Transactions
            Person.withTransaction{
                status ->
                try{
                    deleteVcts(personInstance)
                    removeFromPeopleAssociations(personInstance)
                    personInstance.delete()
                    flash.message = "Person ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Person.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Person ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Person.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Person ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ personInstance : personInstance ]
        }
    }

    def update = {
        def personInstance = Person.get( params.id )

        // As Person is in a bi-directional relationship with organisation, need to remove the associations before updating.
        def orgs = []
        personInstance.organisations.each{
            orgs << it
        }
        orgs.each {
            removeFromPeopleAssociations2(it,personInstance)
        }
        
        if(personInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(personInstance.version > version) {
                    personInstance.errors.rejectValue("version", "person.optimistic.locking.failure", "Another user has updated this Person while you were editing.")
                    render(view:'edit',model:[personInstance:personInstance])
                    return
                }
            }
//            // Encoding the password if it has changed otherwise leave alone
//            if(params.password != null){
//              if(!(params.password.size() < 4) && (params.password != personInstance.password) && (!isHashed(params.password))){
//                   params.password = params.password.encodeAsPassword()
//               }else{
//                   render contentType: "text/xml", {
//                        errors {
//                            message("Password cannot be less than 4 characters long or null.")
//                        }
//                    }
//               }
//            }
//            // Encoding the password if it has changed otherwise leave alone
//            if(params.password == null || params.personInstance.password == ""){
//                params.password = personInstance.password
//            }else{
//                if(!(params.password.size() < 4) && (params.password != personInstance.password) && (!isHashed(params.password))){
//                   params.password = params.password.encodeAsPassword()
//            }else if(isHashed(params.password)){
//                params.password = params.password
//            }else{
//                render contentType: "text/xml", {
//                    errors {
//                        message("Password cannot be less than 4 characters long or null.")
//                    }
//                }
//            }
//        }
            personInstance.properties = params

            if(!personInstance.hasErrors() && personInstance.save(flush: true)) {
                flash.message = "Person ${params.id} updated"
                redirect(action:show,id:personInstance.id)
            }
            else {
                render(view:'edit',model:[personInstance:personInstance])
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def personInstance = new Person()
        personInstance.properties = params

        return ['personInstance':personInstance]
    }

    def save = {
        //If no person role defined, set to Panlab Customer
        if(params.personRoles==null){
            params.personRoles = PersonRole.findByName(grailsApplication.config.defaultRole)
        }

        def personInstance = new Person(params)

        // Encoding the password if not already hashed.
        if(params.password != null){
            if(isHashed(params.password)){
                personInstance.password = params.password
            }else{
                if(!(params.password.size() < 4)){
                       personInstance.password = params.password.encodeAsPassword()
                }else{
                    flash.message = "Password cannot be less than 4 characters long."
                }
            }
        }else{
            flash.message = "Password cannot be empty or less than 4 characters long."
        }

        if(!personInstance.hasErrors() && personInstance.save(flush: true)) {
            flash.message = "Person ${personInstance.id} created"
            redirect(action:show,id:personInstance.id)
        }
        else {
            render(view:'create',model:[personInstance:personInstance])
        }
    }

    /**
     * Removes a personInstance from all Organisations that it's associated with.
     */
    def removeFromPeopleAssociations(personInstance){
        def temp=[]
        personInstance.organisations.each{
            temp << it
        }
        temp.each {
            it.removeFromPeople(personInstance)
        }
    }

    /**
     * Removes a personInstance from a particular Organisation that it's associated with.
     */
    def removeFromPeopleAssociations2(orgInstance, personInstance){
        def temp=[]
        orgInstance.people.each{
            if(it.id==personInstance.id){
                temp << it
            }
        }
        temp.each {
            orgInstance.removeFromPeople(it)
        }
    }

    /**
     * Need to delete any Vcts that are associated to the Person first
     */
    def deleteVcts(personInstance){
        if(Vct.countByUser(personInstance)>=1){
            Vct.findAllByUser(personInstance).each(){
                Vct vctInstance = Vct.get(it.id)
                vctInstance.delete()
            }
        }
    }

    boolean isHashed(String pass){
        return pass.matches("[a-fA-F0-9]{32}")
    }
}

