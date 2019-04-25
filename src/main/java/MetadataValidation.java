import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.xml.sax.SAXException;
import java.util.ArrayList;

import org.w3c.dom.Element;

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
		// make changes to metadata node - originally had following 3 attributes, seems to work with just the first.
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
		// eclipse compile to JAR file
		MetadataModification modifier = new MetadataModification();
		// command line options
		if (args.length == 0 || args.length == 1 && (args[0].equals("--help") || args[0].equals("help"))) { 
				System.out.println("helpful message - details on available methods");
		}
		else if (args[0].equals("options") || args[0].equals("--options")) {
			System.out.println("A list of the available commands of this program. For more details on these methods, use --help or help");
			System.out.println("print\nvalidate\nchange\nremove\nadd");
		}
		// different cases for print command
		else if (args[0].equals("print")) {
			if (args.length == 1)
				System.out.println("this is what print does");
			else if (args.length > 2)
				System.out.println("too many arguments");
			else {
				File file = new File(args[1]);
				System.out.println(modifier.fileToString(file));
			}
		}
		// different cases for validate command
		else if (args[0].equals("validate")) {
			if (args.length == 1)
				System.out.println("this is what validate does");
			if (args.length > 2)
				System.out.println("too many arguments");
			else {
				File file = new File(args[1]);
				MetadataValidation validator = new MetadataValidation(file);
			}
		}
		// different cases for change command
		else if (args[0].equals("change")) {
			if (args.length == 1)
				System.out.println("this is what change does..");
			else if (args.length == 4) {
				File file = new File(args[1]);
				if (file.isFile())
					modifier.changeAttributeForFile(file, args[2], args[3]);
				else if (file.isDirectory())
					modifier.changeAttributeForDirectory(file, args[2], args[3]);
			}
			else if (args.length == 5) {
				// fleshing out all args (in at least one case) to see what is going on
				File file = new File(args[1]);
				String pathToNode = args[2];
				String newValue = args[3];
				String oldValue = args[4];
				if (file.isFile())
					modifier.changeAttributeForFile(file, args[2], args[3], args[4]);
				else if (file.isDirectory())
					modifier.changeAttributeForDirectory(file, args[2], args[3], args[4]);
			}
			else if (args.length == 2 || args.length == 3)
				System.out.println("Not enough arguments\nthis is what change does");
			else if (args.length > 5)
				System.out.println("too many arguments\nthis is what change does..");	
			else // for any other case which I haven't thought of. Is this necessary
				System.out.println("you did something (else) wrong");
		}
		// different cases for remove command
		else if (args[0].equals("remove")) {
			if (args.length == 1)
				System.out.println("this is what remove does..");
			else if (args.length == 2)
				System.out.println("Not enough arguments\nThis is what remove does");
			else if (args.length == 3) {
				File file = new File(args[1]);
				if (file.isFile())
					modifier.removeAttributeForFile(file, args[2]);
				else if (file.isDirectory())
					modifier.removeAttributeForDirectory(file, args[2]);
			}
			else if (args.length == 4) {
				File file = new File(args[1]);
				if (file.isFile())
					modifier.removeAttributeForFile(file, args[2], args[3]);
				else if (file.isDirectory())
					modifier.removeAttributeForDirectory(file, args[2], args[3]);
			}
			else if (args.length > 4)
				System.out.println("too many arguments\nthis is what remove does..");
		}
		// different cases for add command
		else if (args[0].equals("add")) {
			if (args.length == 1)
				System.out.println("this is what add does..");
			else if (args.length == 2 || args.length == 3)
				System.out.println("Not enough arguments\nThis is what add does");
			else if (args.length == 4) {
				// add to root node ("metadata")
				File file = new File(args[1]);
				if (file.isFile())
					modifier.addAttributeForFile(file, args[2], args[3]);
				else if (file.isDirectory())
					modifier.addAttributeForDirectory(file, args[2], args[3]);
			}
			else if (args.length == 5) {
				// add at specific location
				File file = new File(args[1]);
				if (file.isFile())
					modifier.addAttributeAtLocationForFile(file, args[2], args[3], args[4]);
				else if (file.isDirectory())
					modifier.addAttributeAtLocationForDirectory(file, args[2], args[3], args[4]);
			}
			else if (args.length > 5)
				System.out.println("too many arguments\nthis is what add does..");
		}
		// first command is not one of the above cases
		else {
			System.out.println("Initial command could not be recognised. Use one of the following commands: print, validate, change, remove, add."
					+ "\nTo list all options use --options" + "\nFor more details on these use --help");
		}
		// self-testing program.
		//File testFolder = new File("src/test/resources/sample");
		File testFile = new File("/Users/sam/temprep/treebankMetadataValidator/src/test/resources/sample/aUD_German-PUD_v2.3.metadata");
		//System.out.println(modifier.fileToString(testFile));
		//File testSchemaFile = new File("src/test/resources/sample/MetadataValidatorSchema.xsd");

		//MetadataValidation validator = new MetadataValidation(testFile);
		
		modifier.changeAttributeForFile(testFile, "showAttributes", "stillGerman");		
		//modifier.changeAttributeForDirectory(testFolder, "abbreviations/abbreviation/attribute", "stillLemma");
		//modifier.changeAttributeForFile(testFile, "metadata/showAttributes/attribute", "lemma", "newVal");
		//modifier.removeAttributeForFile(testFile, "showAttributes");
		//modifier.addAttributeForFile(testFile, "newName", "newContent");
		//modifier.addAttributeAtLocationForFile(testFile, "showAttributes", "fancyName", "niceContent");
	}
}
