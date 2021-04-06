package com.zykj.purchase.common;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlToMapUtil {
	public static Map<String, String> getXMLStringValue(String result) {

		Map<String, String> rm = new HashMap<String, String>();
		StringReader sr = new StringReader(result); // result数据源：XML格式字符串
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		org.w3c.dom.Document document = null;
		try {
			document = builder.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element rootElement = document.getDocumentElement();
		NodeList nodes = rootElement.getChildNodes(); // 一级标签

		for (int i = 0; i < nodes.getLength(); i++) {

			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getFirstChild() != null) {
					rm.put(node.getNodeName(), node.getFirstChild().getNodeValue());
				}
				NodeList nodeList = node.getChildNodes(); // 二级标签
				if (nodeList != null) {
					for (int j = 0; j < nodeList.getLength(); j++) {
						Node childnode1 = nodeList.item(j);
						if (childnode1 instanceof Element) {
							if (childnode1.getFirstChild() != null) {
								rm.put(childnode1.getNodeName(), childnode1.getFirstChild().getNodeValue());
							}
							NodeList nodeList1 = childnode1.getChildNodes(); // 三级标签

							if (nodeList1 != null) {
								for (int k = 0; k < nodeList1.getLength(); k++) {
									Node childnode2 = nodeList1.item(k);
									if (childnode2 instanceof Element) {
										if (childnode2.getFirstChild() != null) {
											rm.put(childnode2.getNodeName(), childnode2.getFirstChild().getNodeValue());
										}
										NodeList nodeList2 = childnode2.getChildNodes();

										// 当有四级或以上标签时，在此重复写解析代码
									}
								}

							}
						}
					}

				}
			}
		}
		return rm;

	}
}
