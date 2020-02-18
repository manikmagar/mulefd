package com.github.manikmagar.mule.flowdiagrams.drawings;

import com.github.manikmagar.mule.flowdiagrams.model.Component;
import com.github.manikmagar.mule.flowdiagrams.model.MuleFlow;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public interface Diagram {

  boolean draw(DrawingContext drawingContext);

  boolean supports(DiagramType diagramType);

  String name();

  default String getDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm a");
    LocalDateTime date = LocalDateTime.now();
    return date.format(formatter);
  }

  default MuleFlow targetFlowByName(String name, List<Component> components) {
    return components.stream()
        .filter(component -> component.isFlowKind() && component.getName().equals(name)).findFirst()
        .map(component -> (MuleFlow) component).orElse(null);
  }
}
