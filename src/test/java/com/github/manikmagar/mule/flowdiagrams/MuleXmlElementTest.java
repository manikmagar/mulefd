package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.model.Attribute;
import com.github.manikmagar.mule.flowdiagrams.model.Component;
import com.github.manikmagar.mule.flowdiagrams.model.MuleComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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
    return Arrays
        .asList("cache", "try", "async", "until-successful", "foreach", "parallel-foreach",
            "x-logger")
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

    MuleComponent expectedComponent = new MuleComponent("flow-ref", "test-sub-flow-name");
    expectedComponent.addAttribute("name", "test-sub-flow-name");

    List<MuleComponent> components = MuleXmlElement.fillComponents(element, Collections.emptyMap());
    assertThat(components).as("List of Mule components processed").isNotEmpty().hasSize(1)
        .containsExactly(expectedComponent);
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
