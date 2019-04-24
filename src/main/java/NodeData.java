import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NodeData {
	public Node node;
	public Document document;
	public int count;
	
	public NodeData(Node node, Document document, int count) {
		this.node = node;
		this.document = document;
		this.count = count;
	}
}
