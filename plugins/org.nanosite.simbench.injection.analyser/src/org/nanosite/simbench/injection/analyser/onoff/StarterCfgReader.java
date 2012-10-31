package org.nanosite.simbench.injection.analyser.onoff;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StarterCfgReader {

	public static void main (String argv[]) {
		StarterCfgReader instance = new StarterCfgReader();
		instance.read("D:\\Path\\to\\starter.cfg");
	}

	public OnOffModel read (String filename) {
		OnOffModel model = new OnOffModel();
		int errors = 0;
		try {
			File file = new File(filename);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			System.out.println("Root element " + doc.getDocumentElement().getNodeName());

			NodeList interfaces = doc.getElementsByTagName("Interface");
			for(int s = 0; s < interfaces.getLength(); s++) {
				Node node = interfaces.item(s);
				String name = getChild(node, "Name");
				System.out.println("Interface: #" + getChild(node, "Number") + ": "  + name);
				model.addInterface(new Interface(name));
			}

			NodeList processes = doc.getElementsByTagName("Process");
			for(int s = 0; s < processes.getLength(); s++) {
				Node node = processes.item(s);
				if (! readProcess(model, node)) {
					errors++;
				}
			}

			NodeList packages = doc.getElementsByTagName("Package");
			for(int s = 0; s < packages.getLength(); s++) {
				Node node = packages.item(s);
				System.out.println("Package: #" + getChild(node, "Number") + ": "  + getChild(node, "Name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return model;
	}

	private boolean readProcess (OnOffModel model, Node node) {
		if (node.getNodeType() != node.ELEMENT_NODE) {
			return false;
		}
		Element elem = (Element)node;

		String name = getChild(node, "Name");
		System.out.println("Process: #" + getChild(node, "Number") + ": "  + name);
		Process proc = new Process(name);
		model.addProcess(proc);

		NodeList providedIFs = elem.getElementsByTagName("ProvidesInterface");
		for(int s = 0; s < providedIFs.getLength(); s++) {
			Node n = providedIFs.item(s);
			NodeList nl = ((Element)n).getChildNodes();
			if (nl.getLength()>0) {
				int idx = Integer.parseInt(((Node) nl.item(0)).getNodeValue());
				System.out.println("  . provides: " + idx + " = " + model.getInterface(idx).getName());
			}
		}

		NodeList requiredIFs = elem.getElementsByTagName("RequiresInterface");
		for(int s = 0; s < requiredIFs.getLength(); s++) {
			Node n = requiredIFs.item(s);
			NodeList nl = ((Element)n).getChildNodes();
			if (nl.getLength()>0) {
				System.out.println("  * requires: " + ((Node) nl.item(0)).getNodeValue());
			}
		}
		return true;
	}


	private String getChild (Node node, String tag) {
		if (node.getNodeType() != node.ELEMENT_NODE) {
			return null;
		}
		return getChild((Element)node, tag);
	}

	private String getChild (Element elem, String tag) {
		NodeList subElems = elem.getElementsByTagName(tag);
		Element subElem = (Element)subElems.item(0);
		NodeList children = subElem.getChildNodes();
		return ((Node) children.item(0)).getNodeValue();
	}
}
