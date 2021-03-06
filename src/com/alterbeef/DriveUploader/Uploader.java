package com.alterbeef.DriveUploader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Uploader {
	private String account = null;
	private String password = null;
	private DocsService service;
	private DocumentListEntry collection;

	public Uploader(String[] args) {
		if(! testFile(args[0])){		// test declared file
			System.err.println("Exiting - Error with file: " + args[0].toString());
			return;
		}
		if(! readXML()){				// read config from xml
			System.err.println("Exiting - Error with XML");
			return;
		}
		if(! testLogin()){				// test login
			System.err.println("Exiting - Error with login");
			return;
		}
		if(args.length == 2)			// test collection
			if(! testCollection(args[1])){
				System.err.println("Exiting - Error with collection");
				return;
			}
			// create doc
			// push doc
	}

	public static void main(String[] args){
		if(args.length == 0 || args.length > 2){
			System.out.println("\nUsage: uploader <file> ");
			System.out.println("\n       uploader <file> <collection>");
			return;
		}
		new Uploader(args);
	}

	private boolean testFile(String _file){
		File sourceFile = new File(_file);
		if(sourceFile.exists()){
			if(sourceFile.isFile()){
				if(sourceFile.canRead()){
					System.out.println("File test positive");
					return true;
				}else{
					System.out.println("Cannot read file: " + sourceFile.getAbsolutePath());
				}
			}else{
				System.out.println("Not a file: " + sourceFile.getAbsolutePath());
			}
		}else{
			System.out.println("File does not exist: " + sourceFile.getAbsolutePath());
		}
		return false;
	}

	private boolean readXML(){
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse("Uploader.xml");

            Element doc = dom.getDocumentElement();

            account = getTextValue(account, doc, "account");
            if (account != null) {
                if (!account.isEmpty()){
                	System.out.println("Read account: " + account);
                }else{
                	System.out.println("Account name empty");
                	return false;
                }
            }else{
            	System.out.println("Could not read XML account");
            	return false;
            }

            password = getTextValue(password, doc, "application_specific_password");
            if (password != null) {
                if (!password.isEmpty()){
                	System.out.println("Read password: length = " + password.length());
                	return true;
                }
                else
                	System.out.println("Password empty");
            }else{
            	System.out.println("Could not read XML password");
            }
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return false;
	}

	private String getTextValue(String def, Element doc, String tag) {
	    String value = def;
	    NodeList nl;
	    nl = doc.getElementsByTagName(tag);
	    if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
	        value = nl.item(0).getFirstChild().getNodeValue();
	    }
	    return value;
	}

	private boolean testLogin(){
		service = new DocsService("alterbeef-DriveUploader-v1");
	    try {
			service.setUserCredentials(account, password);
			System.out.println("Service Info: " + service.getServiceVersion());
			return true;
		} catch (AuthenticationException e) {
			System.out.println("Failed to authenticate");
			e.printStackTrace();
		}
	    return false;
	}

	private boolean testCollection(String collectionStr){
		URL feedUri;
		DocumentListFeed feed;
		try {
			service.setProtocolVersion(DocsService.Versions.V2);
			feedUri = new URL("https://docs.google.com/feeds/documents/private/full/-/folder?showfolders=true");
			feed = service.getFeed(feedUri, DocumentListFeed.class);
			System.out.println("Searched for " + collectionStr + ", found " + feed.getEntries().size());
			for (DocumentListEntry entry : feed.getEntries()) {
//			    String resourceId = entry.getResourceId();
//			    System.out.println(" -- Document(" + resourceId + "/" + entry.getTitle().getPlainText() + ")");
			    if(entry.getTitle().getPlainText().equalsIgnoreCase(collectionStr)){
			    	collection = entry;
			    	System.out.println("Collection found");
			    	return true;
			    }
			}
			System.out.println("Collection \"" + collectionStr + "\" not found");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return false;
	}
}
