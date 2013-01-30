-- phpMyAdmin SQL Dump
-- version 3.3.2deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 08, 2011 at 02:47 PM
-- Server version: 5.1.41
-- PHP Version: 5.3.2-1ubuntu4.7

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `broker`
--

-- --------------------------------------------------------

--
-- Table structure for table `drlpolicies`
--
CREATE database broker;

use broker;

CREATE TABLE IF NOT EXISTS `drlpolicies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `identity` varchar(200) NOT NULL,
  `idtype` varchar(200) NOT NULL,
  `scope` varchar(200) NOT NULL,
  `event` varchar(200) NOT NULL,
  `priority` int(11) NOT NULL,
  `policy` text NOT NULL,
  UNIQUE KEY `identifiers` (`identity`,`idtype`,`scope`,`event`,`priority`),
  KEY `id` (`id`),
  FULLTEXT KEY `event` (`event`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=278 ;

--
-- Dumping data for table `drlpolicies`
--

INSERT INTO `drlpolicies` (`id`, `identity`, `idtype`, `scope`, `event`, `priority`, `policy`) VALUES
(273, 'XenNode', 'resource', 'Target', 'bookResource', 0, '<?xml version="1.0" encoding="UTF-8"?> \n <package name="xennode.target.bookResource"  \n	xmlns="http://drools.org/drools-5.0" \n	xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" \n	xs:schemaLocation="http://drools.org/drools-5.0 drools.org/drools-5.0.xsd"> \n<import name="de.tub.av.pe.eval.drools.PEInputRequest" /> \n<import name="de.tub.av.pe.eval.drools.DrlActionsManager" /> \n<import name="de.tub.av.pe.eval.drools.DrlAction" /> \n<import name="de.tub.av.pe.eval.drools.Parameter" /> \n<import name="de.tub.av.pe.eval.drools.Utils" /> \n\n\n\n\n<rule name="r1">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n\n\n</pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("doPrint");\r\n  action.addAttribute("message", "Booking resource");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule>\n</package>'),
(275, 'Fraunhofer FOKUS', 'organisation', 'Originator', 'bookResource', 0, '<?xml version="1.0" encoding="UTF-8"?> \n <package name="identity.scope.event"  \n	xmlns="http://drools.org/drools-5.0" \n	xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" \n	xs:schemaLocation="http://drools.org/drools-5.0 drools.org/drools-5.0.xsd"> \n<import name="de.tub.av.pe.eval.drools.PEInputRequest" /> \n<import name="de.tub.av.pe.eval.drools.DrlActionsManager" /> \n<import name="de.tub.av.pe.eval.drools.DrlAction" /> \n<import name="de.tub.av.pe.eval.drools.Parameter" /> \n<import name="de.tub.av.pe.eval.drools.Utils" /> \n\n\n\n\n<rule name="r1">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n\n\n</pattern>\n\n<pattern identifier="p" object-type="Parameter" >\n<field-constraint field-name="name"> \n<literal-restriction evaluator="==" value="resource/ptm" />\n\n</field-constraint>\n<field-constraint field-name="value"> \n<literal-restriction evaluator="==" value="tssg-ptm" />\n\n</field-constraint>\n\n\n</pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("denyRequest");\r\n  action.addAttribute("message", "Users of FOKUS are not allowed to use tssg PTM");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule><rule name="r2">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n<field-constraint field-name="targetIdentities"> \n<literal-restriction evaluator="contains" value="Node" />\n\n</field-constraint>\n\n\n</pattern>\n\n<pattern identifier="p" object-type="Parameter" >\n\n\n</pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("denyRequest");\r\n  action.addAttribute("message", "Users of FOKUS are not allowed to select Node resources");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule>\n</package>'),
(276, 'Fraunhofer FOKUS', 'organisation', 'Target', 'bookResource', 0, '<?xml version="1.0" encoding="UTF-8"?> \n <package name="identity.scope.event"  \n	xmlns="http://drools.org/drools-5.0" \n	xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" \n	xs:schemaLocation="http://drools.org/drools-5.0 drools.org/drools-5.0.xsd"> \n<import name="de.tub.av.pe.eval.drools.PEInputRequest" /> \n<import name="de.tub.av.pe.eval.drools.DrlActionsManager" /> \n<import name="de.tub.av.pe.eval.drools.DrlAction" /> \n<import name="de.tub.av.pe.eval.drools.Parameter" /> \n<import name="de.tub.av.pe.eval.drools.Utils" /> \n\n\n\n\n<rule name="r1">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n\n\n</pattern>\n\n<pattern identifier="p" object-type="Parameter" >\n\n\n</pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("doPrint");\r\n  action.addAttribute("message", "Print your message");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule>\n</package>'),
(277, 'XenNode', 'resource', 'Originator', 'connectResources', 0, '<?xml version="1.0" encoding="UTF-8"?> \n <package name="identity.scope.event"  \n	xmlns="http://drools.org/drools-5.0" \n	xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" \n	xs:schemaLocation="http://drools.org/drools-5.0 drools.org/drools-5.0.xsd"> \n<import name="de.tub.av.pe.eval.drools.PEInputRequest" /> \n<import name="de.tub.av.pe.eval.drools.DrlActionsManager" /> \n<import name="de.tub.av.pe.eval.drools.DrlAction" /> \n<import name="de.tub.av.pe.eval.drools.Parameter" /> \n<import name="de.tub.av.pe.eval.drools.Utils" /> \n\n\n\n\n<rule name="r1">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n<field-constraint field-name="targetIdentities"> \n<literal-restriction evaluator="contains" value="XenNode" />\n\n</field-constraint>\n<field-constraint field-name="targetIdentityType"> \n<literal-restriction evaluator="==" value="resource" />\n\n</field-constraint>\n\n\n</pattern>\n\n<pattern identifier="p" object-type="Parameter" >\n<field-constraint field-name="name"> \n<literal-restriction evaluator="==" value="connectionType" />\n\n</field-constraint>\n<field-constraint field-name="value"> \n<literal-restriction evaluator="==" value="contains" />\n\n</field-constraint>\n\n\n</pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("denyRequest");\r\n  action.addAttribute("message", "This resources should not connect");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule>\n</package>'),
(272, 'ibo', 'user', 'Originator', 'bookVct', 0, '<?xml version="1.0" encoding="UTF-8"?> \n <package name="ibo.originator.bookVct"  \n	xmlns="http://drools.org/drools-5.0" \n	xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" \n	xs:schemaLocation="http://drools.org/drools-5.0 drools.org/drools-5.0.xsd"> \n<import name="de.tub.av.pe.eval.drools.PEInputRequest" /> \n<import name="de.tub.av.pe.eval.drools.DrlActionsManager" /> \n<import name="de.tub.av.pe.eval.drools.DrlAction" /> \n<import name="de.tub.av.pe.eval.drools.Parameter" /> \n<import name="de.tub.av.pe.eval.drools.Utils" /> \n\n\n\n\n<rule name="r1">\n<rule-attribute name="dialect" value="mvel" />\n\n<lhs><pattern identifier="f" object-type="PEInputRequest" >\n\n\n</pattern>\n\n<pattern identifier="n" object-type="Number" >\n<field-constraint field-name="this"> \n<literal-restriction evaluator="&gt;" value="2" />\n\n</field-constraint>\n\n\n<field-constraint field-name="this"> \n<literal-restriction evaluator="&gt;" value="2" />\n\n</field-constraint> <from> <accumulate> <pattern object-type="Parameter" >\n<field-constraint field-name="name"> \n<literal-restriction evaluator="==" value="resource/type" />\n\n</field-constraint>\n<field-constraint field-name="value"> \n<literal-restriction evaluator="==" value="XenNode" />\n\n</field-constraint>\n\n\n</pattern>\n<external-function evaluator="count" expression="1"/> </accumulate> </from> </pattern>\n\n<pattern identifier="actionMng" object-type="DrlActionsManager" >\n\n\n</pattern>\n\n\n</lhs><rhs>  DrlAction action = new DrlAction("denyRequest");\r\n  action.addAttribute("message", "Print your message");\r\n  actionMng.execute(drools.getRule(), action)\r\n</rhs>\n</rule>\n</package>');
