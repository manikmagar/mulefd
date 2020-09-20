package com.javastreets.mulefd.xml;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlParserTest {

  @Test
  void getDocument() {
    XmlParser xmlParser = new XmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    Document doc = xmlParser.getDocument();
    assertThat(doc).isNotNull();
    Element ele = doc.getDocumentElement();
    assertThat(ele).isNotNull().extracting(Element::getNodeName).isEqualTo("mule");
  }

  @Test
  void safeDocumentBuilderFactory() throws ParserConfigurationException {
    XmlParser xmlParser = new XmlParser("src/test/resources/example-config.xml");
    DocumentBuilderFactory factory = xmlParser.safeDocumentBuilderFactory();
    assertThat(factory.isXIncludeAware()).as("XInclude Aware").isFalse();
    assertThat(factory.isExpandEntityReferences()).as("Expand Entity References").isFalse();
    assertThat(factory.getFeature("http://apache.org/xml/features/disallow-doctype-decl"))
        .as("disallow-doctype-decl").isTrue();
  }

  @Test
  void parse() {
    XmlParser xmlParser = new XmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    assertThat(xmlParser.getDocument()).isNotNull();
  }

  @Test
  void isElement() {
    XmlParser xmlParser = new XmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    Document doc = xmlParser.getDocument();
    assertThat(doc).isNotNull();
    Element ele = doc.getDocumentElement();
    assertThat(xmlParser.isElement(ele)).isTrue();
  }
}
