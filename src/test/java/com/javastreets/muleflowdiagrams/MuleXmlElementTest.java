package com.javastreets.muleflowdiagrams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.javastreets.muleflowdiagrams.model.Attribute;
import com.javastreets.muleflowdiagrams.model.ComponentItem;
import com.javastreets.muleflowdiagrams.model.MuleComponent;
import com.javastreets.muleflowdiagrams.xml.XmlParser;

class MuleXmlElementTest {

  static Document document;

  @BeforeAll
  static void setupDocument() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
      document = db.newDocument();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  Document loadXml(String path) {
    XmlParser xmlParser = new XmlParser(getClass().getClassLoader().getResource(path).getFile());
    xmlParser.parse();
    return xmlParser.getDocument();
  }

  Element findElementByAttribute(Element parent, Attribute attribute) {
    Element result = null;
    for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
      Node node = parent.getChildNodes().item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element current = (Element) node;
        if (current.getAttribute(attribute.getName().toString()).equals(attribute.getValue())) {
          result = current;
          break;
        }
      }
    }
    return result;
  }

  MuleComponent getMuleComponent(String type, String name) {
    MuleComponent expectedComponent = new MuleComponent(type, name);
    expectedComponent.addAttribute("name", name);
    return expectedComponent;
  }

  MuleComponent getFlowRefComponent(String name) {
    return getMuleComponent("flow-ref", name);
  }


  @Test
  void isFlowOrSubflow_ForFlow() {
    assertThat(MuleXmlElement.isFlowOrSubflow(document.createElement("flow")))
        .as("flow element identified as flow/subflow").isTrue();
  }

  @Test
  void isFlowOrSubflow_ForSubflow() {
    assertThat(MuleXmlElement.isFlowOrSubflow(document.createElement("sub-flow")))
        .as("sub-flow element identified as flow/subflow").isTrue();
  }

  @Test
  void isFlowOrSubflow_ForNonFlowSubflow() {
    assertThat(MuleXmlElement.isFlowOrSubflow(document.createElement("logger")))
        .as("logger element identified as flow/subflow").isFalse();
  }

  @Test
  @DisplayName("Subflow element identification")
  void isSubFlow() {
    assertThat(MuleXmlElement.isSubFlow(document.createElement("sub-flow")))
        .as("sub-flow element identified as subflow").isTrue();
    assertThat(MuleXmlElement.isSubFlow(document.createElement("flow")))
        .as("flow element identified as subflow").isFalse();
  }

  @Test
  void isFlowRef() {
    assertThat(MuleXmlElement.isFlowRef(document.createElement("flow-ref")))
        .as("flow-ref element identified as flow-re").isTrue();
    assertThat(MuleXmlElement.isFlowRef(document.createElement("flow")))
        .as("flow-ref element identified as flow-ref").isFalse();
  }

  @Test
  void isAsync() {
    assertThat(MuleXmlElement.isAsync(document.createElement("async")))
        .as("async element identified as async").isTrue();
    assertThat(MuleXmlElement.isAsync(document.createElement("flow")))
        .as("flow element identified as async").isFalse();
  }

  @ParameterizedTest
  @MethodSource("isaScopeArgumentProvider")
  void isaScope(Element element, boolean expected) {
    assertThat(MuleXmlElement.isaScope(element)).isEqualTo(expected);
  }

  static Stream<Arguments> isaScopeArgumentProvider() {
    return Arrays.asList("ee:cache", "try", "async", "until-successful", "foreach", "x-logger")
        .stream().map(scopeName -> Arguments.of(document.createElement(scopeName),
            !scopeName.startsWith("x-")))
        .collect(Collectors.toList()).stream();
  }

  @ParameterizedTest
  @MethodSource("isaRouterArgumentProvider")
  void isaRouter(Element element, boolean expected) {
    assertThat(MuleXmlElement.isaRouter(element)).isEqualTo(expected);
  }

  static Stream<Arguments> isaRouterArgumentProvider() {
    return Arrays.asList("choice", "scatter-gather", "round-robin", "first-successful", "x-logger")
        .stream().map(scopeName -> Arguments.of(document.createElement(scopeName),
            !scopeName.startsWith("x-")))
        .collect(Collectors.toList()).stream();
  }

  @ParameterizedTest
  @MethodSource("isaRouteArgumentProvider")
  void isaRoute(Element element, boolean expected) {
    assertThat(MuleXmlElement.isaRoute(element)).isEqualTo(expected);
  }

  static Stream<Arguments> isaRouteArgumentProvider() {
    return Arrays
        .asList("when", "otherwise", "route", "on-error-propagate", "on-error-continue", "x-logger")
        .stream().map(scopeName -> Arguments.of(document.createElement(scopeName),
            !scopeName.startsWith("x-")))
        .collect(Collectors.toList()).stream();
  }

  @Test
  void isanErrorHandler() {
    assertThat(MuleXmlElement.isanErrorHandler(document.createElement("error-handler")))
        .as("error-handler element identified as error-handler").isTrue();
    assertThat(MuleXmlElement.isanErrorHandler(document.createElement("flow")))
        .as("flow element identified as error-handler").isFalse();
  }

  @Test
  void testIsFlowRef() {
    assertThat(MuleXmlElement.isFlowRef(new MuleComponent("flow-ref", "test-name")))
        .as("flow-ref element identified as flow-refs").isTrue();
  }

  @Test
  @DisplayName("Element without children returns empty component list")
  void fillComponents_with_childs() {
    Element element =
        getElementWithAttributes("flow-ref", Attribute.with("name", "test-flow-name"));
    List<MuleComponent> components = MuleXmlElement.fillComponents(element, Collections.emptyMap());
    assertThat(components).as("List of Mule components processed").isEmpty();
  }

  @Test
  @DisplayName("Element with none element child returns empty component list")
  void fillComponents_with_non_element_node_childs() {
    Element element =
        getElementWithAttributes("flow-ref", Attribute.with("name", "test-flow-name"));
    element.appendChild(document.createTextNode("test"));
    List<MuleComponent> components = MuleXmlElement.fillComponents(element, Collections.emptyMap());
    assertThat(components).as("List of Mule components processed").isEmpty();
  }

  @Test
  @DisplayName("Element with flow-ref element child")
  void fillComponents_with_flow_ref_child() {
    Element element = getElementWithAttributes("flow", Attribute.with("name", "test-flow-name"));
    element.appendChild(
        getElementWithAttributes("flow-ref", Attribute.with("name", "test-sub-flow-name")));

    String name = "test-sub-flow-name";
    MuleComponent expectedComponent = getFlowRefComponent(name);

    List<MuleComponent> components = MuleXmlElement.fillComponents(element, Collections.emptyMap());
    assertThat(components).as("List of Mule components processed").isNotEmpty().hasSize(1)
        .containsExactly(expectedComponent);
  }

  @Test
  @DisplayName("Element with flow-ref element child")
  void fillComponents_with_known_components() {
    Element element = getElementWithAttributes("flow", Attribute.with("name", "test-flow-name"));
    element.appendChild(getElementWithAttributes("http:listener", Attribute.with("path", "/test"),
        Attribute.with("configRef", "http-config")));

    ComponentItem item = new ComponentItem();
    item.setPrefix("http");
    item.setOperation("listener");
    item.setSource(true);
    item.setPathAttributeName("path");
    item.setConfigAttributeName("configRef");

    List<MuleComponent> components = MuleXmlElement.fillComponents(element,
        Collections.singletonMap(item.qualifiedName(), item));

    MuleComponent expectedComponent = new MuleComponent("http", "/test");
    expectedComponent.setAsync(true);
    expectedComponent.setSource(true);
    expectedComponent.setPath(Attribute.with("path", "/test"));
    expectedComponent.setConfigRef(Attribute.with("configRef", "http-config"));

    assertThat(components).as("List of Mule components processed").isNotEmpty().hasSize(1)
        .allSatisfy(c -> assertThat(c).isEqualToComparingFieldByField(expectedComponent));

  }

  @Test
  @DisplayName("Element with router element child")
  void fillComponents_with_router_child() {
    Document document = loadXml("mule-xmls/router-config.xml");
    SoftAssertions softly = new SoftAssertions();
    Consumer<String> test = (router) -> {
      Element mainFlow = findElementByAttribute(document.getDocumentElement(),
          Attribute.with("name", "router-config-" + router));
      List<MuleComponent> components =
          MuleXmlElement.fillComponents(mainFlow, Collections.emptyMap());
      softly.assertThat(components).as("List of Mule components processed for router - " + router)
          .isNotEmpty().hasSize(2).containsExactly(getFlowRefComponent(router + "-route-1"),
              getFlowRefComponent(router + "-route-2"));
    };

    MuleXmlElement.routers.forEach(router -> test.accept(router));
    softly.assertAll();
  }

  @Test
  @DisplayName("Element with scopes element child")
  void fillComponents_with_scopes_child() {
    Document document = loadXml("mule-xmls/scopes-config.xml");
    SoftAssertions softly = new SoftAssertions();
    Consumer<String> test = (scope) -> {
      Element mainFlow = findElementByAttribute(document.getDocumentElement(),
          Attribute.with("name", "scope-config-" + scope));
      assertThat(mainFlow).as("Main flow scope-config-" + scope + " for scope " + scope)
          .isNotNull();
      List<MuleComponent> components =
          MuleXmlElement.fillComponents(mainFlow, Collections.emptyMap());
      MuleComponent expected = getFlowRefComponent(scope + "-sub-flow");
      expected.setAsync(scope.equalsIgnoreCase("async"));
      softly.assertThat(components).as("List of Mule components processed for router - " + scope)
          .isNotEmpty().hasSize(1).containsExactly(expected);
    };

    MuleXmlElement.scopes.forEach(router -> test.accept(router));
    softly.assertAll();
  }

  @Test
  @DisplayName("Element with error handler element child")
  void fillComponents_with_error_handler_child() {
    Document document = loadXml("mule-xmls/error-config.xml");
    Element mainFlow = findElementByAttribute(document.getDocumentElement(),
        Attribute.with("name", "error-configFlow"));
    List<MuleComponent> components =
        MuleXmlElement.fillComponents(mainFlow, Collections.emptyMap());
    assertThat(components).as("List of Mule components processed").isNotEmpty().hasSize(3)
        .containsExactly(getFlowRefComponent("try-sub-flow"),
            getFlowRefComponent("on-error-sub-flow"),
            getFlowRefComponent("on-error-propagate-sub-flow"));
  }

  private Element getElementWithAttributes(String name, Attribute... attributes) {
    Element element;
    element = document.createElement(name);
    if (attributes != null) {
      Arrays.asList(attributes).forEach(attribute -> element
          .setAttribute(attribute.getName().toString(), attribute.getValue().toString()));
    }
    return element;
  }
}
