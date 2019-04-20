import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class MetadataModification {
	
	private static HashMap<Node, Document> nodeAndDoc = new HashMap<>();
	
	/**
	 *  Turns xml file into string for parsing or printing.
	 * @return String version of the xml file.
	 */
	public String fileToString(File file) throws FileNotFoundException, IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String output = "", line = "";
		while ((line = reader.readLine()) != null) {
			output += line + "\n";
		}
		output = output.substring(0, output.length() -1);
		reader.close();
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
	*/
	/**
	 * changes first xml mode with name 'parse' for specified file. Overloaded method - no details about content-to-be-removed of node.
	 * Makes no changes to file in ambiguous case - multiple nodes with same 'parse' name.
	 */
	public void changeAttributeForFile(File file, String parse, String newValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, parse, "-1"); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.setTextContent(newValue);
			transformUpdatedFile(file);  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node of name \"" + parse + "\" was found in the file: " + file + ".\nNo changes made to file."
					+ " Additionally specify old value of attribute.");
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("No node of name \"" + parse + "\" was found in the file: " + file);
		}		
	}
	/**
	 * changes first xml node with name 'parse' for each .metadata file in specified directory.
	 * Overloaded method - no details about content-to-be-removed of node.
	 * Makes no changes to file in ambiguous case - multiple nodes with same 'parse' name.
	 */
	public void changeAttributeForDirectory(File file, String parse, String newValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttributeForFile(fileEntry, parse, newValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"changeAttributeForFile\" method ");
	}
	/**
	 * changes first xml node with name 'parse' for specified file. Overloaded method - provides details about node-to-be-removed
	 */
	public void changeAttributeForFile(File file, String parse, String oldValue, String newValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, parse, oldValue); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.setTextContent(newValue);
			transformUpdatedFile(file);  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node of name \"" + parse + "\" was found in the file: " + file + ".\nNo changes made to file."
					+ " Additionally specify old value of attribute."); // figure out logic - don't need last sentence in overloaded method.
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("No node of name \"" + parse + "\" was found in the file: " + file);
		}		
	}
	/**
	 * changes first xml node with name 'parse' for each .metadata file in specified directory. 
	 * Overloaded method - provides details about node-to-be-removed.
	 */
	public void changeAttributeForDirectory(File file, String parse, String oldValue, String newValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttributeForFile(fileEntry, parse, oldValue, newValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"changeAttributeForFile\" method ");
	}
	/**
	 * removes first xml node with name 'parse' for specified file. Overloaded method - no details about node-to-be-removed.
	 * Does not work in ambiguous case - multiple nodes with 'parse' name.
	 */
	public void removeAttributeForFile(File file, String parse) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, parse, "-1"); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.getParentNode().removeChild(node);
			transformUpdatedFile(file);  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("more than one node of name \"" + parse + "\" was found in the file: " + file);
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("no node of name \"" + parse + "\" was found in the file: " + file);
		}
	}
	/**
	 * removes first xml node with name 'parse' for each .metadata file in specified directory.
	 * Overloaded method - no details about node-to-be-removed.
	 * Does not work in ambiguous case - multiple nodes with 'parse' name.
	 */
	public void removeAttributeForDirectory(File file, String parse) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttributeForFile(fileEntry, parse);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"removeAttributeForFile\" method ");
	}
	/**
	 * removes first xml node with name 'parse' for specified file. Overloaded method - provides details about node-to-be-removed.
	 */
	public void removeAttributeForFile(File file, String parse, String oldValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, parse, oldValue); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.getParentNode().removeChild(node);
			transformUpdatedFile(file);  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("more than one node of name \"" + parse + "\" was found in the file: " + file);
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("no node of name \"" + parse + "\" was found in the file: " + file);
		}
	}
	/**
	 * removes first xml node with name 'parse' for each .metadata file in specified directory.
	 * Overloaded method - provides details about node-to-be-removed.
	 */
	public void removeAttributeForDirectory(File file, String parse, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttributeForFile(fileEntry, parse, oldValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"removeAttributeForFile\" method ");
	}
	/**
	 * adds new attribute, as child of 'metadata' node. 
	 * Attribute has name "attributeName" and content "attributeContent"
	 */
	public void addAttributeForFile(File file, String attributeName, String attributeContent) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			Element element = doc.createElement(attributeName);
			element.appendChild(doc.createTextNode(attributeContent));
			doc.getFirstChild().appendChild(element);
			//doc.renameNode(n, null, attributeName);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
	
			System.out.println("Done");

	   } catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
	/**
	 * adds new attribute, as child of 'metadata' node, for each file in specified directory
	 * Attribute has name "attributeName" and content "attributeContent"
	 */
	public void addAttributeForDirectory(File file, String parse, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					addAttributeForFile(fileEntry, parse, oldValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"addAttributeForFile\" method ");
	}
		
	/**
	 * called by primary methods. locates node to change / remove.
	 * @return node to be modified.
	 */
	static void locateNodeToChange(File file, String parse, String oldValue) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			Node rootNode = doc.getFirstChild(); // Get the root element (always metadata)
			
			//follow parse input to get to relevant node
			//potential ambiguity (more complicated XML files): branching paths with the same-named nodes (and which one to select)
			String[] parseList = parse.split(" "); // how are we splitting? check/confirm
			Node current = rootNode;
			for (String pathEntry : parseList) {
				if (pathEntry == "metadata") // we have already moved here when we get the rootNode
					continue;
				else {
					NodeList list = current.getChildNodes(); // all nodes are children of 'metadata' node
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						if (pathEntry.equals(node.getNodeName())) {
							System.out.println("This node along the path is: " + node.getNodeName());
							if (!oldValue.equals("-1")) {
								if (!node.getTextContent().equals(oldValue)) {
									System.out.println("old value: " + oldValue + " did not match text of node - " + node.getTextContent());
								}
								// only ambiguous case (same-named nodes) is in showAttributes. Have default system, otherwise inform user of ambiguity?
								// Lazy solution: if we are at an "attribute" node, and the content is not oldValue, continue
								if (node.getNodeName().equals("attribute") && !node.getTextContent().equals(oldValue))
										continue;
							}
							nodeAndDoc.put(node, doc);
							//current = node;
							//break;
						}
					}
					return;
				}
			}
		   } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }
	}
	/**
	 * called by primary methods. Rewrites XML file with implemented changes.
	 */
	static void transformUpdatedFile(File file) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(nodeAndDoc.values().iterator().next()); //which is the Document doc
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
	
			System.out.println("Done");

	   } catch (TransformerException tfe) {
			tfe.printStackTrace();
	   }
	}
	
}	
