package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.model.ComponentItem;
import com.github.manikmagar.mule.flowdiagrams.model.MuleFlow;
import com.github.manikmagar.mule.flowdiagrams.xml.XmlParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.manikmagar.mule.flowdiagrams.MuleXmlElement.isFlowOrSubflow;

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

  public List<MuleFlow> getMuleFlows(Map<String, ComponentItem> knownComponents) {
    return findChildrenForElement(knownComponents);
  }

  private List<MuleFlow> findChildrenForElement(Map<String, ComponentItem> knownComponents) {
    List<MuleFlow> muleFlows = new ArrayList<>();
    NodeList nodeList = getDocument().getDocumentElement().getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (isElement(node)) {
        Element element = (Element) node;
        if (isFlowOrSubflow(element)) {
          MuleFlow mf = new MuleFlow(element.getNodeName(), element.getAttribute("name"));
          mf.getComponents().addAll(MuleXmlElement.fillComponents(element, knownComponents));
          muleFlows.add(mf);
        }
      }
    }
    return muleFlows;
  }

}
