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
	 * Checks if XML in specific metadata file is well formed. 
	 */
	private boolean isWellFormed(File file) throws IOException {
		File schemaFile = new File("src/test/resources/sample/MetadataValidatorSchema.xsd");
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
	
	public static void main(String args[]) throws IOException {
		// check length of args and do tool based on that.
		// eclipse compile to JAR file
		if (args.length == 1 && args[0].equals("--help")) {//check first arg isn't name of program
				System.out.println("helpful message");
				return;
		}
		File testFolder = new File("src/test/resources/sample");
		File testFile = new File("src/test/resources/sample/UD_German-PUD_v2.3.metadata");
		File testSchemaFile = new File("src/test/resources/sample/MetadataValidatorSchema.xsd");
		//again, testing what java is reading as content of file. After checking (+ changing string -2 -> -1), we now know that they contain the expected!
		MetadataModification modifier = new MetadataModification();
		modifier.fileToString(testSchemaFile);
		modifier.fileToString(testFile);
		assert testSchemaFile.isFile();
		assert testFolder.isFile();
		assert testFile.isFile();

		MetadataValidation validator = new MetadataValidation(testFile); // not working, fails at line 71. 
		
		//modifier.changeAttributeForFile(testFile, "language", "testLanguageVal");
		//modifier.changeAttributeForDirectory(testFile, "metadata showAttributes attribute", "pos", "newValue");
		//modifier.removeAttributeForFile(testFile, "longname");´
		//modifier.addAttributeForDirectory(testFolder, "newName", "newContent");
	}
}
