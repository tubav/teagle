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
 */

The source code for the Panlab Core Repository is contained within this zip file. The code provided has been written for the Panlab project using the Grails open source project and deployed onto an Apache Tomcat 6 webserver using MySQL as the Datasource.

All code is released under the Apache License Version 2.0 in keeping with the original Grails distribution.

The Grails framework generates many files, these have been marked as such and where these files have been modified has been highlighted.

This codebase is being released as part of the Panlab project in order to be disseminated as an Open Source project.

Instructions to run this code:

1.  Install and setup Grails v1.2.1

2.  To run the application within Grails in development mode use the command "grails run-app"
    Note: The tomcat security will have to be removed as grails will run the application in it's own hsql database. Simply, remove the <security-constraint> from /src/templates/war/web.xml


To install on Apache Tomcat using a Mysql database:

1.  Install and setup Apache Tomcat Server (http://tomcat.apache.org/).

2.  Copy CustomRealm.jar and the mysql java connector jar available here (http://dev.mysql.com/downloads/connector/j/) to your tomcat installation /lib/ directory. This setups the security realm for the repo.

3.  Copy CoreRepository.xml to your tomcat installation \conf\Catalina\localhost\ directory.
    This file defines the CustomRealm tables and columns.

4.  Ensure tomcat-users.xml has a manager role defined:
	<role rolename="manager"/>	
	<user username="root" password="r00t" roles="manager"/>

5.  Generate the CoreRepository war by running the grails command within the CoreRepository root directory, e.g. grails war CoreRepository.war

6.  Install the CoreRepository war via the tomcat administration console or by directly copying the war file into your tomcat installation /webapps/ directory.

7.  Install and configure your database. The CoreRepository was tested using Mysql (http://www.mysql.com/) but it should also work with other databases.

8.  Create a database called repository using the command:
	
	create database repository;

9.  Start tomcat

10. Browse to http://localhost:8080/CoreRepository
    Note: There will be one admin user with a username set to "root" and password set to "r00t" to allow initial access to the application.

11. See documentation on the Panlab wiki for more information, http://trac.panlab.net/trac/wiki/TeagleRepository.




