/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.field.address.rfc822;

import org.apache.james.mime4j.dom.address.Mailbox;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class AddressTest {
	
	
	/*private enum RFC{
		822,
		2822
		5322
	}
	
	private static interface Parser{
		
		boolean support(String rfc);
		
		void parse(String address, String rfc); //throws ParseException
	}*/

    @Test
    public void testSimpleMailbox() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("te.st@apache.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }

    
    @Test
    public void testDomainLiteralMailbox() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("te.st@[123.123.123.123]"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test(expected=ParseException.class)
    public void testDoubleDotMailbox() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("te..st@apache.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test(expected=ParseException.class)
    public void testDotStartMailbox() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader(".te..st@apache.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test(expected=ParseException.class)
    public void testUmlautsInMailbox() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("te.st@apäche.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test(expected=ParseException.class)
    public void testCR() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("\"test\r blah\"@iana.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test
    public void testCRLF() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("\"test\r\n blah\"@iana.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    @Test
    public void testLF() throws ParseException {
        RFC822AddressParser p = new RFC822AddressParser(new StringReader("\"test\n blah\"@iana.org"));
        ASTmailbox adr = p.parseMailbox();
        adr.dump(">");
    }
    
    
	//@Test
	public void xpath() throws Exception {
		List<String> failures = new ArrayList<String>();
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();

		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(AddressTest.class.getResourceAsStream("/rfc822test.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();

		String expression = "/tests/test";
		
		/*// read a string value
		String email = xPath.compile(expression).evaluate(document);

		// read an xml node using xpath
		Node node = (Node) xPath.compile(expression).evaluate(document,
				XPathConstants.NODE);*/

		// read a nodelist using xpath
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
				document, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeList.item(i);
			String id = node.getAttributes().getNamedItem("id").getTextContent();
			String address = xPath.compile("address").evaluate(node);
			String category = xPath.compile("category").evaluate(node);
			String diagnosis = xPath.compile("diagnosis").evaluate(node);
			
			System.out.println("##### Parse: "+id+" -> "+address+" -> "+category+" -> "+diagnosis);
			
			RFC822AddressParser p = new RFC822AddressParser(new StringReader(address));
	        boolean expectException = false;
			if(category.equals("ISEMAIL_ERR")) {
				expectException = true;
			} 
			
			
			try {
				p.parseAddress();
				
				if(expectException) {
					failures.add("Exception expected for "+id+" -> "+address+" -> "+category+" -> "+diagnosis);
					//Assert.fail("Exception expected for "+category);
				}
				
			} catch (ParseException e) {
				
				if(!expectException)
				{
					failures.add("Unexpected Exception for "+id+" -> "+address+" -> "+category+" -> "+diagnosis);
					//throw e;
				}
			}
			
	        
	        System.out.println();
		}
		
		for (Iterator iterator = failures.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
			
		}
	}
    
	
	@Test
	public void xpathrfc822() throws Exception {
		List<String> failures = new ArrayList<String>();
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();

		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(AddressTest.class.getResourceAsStream("/rfc822tests.xml"));

		XPath xPath = XPathFactory.newInstance().newXPath();

		String expression = "/tests/test";
		
		/*// read a string value
		String email = xPath.compile(expression).evaluate(document);

		// read an xml node using xpath
		Node node = (Node) xPath.compile(expression).evaluate(document,
				XPathConstants.NODE);*/

		// read a nodelist using xpath
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
				document, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeList.item(i);
			String id = xPath.compile("id").evaluate(node);
			String address = xPath.compile("address").evaluate(node);
			String valid = xPath.compile("valid").evaluate(node);
			String warning = xPath.compile("warning").evaluate(node);
			String tag = xPath.compile("tag").evaluate(node);
			String comment = xPath.compile("comment").evaluate(node);
			
			System.out.println("##### Parse: "+id+" -> "+address+" -> "+valid+" -> "+warning+" -> "+tag+" -> "+comment);
			
			RFC822AddressParser p = new RFC822AddressParser(new StringReader(address));
	        boolean expectException = false;
			if(valid.equals("false")) {
				expectException = true;
			} 
			
			
			try {
				p.parseAddress();
				
				if(expectException) {
					failures.add("Exception expected for "+id+" -> "+address+" -> "+valid+" -> "+warning+" -> "+tag+" -> "+comment);
					//Assert.fail("Exception expected for "+category);
				}
				
			} catch (ParseException e) {
				
				if(!expectException)
				{
					failures.add("Unexpected Exception for "+id+" -> "+address+" -> "+valid+" -> "+warning+" -> "+tag+" -> "+comment);
					//throw e;
				}
			}
			
	        
	        System.out.println();
		}
		
		for (Iterator iterator = failures.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
			
		}
		
		Assert.assertEquals(0, failures.size());
	}
	
	void parseRfc822(String address)
	{
		//parser 1
		//parser 2
		//parser 3
	}
	
	void parseRfc2822(String address)
	{
		//parser 1
				//parser 2
				//parser 3
	}
	
	void parseRfc5322(String address)
	{
		//parser 1
				//parser 2
				//parser 3
	}
	
	void parseRfcAll(String address)
	{
		
	}

	//@Test
	public void variourfcs() throws Exception {
		List<String> failures = new ArrayList<String>();
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
	
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(AddressTest.class.getResourceAsStream("/rfc_mail_tests.xml"));
	
		XPath xPath = XPathFactory.newInstance().newXPath();
	
		String expression = "/tests/test";
		
		/*// read a string value
		String email = xPath.compile(expression).evaluate(document);
	
		// read an xml node using xpath
		Node node = (Node) xPath.compile(expression).evaluate(document,
				XPathConstants.NODE);*/
	
		// read a nodelist using xpath
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
				document, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = (org.w3c.dom.Node) nodeList.item(i);
			String id = xPath.compile("id").evaluate(node);
			String address = xPath.compile("address").evaluate(node);
			String valid = xPath.compile("valid").evaluate(node);
			String category = xPath.compile("category").evaluate(node);
			String comment = xPath.compile("comment").evaluate(node);
			NodeList rfcSections = (NodeList) xPath.compile("rfc*").evaluate(
					document, XPathConstants.NODESET);
			
			if(valid == null) {
				//expect at least one rfc section
				if(rfcSections == null || rfcSections.getLength() == 0) {
					
					//test invalid
				}
				
			}
			else
			{
				boolean defaultValid = Boolean.parseBoolean(valid);
				
				
			}
			
			System.out.println("##### Parse: "+id+" -> "+address);
			
			RFC822AddressParser p = new RFC822AddressParser(new StringReader(address));
	        boolean expectException = false;
			if(valid.equals("false")) {
				expectException = true;
			} 
			
			
			try {
				p.parseAddress();
				
				if(expectException) {
					//failures.add("Exception expected for "+id+" -> "+address+" -> "+valid+" -> "+warning+" -> "+tag+" -> "+comment);
					//Assert.fail("Exception expected for "+category);
				}
				
			} catch (ParseException e) {
				
				if(!expectException)
				{
					//failures.add("Unexpected Exception for "+id+" -> "+address+" -> "+valid+" -> "+warning+" -> "+tag+" -> "+comment);
					//throw e;
				}
			}
			
	        
	        System.out.println();
		}
		
		for (Iterator iterator = failures.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
			
		}
	}
    
}
