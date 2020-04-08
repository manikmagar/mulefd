package com.javastreets.muleflowdiagrams.xml;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlParser {

  Logger logger = LoggerFactory.getLogger(XmlParser.class);

  private final String filePath;

  public XmlParser(String filePath) {
    this.filePath = filePath;
  }

  private Document document;

  public Document getDocument() {
    return document;
  }

  public void parse() {
    try {
      DocumentBuilderFactory documentBuilderFactory = safeDocumentBuilderFactory();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      document = documentBuilder.parse(filePath);
      document.getDocumentElement().normalize();
    } catch (SAXException | IOException | ParserConfigurationException e) {
      logger.error("Failed to parse xml - " + filePath, e);
    }
  }

  DocumentBuilderFactory safeDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    // XXE protection -
    // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
    documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    documentBuilderFactory.setXIncludeAware(false);
    documentBuilderFactory.setExpandEntityReferences(false);
    return documentBuilderFactory;
  }


  protected boolean isElement(Node node) {
    return node.getNodeType() == Node.ELEMENT_NODE;
  }
}
