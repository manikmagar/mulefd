package com.javastreets.mulefd.drawings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.javastreets.mulefd.model.Component;
import com.javastreets.mulefd.model.FlowContainer;

public interface Diagram {

  boolean draw(DrawingContext drawingContext);

  /**
   * This default method returns lines used to write diagram header. Empty line entries are added to
   * create space between header and the diagram.
   * 
   * @return String
   */
  default String[] getDiagramHeaderLines() {
    return new String[] {"Mule Flows - " + name() + " Diagram",
        "Generated on : " + getDate() + " by mulefd", "", ""};
  }

  boolean supports(DiagramType diagramType);

  String name();

  /**
   * Returns "EEE, dd MMM yyyy hh:mm a" formatted current date.
   * 
   * @return String Date
   */
  default String getDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm a");
    LocalDateTime date = LocalDateTime.now();
    return date.format(formatter);
  }

  /**
   * This method finds a flow or a sub-flow by a given name.
   * 
   * @param name of a flow/sub-flow to find
   * @param components {@link List<Component>} to find flow/sub-flow in.
   * @return <{@link FlowContainer}
   */
  default FlowContainer targetFlowByName(String name, List<Component> components) {
    return components.stream()
        .filter(component -> component.isFlowKind() && component.getName().equals(name)).findFirst()
        .map(component -> (FlowContainer) component).orElse(null);
  }

  /**
   * This method find flows or sub-flows having given <code>suffix</code> in name.
   * 
   * @param suffix @{@link String}
   * @param components {@link List<Component>} to find flow/sub-flow in.
   * @return {@link List<FlowContainer>}
   */
  default List<FlowContainer> searchFlowBySuffix(String suffix, List<Component> components) {
    return components.stream()
        .filter(component -> component.isFlowKind() && component.getName().endsWith(suffix))
        .map(component -> (FlowContainer) component).collect(Collectors.toList());
  }
}
