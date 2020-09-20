package com.javastreets.mulefd;

import java.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.javastreets.mulefd.model.Attribute;
import com.javastreets.mulefd.model.ComponentItem;
import com.javastreets.mulefd.model.MuleComponent;

public class MuleXmlElement {
  public static final String ELEMENT_FLOW = "flow";
  public static final String ELEMENT_SUB_FLOW = "sub-flow";
  public static final String ELEMENT_FLOW_REF = "flow-ref";
  public static final String ELEMENT_SCOPE_ASYNC = "async";
  public static final String ELEMENT_ERROR_HANDLER = "error-handler";

  private MuleXmlElement() {}

  public static final List<String> scopes = Collections
      .unmodifiableList(Arrays.asList("ee:cache", "try", "async", "until-successful", "foreach"));
  public static final List<String> routers = Collections.unmodifiableList(
      Arrays.asList("choice", "scatter-gather", "round-robin", "first-successful"));
  private static final List<String> routes =
      Arrays.asList("when", "otherwise", "route", "on-error-propagate", "on-error-continue");

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

  public static boolean isaScope(Element element) {
    return scopes.contains(element.getNodeName().toLowerCase());
  }

  public static boolean isaRouter(Element element) {
    return routers.contains(element.getNodeName().toLowerCase());
  }

  public static boolean isaRoute(Element element) {
    return routes.contains(element.getNodeName().toLowerCase());
  }

  public static boolean isanErrorHandler(Element element) {
    return ELEMENT_ERROR_HANDLER.equalsIgnoreCase(element.getNodeName());
  }

  public static boolean isFlowRef(MuleComponent component) {
    return component.getType().equalsIgnoreCase(ELEMENT_FLOW_REF);
  }

  public static List<MuleComponent> fillComponents(Element flowElement,
      Map<String, ComponentItem> knownComponents) {
    Objects.requireNonNull(flowElement, "Flow element must not be null");
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
        if (isaScope(element)) {
          fillComponents(element, knownComponents).forEach(component -> {
            component.setAsync(isAsync(element));
            muleComponentList.add(component);
          });
        }
        if (isaRouter(element)) {
          muleComponentList.addAll(parseContainerElement(element, knownComponents));
        }
        if (isanErrorHandler(element)) {
          muleComponentList.addAll(parseContainerElement(element, knownComponents));
        }

        processKnownComponents(knownComponents, muleComponentList, element);

      }
    }
    return muleComponentList;
  }

  static void processKnownComponents(Map<String, ComponentItem> knownComponents,
      List<MuleComponent> muleComponentList, Element element) {
    ComponentItem item = null;
    String keyName = element.getTagName();
    String[] wildcards = null;
    if (knownComponents.containsKey(keyName)) {
      item = knownComponents.get(keyName);
    } else if (element.getTagName().contains(":")) {
      wildcards = element.getTagName().split(":");
      String wildcardEntry = wildcards[0] + ":*";
      if (knownComponents.containsKey(wildcardEntry)) {
        item = knownComponents.get(wildcardEntry);
      }
    }

    if (item != null) {
      String name = item.qualifiedName();
      if (!item.getPathAttributeName().trim().isEmpty()) {
        name = element.getAttribute(item.getPathAttributeName());
      }
      if (wildcards != null && wildcards.length > 1) {
        name = wildcards[1];
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
      mc.setAsync(item.isAsync());
      muleComponentList.add(mc);
    }


  }

  /**
   * Parses router elements such as choice, scatter-gather, round-robin, first-successful
   * 
   * @param element
   * @param knownComponents
   * @return
   */
  private static List<MuleComponent> parseContainerElement(Element element,
      Map<String, ComponentItem> knownComponents) {
    List<MuleComponent> muleComponentList = new ArrayList<>();
    NodeList routes = element.getChildNodes();
    for (int i = 0; i < routes.getLength(); i++) {
      Node route = routes.item(i);
      if (route.getNodeType() == Node.ELEMENT_NODE) {
        Element ele = (Element) route;
        if (isaRoute(ele)) {
          muleComponentList.addAll(fillComponents(ele, knownComponents));
        }
      }
    }
    return muleComponentList;
  }

}
