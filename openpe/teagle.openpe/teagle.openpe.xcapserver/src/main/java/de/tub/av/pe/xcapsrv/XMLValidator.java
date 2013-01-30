/*
 * Copyright (C) 2009 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of the FOKUS XDMS - an XML Document Management Server
 * 
 * The FOKUS XDMS is proprietary software that is licensed
 * under the FhG FOKUS "SOURCE CODE LICENSE for FOKUS Open IMS COMPONENTS".
 * You should have received a copy of the license along with this 
 * program; if not, write to Fraunhofer Institute FOKUS, Kaiserin-
 * Augusta Allee 31, 10589 Berlin, GERMANY 
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * It has to be noted that this software is not intended to become 
 * or act as a product in a commercial context! It is a PROTOTYPE
 * IMPLEMENTATION for IMS technology testing and IMS application 
 * development for research purposes, typically performed in IMS 
 * test-beds. See the attached license for more details. 
 *
 * For a license to use this software under conditions
 * other than those described here, please contact Fraunhofer FOKUS 
 * via e-mail at the following address:
 *     info@open-ims.org
 *
 */

package de.tub.av.pe.xcapsrv;

import com.sun.syndication.io.XmlReader;

import de.tub.av.pe.xcapsrv.error.InternalServerErrorException;
import de.tub.av.pe.xcapsrv.error.NotUTF8ConflictException;
import de.tub.av.pe.xcapsrv.error.NotValidXMLFragmentConflictException;
import de.tub.av.pe.xcapsrv.error.NotWellFormedConflictException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


public class XMLValidator {
     private static final Log log = LogFactory.getLog(XMLValidator.class);
    /**
     *
     *
     */
//    public static boolean isQName(String name) {
//        String[] qName = name.split(":");
//        if (qName.length == 1) {
//            return XML11Char.isXML11ValidNCName(name);
//        } else if (qName.length == 2) {
//            return XML11Char.isXML11ValidNCName(qName[0]) && XML11Char.isXML11ValidNCName(qName[1]);
//        }
//        return false;
//    }

    /**
     * Validates if the specific string is a valid xml attribute value.
     * Specs say that an attr value is validated by the following regex:
     * <p/>
     * AttValue	   	::=	'"' ([^<&"] | Reference)* '"' |  "'" ([^<&'] | Reference)* "'"
     * Reference 	::= EntityRef | CharRef
     * EntityRef 	::= '&' Name ';'
     * CharRef		::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
     * <p/>
     * <p/>
     * NOTE: The specified string doesn't come with surroundings " or ' so we can't accept both chars!!!!
     *
     * @param value 
     */
//    public static void checkAttValue(String value) throws NotXMLAttributeValueConflictException{
//
//        try {
//
//            StringBuilder sb = new StringBuilder(value);
//
//            // check and remove char refs
//
//            // &#x [0-9a-fA-F]+ ;
//
//            Set<String> set = new HashSet<String>();
//            while (true) {
//                int begin = sb.indexOf("&#x");
//                if (begin > -1) {
//                    // found begin
//                    int end = sb.indexOf(";", begin + 3);
//                    if (end > -1) {
//                        // found an end
//                        set.add(sb.substring(begin + 3, end));
//                        sb = new StringBuilder(sb.substring(0, begin)).append(sb.substring(end + 1));
//                    } else {
//                        break;
//                    }
//                } else {
//                    break;
//                }
//            }
//
//            Pattern p = Pattern.compile("[0-9a-fA-F]+");
//            for (Iterator<String> i = set.iterator(); i.hasNext();) {
//                String t = i.next();
//                Matcher m = p.matcher(t);
//                if (!m.matches()) {
//                    throw new NotXMLAttributeValueConflictException();
//                }
//            }
//
//            // &# [0-9]+ ;
//
//            set = new HashSet<String>();
//            while (true) {
//                int begin = sb.indexOf("&#");
//                if (begin > -1) {
//                    // found begin
//                    int end = sb.indexOf(";", begin + 2);
//                    if (end > -1) {
//                        // found an end
//                        set.add(sb.substring(begin + 2, end));
//                        sb = new StringBuilder(sb.substring(0, begin)).append(sb.substring(end + 1));
//                    } else {
//                        break;
//                    }
//                } else {
//                    break;
//                }
//            }
//
//            p = Pattern.compile("[0-9]+");
//            for (Iterator<String> i = set.iterator(); i.hasNext();) {
//                String t = i.next();
//                Matcher m = p.matcher(t);
//                if (!m.matches()) {
//                    throw new NotXMLAttributeValueConflictException();
//                }
//            }
//
//            // check and remove entity refs
//            // & name ;
//
//            set = new HashSet<String>();
//            while (true) {
//                int begin = sb.indexOf("&");
//                if (begin > -1) {
//                    // found begin
//                    int end = sb.indexOf(";", begin + 1);
//                    if (end > -1) {
//                        // found an end
//                        set.add(sb.substring(begin + 1, end));
//                        sb = new StringBuilder(sb.substring(0, begin)).append(sb.substring(end + 1));
//                    } else {
//                        break;
//                    }
//                } else {
//                    break;
//                }
//            }
//
//            // check all names found
//            for (Iterator<String> i = set.iterator(); i.hasNext();) {
//                String name = i.next();
//                if (!XML11Char.isXML11ValidName(name)) {
//                    throw new NotXMLAttributeValueConflictException();
//                }
//            }
//
//            // check remaining chars
//
//            for (int i = 0; i < sb.length(); i++) {
//                if (sb.charAt(i) == '&' || sb.charAt(i) == '\'' || sb.charAt(i) == '"' || sb.charAt(i) == '<') {
//                    throw new NotXMLAttributeValueConflictException();
//                }
//            }
//
//        }
//        catch (Exception e) {
//            // parsing error
//            throw new NotXMLAttributeValueConflictException();
//        }
//
//    }

