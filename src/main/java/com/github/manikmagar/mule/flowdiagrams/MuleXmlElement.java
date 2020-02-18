package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class MuleXmlElement {
  public static final String ELEMENT_FLOW = "flow";
  public static final String ELEMENT_SUB_FLOW = "sub-flow";
  public static final String ELEMENT_FLOW_REF = "flow-ref";
  public static final String ELEMENT_SCOPE_ASYNC = "async";

  public static boolean isFlowOrSubflow(Element element) {
    return element.getNodeName().equalsIgnoreCase(ELEMENT_FLOW)
        || element.getNodeName().equalsIgnoreCase(ELEMENT_SUB_FLOW);
  }

  public static boolean isSubFlow(Element element) {
    return element.getNodeName().equalsIgnoreCase(ELEMENT_SUB_FLOW);
  }

  public static boolean isFlowRef(Element element) {
    return element.getNodeName().equalsIgnoreCase(ELEMENT_FLOW_REF);
  }

  public static boolean isScope(Element element, String scopeName) {
    return element.getNodeName().equalsIgnoreCase(scopeName);
  }

  public static boolean isFlowRef(MuleComponent component) {
    return component.getName().equalsIgnoreCase(ELEMENT_FLOW_REF);
  }

  public static List<MuleComponent> fillComponents(Element flowElement) {
    List<MuleComponent> muleComponentList = new ArrayList<>();
    NodeList children = flowElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        if (isFlowRef(element)) {
          String nameAttr = element.getAttribute("name");
          MuleComponent component = new MuleComponent(element.getNodeName(), nameAttr);
          component.addAttribute("name", nameAttr);
          muleComponentList.add(component);
        }
        if (isScope(element, ELEMENT_SCOPE_ASYNC)) {
          // Check if any flow ref exists in async
          fillComponents(element).forEach(component -> {
            component.setAsync(true);
            muleComponentList.add(component);
          });
        }

      }
    }
    return muleComponentList;
  }
}
