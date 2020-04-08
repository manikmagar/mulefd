package com.javastreets.muleflowdiagrams.model;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComponentItemTest {

  @Test
  @DisplayName("Validate ComponentItem creation")
  void componentItem() {
    ComponentItem item = new ComponentItem();
    item.setPrefix("vm");
    item.setOperation("listner");
    item.setSource(true);
    item.setConfigAttributeName("config-ref");
    item.setPathAttributeName("path");

    assertThat(item)
        .extracting(ComponentItem::getPrefix, ComponentItem::getOperation, ComponentItem::isSource,
            ComponentItem::getConfigAttributeName, ComponentItem::getPathAttributeName)
        .containsExactly("vm", "listner", true, "config-ref", "path");
  }

}
