package com.github.manikmagar.mule.flowdiagrams.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XmlParser {

  private final String filePath;

  public XmlParser(String filePath) {
    this.filePath = filePath;
  }

  private Document document;

  public Document getDocument() {
    return document;
  }

  public void parse() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      document = documentBuilder.parse(filePath);
      document.getDocumentElement().normalize();
    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
    }
  }


  protected boolean isElement(Node node) {
    return node.getNodeType() == Node.ELEMENT_NODE;
  }
}
