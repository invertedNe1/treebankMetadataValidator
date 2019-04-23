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
	/**
	 * changes first xml mode with name 'pathToNode' for specified file. Overloaded method - no details about old value of node.
	 * Makes no changes to file in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void changeAttributeForFile(File file, String pathToNode, String newValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, pathToNode, "-1"); // if no oldValue specified, call method with "-1"
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.setTextContent(newValue);
			transformUpdatedFile(file, nodeAndDoc.values().iterator().next());  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node with path \"" + pathToNode + "\" was found in the file: " + file + ".\nNo changes made to file."
					+ " Additionally specify old value of attribute.");
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("No node with path \"" + pathToNode + "\" was found in the file: " + file);
		}		
	}
	/**
	 * changes first xml node with name 'pathToNode' for each .metadata file in specified directory.
	 * Overloaded method - no details about content-to-be-removed of node.
	 * Makes no changes to file in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void changeAttributeForDirectory(File file, String pathToNode, String newValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttributeForFile(fileEntry, pathToNode, newValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"changeAttributeForFile\" method ");
	}
	/**
	 * changes first xml node with name 'pathToNode' for specified file. Overloaded method - provides details about node-to-be-removed
	 */
	public void changeAttributeForFile(File file, String pathToNode, String newValue, String oldValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, pathToNode, oldValue); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.setTextContent(newValue);
			transformUpdatedFile(file, nodeAndDoc.values().iterator().next());  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file
					+ ".\nNo changes made to file.");
		}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("No node with path \"" + pathToNode + "\" was found in the file: " + file);
		}		
	}
	/**
	 * changes first xml node with name 'pathToNode' for each .metadata file in specified directory. 
	 * Overloaded method - provides details about node-to-be-removed.
	 */
	public void changeAttributeForDirectory(File file, String pathToNode, String newValue, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					changeAttributeForFile(fileEntry, pathToNode, oldValue, newValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"changeAttributeForFile\" method ");
	}
	/**
	 * removes first xml node with name 'pathToNode' for specified file. Overloaded method - no details about node-to-be-removed.
	 * Does not work in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void removeAttributeForFile(File file, String pathToNode) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, pathToNode, "-1"); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.getParentNode().removeChild(node);
			transformUpdatedFile(file, nodeAndDoc.values().iterator().next());  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node with path \"" + pathToNode + "\" was found in the file: " + file + ".\nNo changes made to file."
					+ " Additionally specify old value of attribute.");
			}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("no node with path \"" + pathToNode + "\" was found in the file: " + file);
		}
	}
	/**
	 * removes first xml node with name 'pathToNode' for each .metadata file in specified directory.
	 * Overloaded method - no details about node-to-be-removed.
	 * Does not work in ambiguous case, where multiple nodes have same 'pathToNode' name.
	 */
	public void removeAttributeForDirectory(File file, String pathToNode) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttributeForFile(fileEntry, pathToNode);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"removeAttributeForFile\" method ");
	}
	/**
	 * removes first xml node with name 'pathToNode' for specified file. Overloaded method - provides details about node-to-be-removed.
	 */
	public void removeAttributeForFile(File file, String pathToNode, String oldValue) throws FileNotFoundException {
		nodeAndDoc.clear();
		locateNodeToChange(file, pathToNode, oldValue); // refills HashMap 'nodeAndDoc'
		if (nodeAndDoc.size() == 1) {
			Node node = nodeAndDoc.keySet().iterator().next();
			node.getParentNode().removeChild(node);
			transformUpdatedFile(file, nodeAndDoc.values().iterator().next());  // write the content into xml file
		}
		else if (nodeAndDoc.size() > 1) {
			System.out.println("More than one node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file
					+ ".\nNo changes made to file.");
			}
		else if (nodeAndDoc.size() == 0) {
			System.out.println("no node with path \"" + pathToNode + "\" and oldValue \"" + oldValue + "\" was found in the file: " + file);
		}
	}
	/**
	 * removes first xml node with name 'pathToNode' for each .metadata file in specified directory.
	 * Overloaded method - provides details about node-to-be-removed.
	 */
	public void removeAttributeForDirectory(File file, String pathToNode, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					removeAttributeForFile(fileEntry, pathToNode, oldValue);
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
	/**
	 * adds new attribute, as child of 'metadata' node, for each file in specified directory
	 * Attribute has name "attributeName" and content "attributeContent"
	 */
	public void addAttributeForDirectory(File file, String pathToNode, String oldValue) throws FileNotFoundException {
		if (file.isDirectory()) {
			for (File fileEntry: file.listFiles()) {
				if (fileEntry.getName().endsWith(".metadata"))
					addAttributeForFile(fileEntry, pathToNode, oldValue);
			}
		}
		else 
			System.out.println("No directory selected. For modification of a single file, use \"addAttributeForFile\" method ");
	}
		
	/**
	 * called by primary methods. locates node to change / remove.
	 * either @return node to be modified OR store node and doc in class HashMap
	 */
	static NodeData locateNodeToChange(File file, String pathToNode, String oldValue) {
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
									//rare case / problem: when multiple nodes have the same oldValue, no change will be made 
									//solution: don't change count (remains 0). Last node changed.									
									nodeAndDoc.put(node, doc); //either HashMap or class NodeData
									nodeData = new NodeData(node, doc, count);
								}
							}
							else { // add each node with name path
								count += 1;
								nodeAndDoc.put(node, doc); // either HashMap or other class
								nodeData = new NodeData(node, doc, count);
								//current = node; // when pathToNode is longer than one element.
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
		// return;
		return nodeData;
	}
	/**
	 * called by primary methods. Rewrites XML file with implemented changes.
	 */
	static void transformUpdatedFile(File file, Document doc) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//DOMSource source = new DOMSource(nodeAndDoc.values().iterator().next()); //which is the Document doc
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
	
			System.out.println("File changed : " + file);

	   } catch (TransformerException tfe) {
			tfe.printStackTrace();
	   }
	}
	
}	
