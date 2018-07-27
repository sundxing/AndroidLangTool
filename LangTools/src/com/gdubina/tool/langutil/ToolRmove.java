package com.gdubina.tool.langutil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ToolRmove {

	private static final String DIR_VALUES = "values";
	private static boolean ignoreComment;

	private DocumentBuilder builder;
	private List<String> filterList = new ArrayList<>();
	private String project;
	private PrintStream out;
	private String inputFileName;

	public ToolRmove(PrintStream out) throws ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();
		this.out = out == null ? System.out : out;
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		if(args == null || args.length == 0){
			System.out.println("Project folder doesn't exists");
			return;
		}
		run(null, args[0], null, args.length > 1 ? args[1] : null, null);
	}
	
	public static void run(String projectDir, String outputFile) throws SAXException, IOException, ParserConfigurationException {
		run(null, projectDir, null, outputFile, null);
	}

	public static void run(String projectDir, String outputFile, String inFileName) throws SAXException, IOException, ParserConfigurationException {
		run(null, projectDir, null, outputFile, inFileName);
	}
	
	public static void run(PrintStream out, String projectDir, String listFilter, String outputFile, String inFileName) throws SAXException, IOException, ParserConfigurationException {
		System.out.println("projectDir " + projectDir);
		System.out.println("listFilter " + listFilter);
		System.out.println("outputFile " + outputFile);
		System.out.println("inFileName " + inFileName);



		ToolRmove tool = new ToolRmove(out);
		if(projectDir == null || "".equals(projectDir)){
			tool.out.println("Project folder doesn't exists");
			return;
		}

		String outPath = FileUtils.seekToResPath(projectDir);
		File project = new File(outPath);

		if (listFilter != null) {
			StringBuffer sb = new StringBuffer();
			FileUtils.readToBuffer(sb, listFilter);
			String[] list = sb.toString().split("\n");
			System.out.println("list size: " + list.length + ", " + list[0]);
			if (list.length > 0) {
				ignoreComment = true;
			} else {
				ignoreComment = false;
			}
			tool.filterList = Arrays.asList(list);
		} else {
			System.out.println("Please support a string name-list that to be removed!");
			return;
		}


//		tool.outExcelFile = new File(outputFile != null ? outputFile : "exported_strings_" + System.currentTimeMillis() + ".xls");
		tool.project = project.getParentFile().getName();
		tool.inputFileName = inFileName == null ? "strings.xml" : inFileName;
		tool.export(project);
	}
	
	private void export(File project) throws SAXException, IOException{
		File res = project;
		if (res == null || !res.exists()) {
			System.out.println("res folder doesn't exists");
			return;
		}
		for(File dir : res.listFiles()){
			if(!dir.isDirectory() || !dir.getName().startsWith(DIR_VALUES)){
				continue;
			}
			String dirName = dir.getName();
			if(dirName.equals(DIR_VALUES)){
				if (!removeDefLang(dir)) {
					System.out.println("res/values/ folder doesn't exists");
					return;
				}
			}
		}

		for(File dir : res.listFiles()){
			if(!dir.isDirectory() || !dir.getName().startsWith(DIR_VALUES)){
				continue;
			}
			String dirName = dir.getName();
			if(!dirName.equals(DIR_VALUES)){
				int index = dirName.indexOf('-');
				if(index == -1)
					continue;
				String lang = dirName.substring(index + 1);
				removeLang(lang, dir);
			}
		}
	}
	
	private void removeLang(String lang, File valueDir) throws FileNotFoundException, IOException, SAXException{
		File stringFile = new File(valueDir, inputFileName);
		// values-XX folder has not string.xml
		if(!stringFile.exists()){
			return;
		}
		System.out.println("Delete start, lan =" + lang);
		removeNodes(stringFile);
	}

	private void removeNodes(File file) throws FileNotFoundException, IOException, SAXException{
		Document dom = builder.parse(file);
		NodeList stringNodes = dom.getDocumentElement().getChildNodes();;
		for (int i = 0; i < stringNodes.getLength(); i++) {
			Node node = stringNodes.item(i);
			if ("string".equals(node.getNodeName())) {
				System.out.println("Delete start, node =" + node.getNodeName());
				System.out.println("Delete start, lan =" + "default");
				String stringId = node.getAttributes().getNamedItem("name").getNodeValue();
				if (filterList.contains(stringId)) {
					node.getParentNode().removeChild(node);
					System.out.println("Delete: " + stringId);
				}
			}
		}

		try {
			save(dom, file);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private boolean removeDefLang(File valueDir) throws FileNotFoundException, IOException, SAXException{
		File stringFile = new File(valueDir, inputFileName);
		if(!stringFile.exists()){
			return false;
		}

		System.out.println("Delete start, lan =" + "default");
		removeNodes(stringFile);
		return true;
	}

	private void save(Document doc, File destFile) throws TransformerException {

		//DOMUtils.prettyPrint(doc);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(destFile);

		transformer.transform(source, result);
	}

}
