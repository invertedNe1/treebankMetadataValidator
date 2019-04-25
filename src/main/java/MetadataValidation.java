import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

/**
 * Checks validity of metadata files. 
 * Command line interactivity in main method, includes methods from MetadataModification 
 * 		- changes, removes and adds nodes to a file
 * 		- prints file contents 
 */
public class MetadataValidation {
	
	/**
	 * Performs metadata validation for a single file, or for all files in a directory.
	 */
	public MetadataValidation(File file) throws FileNotFoundException, IOException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata")) {
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
	 * Add attributes to metadata node of file before validating. Removes afterwards.
	 * 		Uses a DocumentBuilder and a Transformer from MetadataModification to achieve this.
	 */
	private boolean isWellFormed(File file) throws IOException {
		/**
	 	make changes to metadata node - originally had following 3 attributes, seems to work with just the first.
		xmlns="http://www.example.org/MetadataValidatorSchema" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xsi:schemaLocation="http://www.w3schools.com/MetadataValidatorSchema metadata.xsd"
		*/
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
		MetadataModification modifier = new MetadataModification();
		// command line options
		if (args.length == 0 || args.length == 1 && (args[0].equals("--help") || args[0].equals("help"))) { 
				System.out.println("--help OR help: in-detail descriptions of all available commands" 
						+ "\n--options OR options: lists all available commands"
						+ "\nprint: prints the contents of the file\n\targument 1 (required): file path"
						+ "\nvalidate: Validates an XML file with the custom xsd file MetadataValidatorSchema"
						+ "\n\targument 1 (required): file path"
						+ "\nchange: changes the value for a node in a metadata file. Can be called on a single file"
						+ " or a directory (all subfiles). Notifies when trying to change content of a node which "
						+ "contains child nodes\n\targument 1 (required): file path\n\targument 2 (required): path to "
						+ "node to change (split by / symbol)\n\targument 3 (required): new value for text of node"
						+ "\n\targument 4 (optional): old value for text of node - used to clarify when multiple nodes"
						+ " have the same name and different contents"
						+ "\nremove: removes a node in a metadata file. Can be called on a single file or a directory (all"
						+ "subfiles). Notifies when trying to remove a node with child nodes\n\targument 1 (required):"
						+ " file path\n\targument 2 (required): path to node to change (split by / symbol)"
						+ "\n\targument 3 (optional): old value for text of node - used to clarify when multiple nodes"
						+ "have the same name and different contents"
						+ "\nadd: adds a node to a metadata file. As a base case, will be added after the last child of "
						+ "the metadata node. Additionally can specify path to another node, to add it as a child of that"
						+ "node. Can be called on a single file or a directory (all subfiles)\n\targument 1 (required):"
						+ " file path\n\targument 2 (required): name of new node\n\targument 3 (required): content of new node"
						+ "\n\targument 4 (optional): path to node (split by / symbol). Will be added as child of this"
						+ " node instead of metadata root node");
		}
		else if (args[0].equals("options") || args[0].equals("--options")) {
			System.out.println("A list of the available commands of this program. For more details on these methods, use --help or help");
			System.out.println("print\nvalidate\nchange\nremove\nadd");
		}
		// different cases for print command
		else if (args[0].equals("print")) {
			if (args.length == 1)
				System.out.println("print: prints the contents of the file\\n\\targument 1 (required): file path");
			else if (args.length > 2)
				System.out.println("too many arguments");
			else {
				File file = new File(args[1]);
				if (file.isDirectory())
					System.out.println("Cannot print contents of a directory. Please select single file.");
				else
					System.out.println(modifier.fileToString(file));
			}
		}
		// different cases for validate command
		else if (args[0].equals("validate")) {
			if (args.length == 1)
				System.out.println("validate: Validates an XML file with the custom xsd fileMetadataValidatorSchema"
						+ "\\n\\targument 1 (required): file path");
			if (args.length > 2)
				System.out.println("too many arguments");
			else {
				File file = new File(args[1]);
				MetadataValidation validator = new MetadataValidation(file);
			}
		}
		// different cases for change command
		else if (args[0].equals("change")) {
			String changeMessage = "change: changes the value for a node in a metadata file. Can be called on a single file or a "
					+ "directory (all subfiles). Notifies when trying to change content of a node which contains child nodes"
					+ "\\n\\targument 1 (required): file path\\n\\targument 2 (required): path to node to change (split by"
					+ " / symbol)\\n\\targument 3 (required): new value for text of node\\n\\targument 4 (optional): "
					+ "old value for text of node - used to clarify when multiple nodes have the same name and different contents";
			if (args.length == 1)
				System.out.println(changeMessage);
			else if (args.length == 4) {
				File file = new File(args[1]);
				modifier.changeAttribute(file, args[2], args[3]);
			}
			else if (args.length == 5) {
				// fleshing out all args (in at least one case) so someone could see what is going on
				File file = new File(args[1]);
				String pathToNode = args[2];
				String newValue = args[3];
				String oldValue = args[4];
				modifier.changeAttribute(file, args[2], args[3], args[4]);
			}
			else if (args.length == 2 || args.length == 3)
				System.out.println("Not enough arguments\n" + changeMessage);
			else if (args.length > 5)
				System.out.println("Too many arguments\n" + changeMessage);
		}
		// different cases for remove command
		else if (args[0].equals("remove")) {
			String removeMessage = "remove: removes a node in a metadata file. Can be called on a single file or a directory (all"
					+ "subfiles). Notifies when trying to remove a node with child nodes\n\targument 1 (required):"
					+ " file path\n\targument 2 (required): path to node to change (split by / symbol)"
					+ "\n\targument 3 (optional): old value for text of node - used to clarify when multiple nodes"
					+ "have the same name and different contents";
			if (args.length == 1)
				System.out.println(removeMessage);
			else if (args.length == 2)
				System.out.println("Not enough arguments\n" + removeMessage);
			else if (args.length == 3) {
				File file = new File(args[1]);
				modifier.removeAttribute(file, args[2]);
			}
			else if (args.length == 4) {
				File file = new File(args[1]);
				modifier.removeAttribute(file, args[2], args[3]);
			}
			else if (args.length > 4)
				System.out.println("too many arguments\n" + removeMessage);
		}
		// different cases for add command
		else if (args[0].equals("add")) {
			String addMessage = "add: adds a node to a metadata file. As a base case, will be added after the last child of "
					+ "the metadata node. Additionally can specify path to another node, to add it as a child of that"
					+ "node. Can be called on a single file or a directory (all subfiles)\n\targument 1 (required):"
					+ " file path\n\targument 2 (required): name of new node\n\targument 3 (required): content of new node"
					+ "\n\targument 4 (optional): path to node (split by / symbol). Will be added as child of this node "
					+ "instead of metadata root node";
			if (args.length == 1)
				System.out.println(addMessage);
			else if (args.length == 2 || args.length == 3)
				System.out.println("Not enough arguments\n" + addMessage);
			else if (args.length == 4) {
				// add to root node ("metadata")
				File file = new File(args[1]);
				modifier.addAttribute(file, args[2], args[3]);
			}
			else if (args.length == 5) {
				// add at specific location
				File file = new File(args[1]);
				modifier.addAttributeAtLocation(file, args[2], args[3], args[4]);
			}
			else if (args.length > 5)
				System.out.println("too many arguments\n" + addMessage);
		}
		// first command is not one of the above cases
		else {
			System.out.println("Initial command could not be recognised. Use one of the following commands: print, validate, change, remove, add."
					+ "\nTo list all options use --options" + "\nFor more details on these use --help");
		}
	}
}
