<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:include page="fragments/header.jsp">
	<jsp:param name="boxes" value="no" />
</jsp:include>

<jsp:include page="fragments/nav.jsp">
	<jsp:param name="current" value="" />
</jsp:include>

<!-- begin: center column -->
<div id="col3"><!--###col3### begin -->
	<div id="col3_content" class="clearfix"><!--###col3_content### begin -->
		<div id="col3_innen" class="floatbox">
			<h2>Tutorial at 3rd Future Internet Symposium 2010 (FIS 2010)</h2>
			<h3>Federation of Future Internet Research & Experimentation (FIRE) Facilities - “Teagle” as a Tool for Generic Resource Federation</h3>
			<p>Tutorial will take place as part of <a href="http://www.fis2010.org/program/tutorials-menu">FIS 2010</a>.
			<BR>
			Tentative date: September 20th 2010, morning session
      
      <h3>Tutorial Outline</h3>
       Teagle is the central coordination and testbed deployment engine used in Panlab, a large scale federated experimental facility. Teagle allows the setup of distributed testbeds using ICT resources
provided by Panlab. Such resources include general purpose machines, dedicated devices or complex
software systems such as Next Generation Network (NGN) enablers and an Evolved Packet Core
(EPC) for mobile and fixed NGN related testbeds.<br>
The resources can be booked and configured upon demand serving specific testing or
experimentation needs. Users of this facility and its underlying infrastructure are research
and development teams or individuals from industry and academia.
<BR>
<BR>
In this half-day tutorial participants will learn:
<ul>
<li> About the FIRE initiative as the driver for experimentally driven Future Internet research in
Europe
<li> About the Teagle architecture and its core components</li>
<li> About the Panlab framework and its operational procedures</li>
<li> How to use the creation environment provided by Teagle to design custom testbed setups</li>
<li> How to make use of such a testbed for experiments</li>
<li> How to join the federation, commit own resources, and use resources provided by other partners</li>
</ul>
The tutorial aims at facilitating the understanding of the abstract federation framework and services provided
by Panlab. Resource federation is a concept for sharing heterogeneous resources across various administrative
domains (i.e. multiple autonomous organizations) to increase the usage of available resources. The
benefit provided to users of the federation is the seamless access to a large collection of resources
via unified interfaces. Resource providers benefit from increasing their resource usage and some sort
of compensation. A federation organization maintains relationships with all stakeholders and offers
central services to ensure to overall operation.<BR>
The main focus of this tutorial is on Teagle, its inner mechanisms and
technologies, enabling participants to provide and/or use resources provided via Teagle.
Therefore, target audience for this tutorial are researchers from industry and academia that are
interested in either joining the Panlab federation or using its services. Furthermore, the inner
workings of Teagle and the underlying federation framework are interesting for researchers from
resource management, resource federation, distributed computing, and cloud computing fields.
<BR>
The presentation style will be interactive, including several screen casts and a live demonstration of
the Teagle framework and tools. It will also include a practical session covering the implementation
details of a resource adaptor. Resource adaptors are implemented by Panlab resource providers and
act like a device driver controlling resources within an administrative domain. 

<h3>Tutorial Schedule</h3>
The Tutorial covers five parts:
<ul> 
<li>Part 01: Introduction ~ 40 min</li>
<li>Part 02: Panlab Concept & Architecture ~ 40 min</li>
15 Minutes Break
<li>Part 03: Use Case ~ 30 min</li>
<li>Part 04: VCT Tool videos & demo ~ 20 min</li>
15 Minutes Break
<li>Part 05: How to join, how to commit resources ~ 20 min</li>
</ul>

<h3>About the presenters</h3>
<u>Sebastian Wahle, Fraunhofer FOKUS, sebastian.wahle@fokus.fraunhofer.de</u>
<BR>
Sebastian leads the Evolving Infrastructure and Services group at NGNI within
the Fraunhofer FOKUS institute in Berlin. The group is active in a number of
national and international R&D projects in the Future Internet field and
supports the commercial Fraunhofer NGN testbed deployments at customer’s
premises worldwide. The group’s research activities are centered around large
scale infrastructure federation, cross-layer monitoring and management, as well as cloud computing
for NGNs. Sebastian received a Diploma-Engineer degree in Industrial Engineering and Management
from the Technical University of Berlin. His personal research interests include Resource Federation
Frameworks and Service Oriented Architectures.
<BR>
<BR>
<u>Konrad Campowsky, Technische Universität Berlin, AV, konrad.campowsky@tu-berlin.de</u>
<BR>
Konrad has joined the AV (Architektur der Vermittlungsknoten) department at
Technische Universität Berlin in 2010, where he is working as a researcher in
the fields of testbed management and network domain federation. In
collaboration with the Fraunhofer FOKUS Institute, his current work focuses on
extending the testbed federation concept towards service and application levels and automating the
process of composite, cross-domain service creation and management.

</p>
		</div>
	</div>
	<div class="clear">&nbsp;</div>
</div>
<!-- end: center column -->

<%@ include file="fragments/footer.jsp" %>