    public static String getUTF8String(InputStream is) throws NotUTF8ConflictException, InternalServerErrorException {
        // lets get the byte array in stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = is.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        byte[] data = bos.toByteArray();
        // now decode the bytes
        CharsetDecoder dec = Charset.forName("UTF8").newDecoder();
        try {
            return dec.decode(ByteBuffer.wrap(data)).toString();
        } catch (Exception e) {
            throw new NotUTF8ConflictException();
        }
    }

    public static Document getWellFormedDocument(Reader reader) throws NotWellFormedConflictException, InternalServerErrorException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document docu = parser.parse(new InputSource(reader));
            //testTheCreatedDocu(docu);
            return   docu;
        } catch (SAXException e) {
            throw new NotWellFormedConflictException();
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static Element getWellFormedDocumentFragment(Reader reader) throws NotValidXMLFragmentConflictException, InternalServerErrorException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document dummyDocument = parser.parse(new InputSource(reader));
            return dummyDocument.getDocumentElement();
        } catch (SAXException e) {

            throw new NotValidXMLFragmentConflictException();
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
    public static String getWellFormedElement(InputStream input) throws NotValidXMLFragmentConflictException, InternalServerErrorException, TransformerException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        String result = null;

            DocumentBuilder parser = factory.newDocumentBuilder();
            Document dummyDocument = parser.parse(input);
            result =  TextWriter.toString(dummyDocument.getDocumentElement());

       return result;
    }
    public static Reader getUTF8Reader(InputStream is) throws NotUTF8ConflictException, IOException {

      //  try {
            XmlReader reader = new XmlReader(is);
            if (reader.getEncoding().equals("UTF-8")) {
                // encoding ok, return reader
                return reader;
            }else{
                log.debug("the input is not UTF-8 encoded!!");
                 // encoding not ok, throw exception
                 throw new NotUTF8ConflictException();

            }
        /*} catch (Exception e) {
            logger.error(e);
            throw new InternalServerErrorException(e.getMessage());
        } */


    }

    public static boolean weaklyEquals(String xml1, String xml2) {

        // clean xml1 string
        xml1 = xml1.trim().replaceAll("\n", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "").replaceAll("\f", "");

        // clean xml2 string
        xml2 = xml2.trim().replaceAll("\n", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "").replaceAll("\f", "");

        return xml1.compareTo(xml2) == 0;

    }
}
