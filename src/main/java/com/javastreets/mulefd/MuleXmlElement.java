package com.javastreets.mulefd;

import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.javastreets.mulefd.model.Attribute;
import com.javastreets.mulefd.model.ComponentItem;
import com.javastreets.mulefd.model.KnownMuleComponent;
import com.javastreets.mulefd.model.MuleComponent;

public class MuleXmlElement {
  public static final String ELEMENT_FLOW = "flow";
  public static final String ELEMENT_SUB_FLOW = "sub-flow";
  public static final String ELEMENT_FLOW_REF = "flow-ref";
  public static final String ELEMENT_SCOPE_ASYNC = "async";
  public static final String ELEMENT_ERROR_HANDLER = "error-handler";
  public static final String XPATH_INDICATOR = "xpath:";

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

  public static boolean isMuleCoreElement(Element element) {
    return "mule".equalsIgnoreCase(element.getNodeName());
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

  /**
   * Checks if component represented by the given {@link Element} is known i.e. configured in the
   * external CSV file.
   * 
   * @param knownComponents {@link Map} of element name {@link String} and {@link ComponentItem}
   *        definition
   * @param muleComponentList Modifiable list of {@link MuleComponent}s to add known instances
   * @param element {@link Element} representing the target component
   */
  static void processKnownComponents(Map<String, ComponentItem> knownComponents,
      List<MuleComponent> muleComponentList, Element element) {
    ComponentItem item = null;
    String keyName = element.getTagName();
    String[] wildcards = null;
    if (knownComponents.containsKey(keyName)) {
      item = knownComponents.get(keyName);
    } else if (!element.getNodeName().contains(":")) {
      item = knownComponents.get(MuleComponent.toMuleCoreComponentName(element.getNodeName()));
    } else if (element.getTagName().contains(":")) {
      wildcards = element.getTagName().split(":");
      String wildcardEntry = wildcards[0] + ":*";
      if (knownComponents.containsKey(wildcardEntry)) {
        item = knownComponents.get(wildcardEntry);
      }
    }

    if (item != null) {
      String name = item.getOperation();
      if (wildcards != null && wildcards.length > 1) {
        name = wildcards[1];
      }
      KnownMuleComponent mc = new KnownMuleComponent(item.getPrefix(), name);
      mc.setSource(item.isSource());
      if (!item.getConfigAttributeName().trim().isEmpty()) {
        mc.setConfigRef(Attribute.with(item.getConfigAttributeName(),
            element.getAttribute(item.getConfigAttributeName())));
      }
      String pathExpression = item.getPathAttributeName().trim();
      if (!pathExpression.isEmpty()) {
        final String evaluatedPath =
            isXPath(pathExpression) ? evaluateXpathOnElement(element, pathExpression)
                : element.getAttribute(item.getPathAttributeName());
        mc.setPath(Attribute.with(item.getPathAttributeName(), evaluatedPath));
      }
      mc.setAsync(item.isAsync());
      muleComponentList.add(mc);
    }


  }

  private static String evaluateXpathOnElement(Element element, String expression) {
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      return (String) xPath.compile(expression.substring(XPATH_INDICATOR.length()))
          .evaluate(element, XPathConstants.STRING);
    } catch (XPathExpressionException e) {
      throw new MuleFDException(
          "Could not evaluate " + expression + " on element " + element.getNodeName(), e);
    }
  }

  private static boolean isXPath(final String expression) {
    return expression.startsWith(XPATH_INDICATOR);
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
