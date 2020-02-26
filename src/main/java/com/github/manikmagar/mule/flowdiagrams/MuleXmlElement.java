package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.model.Attribute;
import com.github.manikmagar.mule.flowdiagrams.model.ComponentItem;
import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MuleXmlElement {
  public static final String ELEMENT_FLOW = "flow";
  public static final String ELEMENT_SUB_FLOW = "sub-flow";
  public static final String ELEMENT_FLOW_REF = "flow-ref";
  public static final String ELEMENT_SCOPE_ASYNC = "async";

  private static final List<String> loops = Arrays.asList("foreach", "parallel-foreach");
  private static final List<String> scopes =
      Arrays.asList("cache", "try", "async", "until-successful", "foreach", "parallel-foreach");

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

  public static boolean isAsync(Element element) {
    return element.getNodeName().equalsIgnoreCase(ELEMENT_SCOPE_ASYNC);
  }

  public static boolean isScope(Element element) {
    return scopes.contains(element.getNodeName().toLowerCase());
  }

  public static boolean isForLoop(Element element) {
    return loops.contains(element.getNodeName().toLowerCase());
  }

  public static boolean isFlowRef(MuleComponent component) {
    return component.getName().equalsIgnoreCase(ELEMENT_FLOW_REF);
  }

  public static List<MuleComponent> fillComponents(Element flowElement,
      Map<String, ComponentItem> knownComponents) {
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
        if (isScope(element)) {
          fillComponents(element, knownComponents).forEach(component -> {
            component.setAsync(isAsync(element));
            muleComponentList.add(component);
          });
        }

        if (knownComponents.containsKey(element.getTagName())) {
          ComponentItem item = knownComponents.get(element.getTagName());
          String name = item.qualifiedName();
          if (!item.getPathAttributeName().trim().isEmpty()) {
            name = element.getAttribute(item.getPathAttributeName());
          }
          MuleComponent mc = new MuleComponent(item.getPrefix(), name);
          mc.setSource(item.isSource());
          if (!item.getConfigAttributeName().trim().isEmpty()) {
            mc.setConfigRef(Attribute.with(item.getConfigAttributeName(),
                element.getAttribute(item.getConfigAttributeName())));
          }
          if (!item.getPathAttributeName().trim().isEmpty()) {
            mc.setPath(Attribute.with(item.getPathAttributeName(),
                element.getAttribute(item.getPathAttributeName())));
          }
          mc.setAsync(true);
          muleComponentList.add(mc);
        }

      }
    }
    return muleComponentList;
  }
}
