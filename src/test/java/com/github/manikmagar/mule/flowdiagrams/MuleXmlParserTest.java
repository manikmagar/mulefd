package com.github.manikmagar.mule.flowdiagrams;

import com.github.manikmagar.mule.flowdiagrams.model.FlowContainer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MuleXmlParserTest {

  @Test
  void parse() {
    MuleXmlParser xmlParser = new MuleXmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    assertThat(xmlParser.getDocument()).isNotNull();
  }

  @Test
  void isMuleFile() {
    MuleXmlParser xmlParser = new MuleXmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    assertThat(xmlParser.isMuleFile()).isTrue();
  }

  @Test
  void isNotMuleFile() {
    MuleXmlParser xmlParser = new MuleXmlParser("src/test/resources/non-mule-file.xml");
    xmlParser.parse();
    assertThat(xmlParser.isMuleFile()).isFalse();
  }

  @Test
  void getMuleFlows() {
    MuleXmlParser xmlParser = new MuleXmlParser("src/test/resources/example-config.xml");
    xmlParser.parse();
    List<FlowContainer> flowContainers = xmlParser.getMuleFlows(Collections.emptyMap());
    assertThat(flowContainers).as("List of flows/subflows detected").hasSize(2)
        .allSatisfy(flowContainer -> {
          assertThat(flowContainer.isFlowKind()).isTrue();
          if (flowContainer.isaFlow()) {
            assertThat(flowContainer).extracting("type", "name").containsExactly("flow",
                "test-hello-appFlow");

          } else {
            assertThat(flowContainer).extracting("type", "name").containsExactly("sub-flow",
                "sub-flow-level-1-1");
          }
        });


  }
}
