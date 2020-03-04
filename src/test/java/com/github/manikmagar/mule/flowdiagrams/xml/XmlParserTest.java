package com.github.manikmagar.mule.flowdiagrams.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
