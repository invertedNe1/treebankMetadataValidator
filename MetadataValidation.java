import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import java.net.MalformedURLException;
import java.net.URL;
import org.xml.sax.SAXException;
import java.util.ArrayList;

//imports for attribute configuration (could be in another file perhaps)
//import java.io.File;
//import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MetadataValidation {
	
	// class variables
	public ArrayList<File> fileEntries = new ArrayList<File>(); //potential place to store files in a folder.
	//square brackets []
	
	/**
	 * constructor. Performs metadata validation for a single file, or for all files in a directory.
	 */
	public MetadataValidation(File file) throws FileNotFoundException, IOException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata")) {
					//either do helper methods here, or all at once. Store in class variable, then can easier retrieve and perform other operations later?
					//data structure to format validity? for each file. and print part where it is not well-formatted? might know more when implementing schema
					if (!isWellFormed(fileEntry)) {
						System.out.format("XML in file '%s' in directory '%s' is not well-formed.", fileEntry, 	file);
					}
				}
			}
		}
		/** Would print out the file. How to determine how many/which files to print out?
		 * for (File fileEntry: fileEntries) {
			String xml = fileIntoString(fileEntry);
			isWellFormed(fileEntry);
		}*/			
		else {
			if(!isWellFormed(file)) {
				System.out.format("XML in file '%s' is not well-formatted", file);
			}
		}			
	}
	
	/**
	 * helper method. Checks if XML in metadata file is well formed. 
	 * @throws IOException 
	 */
	public static boolean isWellFormed(File file) throws IOException {
		File schemaFile = new File("/src/test/resources/sample/MetadataValidatorSchema.xsd");
		Source xmlFile = new StreamSource(file);
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
		  Schema schema = schemaFactory.newSchema(schemaFile);
		  Validator validator = schema.newValidator();
		  validator.validate(xmlFile);
		  System.out.println(xmlFile.getSystemId() + " is valid");
		  return true;
		} catch (SAXException e) {
		  System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
		} catch (IOException e) {}
		return false;
		/**
		 * if not true:
		 * print specific file name and error
		 */
	}
	
	// Turns xml file into string for parsing or printing.
	public static String fileToString(File file) throws FileNotFoundException, IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String output = "", line = "";
		while ((line = reader.readLine()) != null) {
			output += line + "\n"; //correctness of adding new line characters here
		}
		output = output.substring(0, output.length() -2);
		return output;
	}
	/*
	 more comments and thoughts: temp file for validation
	 have file local
	 file with and without header lines - removed lines are as follows:
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.w3schools.com/MetadataValidatorSchema metadata.xsd"
		works without these two, not the first line:  xmlns="http://www.example.org/MetadataValidatorSchema"
	now for making local file:
	two lines removed targetNamespace="http://www.example.org/MetadataValidatorSchema" 
xmlns:tns="http://www.example.org/MetadataValidatorSchema" 
	these two lines removed from UD_German-PUD:
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.w3schools.com/MetadataValidatorSchema metadata.xsd">
	
	//for changing, adding removing values: first parameter parse with old value, second parameter new value
	
	*/
	/**
	 * a lot of this method adapted from mkyong (https://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/)  
	 */
	public static void changeAttributeForFile(File file, String parse, String oldValue, String newValue) throws FileNotFoundException{
		// Idea of doing the changes in another class: reduce amount of imports and bulky methods here
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);

			// Get the root element (always metadata)
			Node rootNode = doc.getFirstChild();
			
			//follow parse input to get to relevant node
			//potential ambiguity (more more complicated XML files): branching paths with the same-named nodes (and which one to select)
			String[] parseList = parse.split(" "); // how are we splitting? check/confirm
			Node current = rootNode;
			for (String pathEntry : parseList) {
				if (pathEntry == "metadata") // we have already moved here when we get the rootNode
					continue;
				else {
					NodeList list = current.getChildNodes();
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						if (pathEntry.equals(node.getNodeName())) {
							System.out.println("This node along the path is: " + node.getNodeName());
							// only ambiguous case (same-named nodes) is in showAttributes. Lazy solution: if we are at an "attribute" node,
							// and the content is not oldValue, continue
							if (node.getNodeName().equals("attribute") && !node.getTextContent().equals(oldValue))
									continue;
							current = node;
							break;
						}
					}
				}
			}
			current.setTextContent(newValue);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

			System.out.println("Done");

		   } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (TransformerException tfe) {
				tfe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }
		}
	/**
	 * above (changeAttributeForFile) method, applied to a directory. Makes use of individual file.
	 */
	public static void changeAttributeForDirectory(File file, String parse, String oldValue, String newValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttributeForFile(fileEntry, parse, oldValue, newValue);
			}
		}
	}
	
	
	public static void main(String args[]) throws IOException {
		File folder = new File("src/test/resources/sample");
		/**MetadataValidation mainValidator = new MetadataValidation(folder);
		//mainValidator.isValidDirectory(folder);
		
		System.out.println(alsoTestName.isFile());
		System.out.println(testName.isFile());
		*/ //nb 4 above print command working as intended
		//File testName = new File("src/test/resources/sample/UD_English-PUD_v2.3.metadata");
		File testName = new File("src/test/resources/sample");
		File alsoTestName = new File("src/test/resources/MetadataValidatorSchema.xsd");
		//System.out.println(isWellFormed(testName)); //THIS ERROR PROBLEM. in the meantime working on 
		// System.out.println(fileToString(testName));
		changeAttributeForDirectory(testName, "metadata showAttributes attribute", "pos", "newValue");		
	}
	
}