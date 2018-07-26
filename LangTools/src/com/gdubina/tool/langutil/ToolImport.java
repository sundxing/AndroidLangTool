package com.gdubina.tool.langutil;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.io.*;
import java.util.Iterator;

public class ToolImport {

	private DocumentBuilder builder;
	private File outResDir;
	private PrintStream out;

	public ToolImport(PrintStream out) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();
		this.out = out == null ? System.out : out;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ParserConfigurationException, TransformerException, SAXException {
		if(args == null || args.length == 0){
			System.out.println("File name is missed");
			return;
		}
		run(args[0]);
	}
	
	public static void run(String input) throws FileNotFoundException, IOException, ParserConfigurationException, TransformerException, SAXException {
		if(input == null || "".equals(input)){
			System.out.println("File name is missed");
			return;
		}
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(new File(input)));
		HSSFSheet sheet = wb.getSheetAt(0);
		

		ToolImport tool = new ToolImport(null);
		tool.outResDir = new File("out/" + sheet.getSheetName()+ "/res");
		tool.outResDir.mkdirs();
		tool.parse(sheet);
	}
	
	public static void run(PrintStream out, String projectDir, String input) throws FileNotFoundException, IOException, ParserConfigurationException, TransformerException, SAXException {
		ToolImport tool = new ToolImport(out);
		if(input == null || "".equals(input)){
			tool.out.println("File name is missed");
			return;
		}
		
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(new File(input)));
		HSSFSheet sheet = wb.getSheetAt(0);
		

		tool.outResDir = new File(projectDir, "/res");
		//tool.outResDir.mkdirs();
		tool.parse(sheet);
	}

	private void parse(HSSFSheet sheet) throws IOException, TransformerException, SAXException {
		Row row = sheet.getRow(0);
		Iterator<Cell> cells = row.cellIterator();
		cells.next();// ignore key
		int i = 1;
		while (cells.hasNext()) {
			String lang = cells.next().toString();
			generateLang(sheet, lang, i);
			i++;
		}
	}

	/**
	 * 单个语言项目
	 * @param sheet
	 * @param lang
	 * @param column
	 * @throws IOException
	 * @throws TransformerException
	 * @throws SAXException
	 */
	private void generateLang(HSSFSheet sheet, String lang, int column) throws IOException, TransformerException, SAXException {
		File file = getOutResDir(outResDir, lang);
		Document dom = null;
		Element root = null;
		boolean appendMode = file.exists();
		if (appendMode) {
			System.out.println( file.getAbsolutePath() + "; File has exit");
			Document document = builder.parse(file);
			document.setXmlStandalone(true);
			NodeList nodeList = document.getElementsByTagName("resources");
			appendMode = nodeList != null && nodeList.getLength() == 1;
			if (appendMode) {
				dom = document;
				root = (Element) nodeList.item(0);
			}
			// 更新
		}

		if (!appendMode) {
			System.out.println( file.getAbsolutePath() + "; Create new file");

			dom = builder.newDocument();
			root = dom.createElement("resources");
			dom.appendChild(root);
		}
		
		Iterator<Row> iterator = sheet.rowIterator();
		iterator.next();//ignore first row;
		Element pluralsNode = null;
		Element arraysNode = null;
		String plurarName = null;
		String arrayName = null;

		while (iterator.hasNext()) {
			HSSFRow row = (HSSFRow) iterator.next();
			Cell cell = row.getCell(0);// android key
			if (cell == null) {
				continue;
			}
			String key = cell.toString();
			if (key == null || "".equals(key)){
				root.appendChild(dom.createTextNode(""));
				continue;
			}
			if(key.startsWith("/**")){
				root.appendChild(dom.createComment(key.substring(3, key.length() - 3)));
				continue;
			}
			
			if(key.startsWith("//")){
				root.appendChild(dom.createComment(key.substring(2)));
				continue;
			}
			
			int plurarIndex = key.indexOf("#");
			int arrayDotIndex = key.indexOf(".");
			if(plurarIndex == -1 && arrayDotIndex == -1){//string
				System.out.println("add key : " + key);
				Cell valueCell = row.getCell(column);
				if(valueCell == null){
					addEmptyKeyValue(dom, root, key);
					continue;
				}
				String value = valueCell.toString();// value
				
				if(value.isEmpty()){
					addEmptyKeyValue(dom, root, key);
				}else {
					Element node = dom.createElement("string");
					node.setAttribute("name", key);
					node.setTextContent(value);
					root.appendChild(node);
				}
			} else if (arrayDotIndex != -1) {
				Cell valueCell = row.getCell(column);
				String value = "";
				if(valueCell != null){
					value = valueCell.toString();// value
				}
				String arrayNameNew = key.substring(0, arrayDotIndex);
				if(!arrayNameNew.equals(arrayName)){
					arrayName = arrayNameNew;
					arraysNode = dom.createElement("string-array");
					arraysNode.setAttribute("name", arrayName);
				}
				Element item = dom.createElement("item");
				item.setTextContent(value);

				arraysNode.appendChild(item);

				root.appendChild(arraysNode);
			}else{
				Cell valueCell = row.getCell(column);
				String value = "";
				if(valueCell != null){
					value = valueCell.toString();// value
				}
				String plurarNameNew = key.substring(0, plurarIndex);
				String quantity = key.substring(plurarIndex + 1);
				if(!plurarNameNew.equals(plurarName)){
					plurarName = plurarNameNew; 
					pluralsNode = dom.createElement("plurals");
					pluralsNode.setAttribute("name", plurarName);
				}
				Element item = dom.createElement("item");
				item.setAttribute("quantity", quantity);
				item.setTextContent(value);
				
				pluralsNode.appendChild(item);
				
				root.appendChild(pluralsNode);				
			}

		}


		save(dom, file);
	}
	
	private static void addEmptyKeyValue(Document dom, Element root, String key){
		root.appendChild(dom.createComment(String.format(" TODO: string name=\"%s\" ", key)));
	}

	private File getOutResDir(File outDir, String lang) {
		File dir;
		if ("default".equals(lang) || lang == null || "".equals(lang)) {
			dir = new File(outDir, "values");
		} else {
			dir = new File(outDir, "values-" + lang);
		}
		dir.mkdir();
		return new File(dir, "strings.xml");
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

	public static void appendXMLByDOM4J(String fileName) throws IOException {
		// 1.创建一个SAXReader对象reader
		SAXReader reader = new SAXReader();
		try {
			// 2.通过reader对象的read方法加载xml文件，获取Document对象
			org.dom4j.Document document = reader.read(new File(fileName));
			org.dom4j.Element bookStore = document.getRootElement();

			org.dom4j.Element book = bookStore.element("book");
			org.dom4j.Element language = book.addElement("language");
			language.setText("简体中文");

			// 3.设置输出格式和输出流
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileOutputStream(
					"books_append_dom4j.xml"), format);
			writer.write(document);// 将文档写入到输出流
			writer.close();

		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
/*
	private static String eluminateText(String text) {
		return text.replace("'", "\\'").replace("\"", "\\\"");
	}
*/
}
