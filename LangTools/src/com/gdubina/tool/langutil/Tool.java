package com.gdubina.tool.langutil;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Tool {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParserConfigurationException, TransformerException, SAXException {
		if(args == null || args.length == 0){
			printHelp();
			return;
		}
		
		if("-i".equals(args[0])){
			ToolImport.run(args[1], args.length > 2 ? args[2] : null);
		}else if("-e".equals(args[0])){
			if (args.length > 2 && "-l".equals(args[2])) {
				String infileName = null;
				String outName = null;
				if (args.length > 4) {
					if ("-f".equals(args[4])) {
						infileName = args[5];
					} else {
						outName = args[4];
					}

					if (args.length > 6) {
						outName = args[6];
					}
				}

				ToolExport.run(null, args[1], args[3], outName,infileName);
			}
			else if (args.length > 2 && "-f".equals(args[2])) {
				ToolExport.run(args[1], args.length > 4 ? args[4] : null, args[3]);
			} else {
				ToolExport.run(args[1], args.length > 2 ? args[2] : null);
			}
		}else{
			printHelp();
		}
	}
	
	private static void printHelp(){
		System.out.println("commands format:");
		System.out.println("\texport: -e <project dir> [-l <list of string names to export(others are ignore)>] [-f <input file name>] <output file>");
		System.out.println("\timport: -i <input file>");
	}

}
