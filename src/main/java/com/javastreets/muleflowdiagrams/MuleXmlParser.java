package com.javastreets.muleflowdiagrams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.javastreets.muleflowdiagrams.model.ComponentItem;
import com.javastreets.muleflowdiagrams.model.FlowContainer;
import com.javastreets.muleflowdiagrams.xml.XmlParser;

public class MuleXmlParser extends XmlParser {

  public MuleXmlParser(String filePath) {
    super(filePath);
  }

  @Override
  public void parse() {
    super.parse();
  }

  public boolean isMuleFile() {
    return getDocument().getDocumentElement().getNodeName().equalsIgnoreCase("mule");
  }

  public List<FlowContainer> getMuleFlows(Map<String, ComponentItem> knownComponents) {
    return findChildrenForElement(knownComponents);
  }

  private List<FlowContainer> findChildrenForElement(Map<String, ComponentItem> knownComponents) {
    List<FlowContainer> flowContainers = new ArrayList<>();
    NodeList nodeList = getDocument().getDocumentElement().getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (isElement(node)) {
        Element element = (Element) node;
        if (MuleXmlElement.isFlowOrSubflow(element)) {
          FlowContainer mf = new FlowContainer(element.getNodeName(), element.getAttribute("name"));
          mf.getComponents().addAll(MuleXmlElement.fillComponents(element, knownComponents));
          flowContainers.add(mf);
        }
      }
    }
    return flowContainers;
  }

}
