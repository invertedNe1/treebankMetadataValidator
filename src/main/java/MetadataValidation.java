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

/**
 * Modifies existing metadata.
 * Methods to show metadata file content, and to change, add and remove attributes.
 * Methods for changing and removing are overloaded - either with or without the old content of the attribute.
 * If multiple attributes with the same name are provided, console will prompt to provide old content (and thus specify
 * single attribute to change / remove. 
 *
 */
public class MetadataValidation {
	
	public ArrayList<File> fileEntries = new ArrayList<File>(); // potential place to store files in a folder.
	
	/**
	 * Performs metadata validation for a single file, or for all files in a directory.
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
	 * Checks if XML in specific metadata file (parameter 'file') is well formed.
	 * Add attributes to metadata node of file before validating. Removes afterwards.
	 */
	private boolean isWellFormed(File file) throws IOException {
		// make changes to metadata node
		//xmlns="http://www.example.org/MetadataValidatorSchema" 
		//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		//xsi:schemaLocation="http://www.w3schools.com/MetadataValidatorSchema metadata.xsd"
		NodeData changeNodeData = MetadataModification.locateNodeToChange(file, "metadata", "-1");
		((Element)changeNodeData.node).setAttribute("xmlns", "http://www.example.org/MetadataValidatorSchema");	
		MetadataModification.transformUpdatedFile(file, changeNodeData.document);
		
		Source xmlFile = new StreamSource(file);
		File schemaFile = new File("src/test/resources/sample/MetadataValidatorSchema.xsd");
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);		
		try {
		  Schema schema = schemaFactory.newSchema(schemaFile);
		  Validator validator = schema.newValidator();
		  validator.validate(xmlFile);
		  System.out.println(xmlFile.getSystemId() + " is valid");
		  
		  // remove changes to metadata node
		  NodeData removeNodeData = MetadataModification.locateNodeToChange(file, "metadata", "-1");
		  ((Element)removeNodeData.node).removeAttribute("xmlns");
		  MetadataModification.transformUpdatedFile(file, removeNodeData.document);
		  return true;
		} catch (SAXException e) {
		  System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
		} catch (IOException e) {}
		
		// remove changes to metadata node - other case
		NodeData removeNodeData = MetadataModification.locateNodeToChange(file, "metadata", "-1");
		((Element)changeNodeData.node).removeAttribute("xmlns");
		MetadataModification.transformUpdatedFile(file, removeNodeData.document);
		return false;
	}
	
	public static void main(String args[]) throws IOException {
		// check length of args and do tool based on that.
		// eclipse compile to JAR file
		if (args.length == 1 && args[0].equals("--help")) { //check first arg isn't name of program
				System.out.println("helpful message");
				return;
		}
		File testFolder = new File("src/test/resources/sample");
		File testFile = new File("src/test/resources/sample/aUD_German-PUD_v2.3.metadata");
		File testSchemaFile = new File("src/test/resources/sample/MetadataValidatorSchema.xsd");
		MetadataModification modifier = new MetadataModification();

		//MetadataValidation validator = new MetadataValidation(testFile);
		
		//modifier.changeAttributeForFile(testFile, "language", "stillGerman");
		//modifier.changeAttributeForDirectory(testFolder, "abbreviations/abbreviation/attribute", "stillLemma");
		//modifier.changeAttributeForFile(testFile, "metadata/showAttributes/attribute", "lemma", "newVal");
		modifier.removeAttributeForFile(testFile, "showAttributes/attribute", "Lemma");
		//modifier.addAttributeForDirectory(testFolder, "newName", "newContent");
	}
}
