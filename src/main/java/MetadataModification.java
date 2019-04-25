import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

/**
 * Contains methods to change, remove and add nodes to a metadata file
 * These are all overloaded. change and remove either have or don't have parameter 'oldValue',
 * which is used to distinguish when multiple nodes have the same name. Add has/doesn't have parameter
 * 'pathToNode - either adds new node at specific location or at end of file.
 * Also contains method to return content of a file as a String.
 * 
 */
public class MetadataModification {	
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
	/**
	 * changes first xml mode with name 'pathToNode' for specified file. Overloaded method - no details about old value of node.
	 * Makes no changes to file in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void changeAttribute(File file, String pathToNode, String newValue) throws FileNotFoundException {
		if (file.isDirectory()) { // call method for individual files in directory
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttribute(fileEntry, pathToNode, newValue);
			}
		}
		else {
			NodeData nodeData = locateNodeToChange(file, pathToNode, "-1"); // if no oldValue specified, call method with "-1"
			if (nodeData.count == 1) {
				Node node = nodeData.node;
				// check if node has any child elements
				boolean hasChildNodes = false;
				NodeList childNodeList = node.getChildNodes();
				for (int i = 0; i < childNodeList.getLength(); i++) {
					Node childNode = childNodeList.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE)
						hasChildNodes = true;
				}
				if (hasChildNodes) {
					System.out.println("The node with path \"" + pathToNode + "\" has multiple child nodes. No changes made. "
							+ "Additionally specify which child node to change.");
					return;
				}
				node.setTextContent(newValue);
				transformUpdatedFile(file, nodeData.document);  // write the content into xml file
			}
			else if (nodeData.count > 1) {
				System.out.println("More than one node with path \"" + pathToNode + "\" was found in the file: " + file + ".\nNo changes made to file."
						+ " Additionally specify old value of attribute.");
			}
			else if (nodeData.count == 0) {
				System.out.println("No node with path \"" + pathToNode + "\" was found in the file: " + file);
			}
		}
	}
	/**
	 * changes first xml node with name 'pathToNode' for specified file. Overloaded method - provides details about node-to-be-removed
	 */
	public void changeAttribute(File file, String pathToNode, String newValue, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttribute(fileEntry, pathToNode, oldValue, newValue);
			}
		}
		else {
			NodeData nodeData = locateNodeToChange(file, pathToNode, oldValue); // refills HashMap 'nodeAndDoc'
			if (nodeData.count == 1) {
				Node node = nodeData.node;
				// check if node has any child elements
				boolean hasChildNodes = false;
				NodeList childNodeList = node.getChildNodes();
				for (int i = 0; i < childNodeList.getLength(); i++) {
					Node childNode = childNodeList.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE)
						hasChildNodes = true;
				}
				if (hasChildNodes) {
					System.out.println("The node with path \"" + pathToNode + "\" has multiple child nodes. No changes made. "
							+ "Additionally specify which child node to change.");
					return;
				}
				node.setTextContent(newValue);
				transformUpdatedFile(file, nodeData.document);  // write the content into xml file
			}
			else if (nodeData.count > 1) {
				System.out.println("More than one node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file
						+ ".\nNo changes made to file.");
			}
			else if (nodeData.count == 0) {
				System.out.println("No node with path \"" + pathToNode + "\" was found in the file: " + file);
			}
		}
	}
	/**
	 * removes first xml node with name 'pathToNode' for specified file. Overloaded method - no details about node-to-be-removed.
	 * Does not work in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void removeAttribute(File file, String pathToNode) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttribute(fileEntry, pathToNode);
			}
		}
		else{
			NodeData nodeData = locateNodeToChange(file, pathToNode, "-1"); // refills HashMap 'nodeAndDoc'
			if (nodeData.count == 1) {
				Node node = nodeData.node;
				// check if node has any child elements
				boolean hasChildNodes = false;
				NodeList childNodeList = node.getChildNodes();
				for (int i = 0; i < childNodeList.getLength(); i++) {
					Node childNode = childNodeList.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE)
						hasChildNodes = true;
				}
				if (hasChildNodes) {
					System.out.println("The node with path \"" + pathToNode + "\" had multiple child nodes. All were successfully removed");
				}
				node.getParentNode().removeChild(node);
				transformUpdatedFile(file, nodeData.document);  // write the content into xml file
			}
			else if (nodeData.count > 1) {
				System.out.println("More than one node with path \"" + pathToNode + "\" was found in the file: " + file + ".\nNo changes made to file."
						+ " Additionally specify old value of attribute.");
				}
			else if (nodeData.count == 0) {
				System.out.println("no node with path \"" + pathToNode + "\" was found in the file: " + file);
			}
		}
	}
	/**
	 * removes first xml node with name 'pathToNode' for specified file. Overloaded method - provides details about node-to-be-removed.
	 */
	public void removeAttribute(File file, String pathToNode, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttribute(fileEntry, pathToNode, oldValue);
			}
		}
		else {
			NodeData nodeData = locateNodeToChange(file, pathToNode, oldValue); // refills HashMap 'nodeAndDoc'
			if (nodeData.count == 1) {
				Node node = nodeData.node;
				node.getParentNode().removeChild(node);
				transformUpdatedFile(file, nodeData.document);  // write the content into xml file
			}
			else if (nodeData.count > 1) {
				System.out.println("More than one node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file
						+ ".\nNo changes made to file.");
				}
			else if (nodeData.count == 0) {
				System.out.println("no node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file);
			}
		}
	}
	/**
	 * adds new attribute, as child of 'metadata' node. 
	 * Attribute has name "attributeName" and content "attributeContent"
	 * Overloaded method - base case, no specific path provided. Attribute added after last child node of metadata root node.
	 * Warning: xsd file won't be changed when adding attribute. File that was otherwise well-formed 
	 * according to MetadataValidation will no longer be so.
	 */
	public void addAttribute(File file, String attributeName, String attributeContent) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					addAttribute(fileEntry, attributeName, attributeContent);
			}
		}
		else {
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
		
				System.out.println("File changed : " + file);
	
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
	}
	/**
	 * adds new attribute as a child node of the node 'pathToNode'.
	 * Attribute has name 'attributeName' and content 'attributeContent'
	 * Overloaded method - contains path to where attribute should be added.
	 * Warning: xsd file won't be changed when adding attribute. File that was otherwise well-formed 
	 * according to MetadataValidation will no longer be so.
	 */
	public void addAttributeAtLocation(File file, String attributeName, String attributeContent, String pathToNode) throws FileNotFoundException{
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					addAttributeAtLocation(file, pathToNode, attributeName, attributeContent);
			}
		}
		else {
			NodeData addNode = locateNodeToChange(file, pathToNode, "-1");
			Element element = addNode.document.createElement(attributeName);
			element.appendChild(addNode.document.createTextNode(attributeContent));
			addNode.node.appendChild(((Node)element));
			transformUpdatedFile(file, addNode.document);	
		}
	}
	/**
	 * called by primary methods. locates node to change / remove.
	 * if no relevant oldValue is used, method will be called with "-1" for this parameter
	 * either @return node to be modified OR store node and doc in class HashMap
	 */
	static NodeData locateNodeToChange(File file, String pathToNode, String oldValue) throws FileNotFoundException {
		NodeData nodeData = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			
			Node rootNode = doc.getFirstChild(); // Get the root element (always metadata)
			
			//follow pathToNode input to get to relevant node
			//potential ambiguity not covered (more complicated XML files): branching paths with the same-named nodes - here just picks first one.
			String[] path = pathToNode.split("/");
			Node current = rootNode;
			for (String pathEntry : path) {
				// if not the last part of path (and not "metadata" - we already move here when we get the rootNode), move along the path
				if (!pathEntry.equals(path[path.length - 1]) && !pathEntry.equals("metadata")) {
					NodeList nodeList = current.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						if (pathEntry.equals(node.getNodeName())) {
							current = node;
							break;
						}
					}
				}
				else {
					int count = 0; // counts number of nodes with the name pathToNode
					boolean oldValueUsed = !oldValue.equals("-1");
					NodeList nodeList = current.getChildNodes();
					if (pathEntry.equals("metadata")) {
						nodeData = new NodeData(current, doc, 0);
					}
					for (int i = 0; i < nodeList.getLength(); i++) { // Check all children of 'metadata' node for matches to pathToNode name
						Node node = nodeList.item(i);
						if (pathEntry.equals(node.getNodeName())) {
							if (oldValueUsed) { // only add if oldValue and textContent match
								/**
								 * Print message if oldValue is used and does not match attribute text.
								 * Theoretically not so necessary, as, with multiple nodes of same name,
								 * there should always be a case where oldValue and text of node do not match.
								if (!node.getTextContent().equals(oldValue)) {
									System.out.println("old value: " + oldValue + " did not match text of node - " + node.getTextContent());
								}
								*/
								if (node.getTextContent().equals(oldValue)) {
									nodeData = new NodeData(node, doc, count);
								}
							}
							else { // add each node with name path
								count += 1;
								nodeData = new NodeData(node, doc, count);
							}
						}
					}
				}
			}
		   } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }
		return nodeData;
	}
	/**
	 * called by primary methods. Rewrites XML file with implemented changes.
	 */
	static void transformUpdatedFile(File file, Document doc) throws FileNotFoundException {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
	
			System.out.println("File changed : " + file);

	   } catch (TransformerException tfe) {
			tfe.printStackTrace();
	   }
	}
}	
