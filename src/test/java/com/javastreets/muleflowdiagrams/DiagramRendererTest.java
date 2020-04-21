package com.javastreets.muleflowdiagrams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.javastreets.muleflowdiagrams.app.CommandModel;
import com.javastreets.muleflowdiagrams.drawings.DiagramType;
import com.javastreets.muleflowdiagrams.drawings.DrawingContext;
import com.javastreets.muleflowdiagrams.model.FlowContainer;

import io.github.netmikey.logunit.api.LogCapturer;

class DiagramRendererTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DiagramRenderer.class, Level.DEBUG);

  @TempDir
  File tempDir;

  private CommandModel getCommandModel() {
    return getCommandModel(tempDir.toPath());
  }

  private CommandModel getCommandModel(Path sourcePath) {
    CommandModel model = new CommandModel();
    model.setDiagramType(DiagramType.GRAPH);
    model.setOutputFilename("test-output-file");
    model.setTargetPath(Paths.get("dummy-result-path"));
    model.setSourcePath(sourcePath);

    return model;
  }


  @Test
  @DisplayName("Empty source directory rendering return false")
  void emptySouceDirRendering() {
    DiagramRenderer renderer = Mockito.spy(new DiagramRenderer(getCommandModel()));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).isEmpty();
  }

  @Test
  @DisplayName("Skips rendering non-mule file")
  void skipsNonMuleFileRendering() {
    Path sourcePath = Paths.get("src/test/resources/renderer/non-mule");
    DiagramRenderer renderer = Mockito.spy(new DiagramRenderer(getCommandModel(sourcePath)));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).isEmpty();
    logs.assertContains(
        "Not a mule configuration file: " + Paths.get(sourcePath.toString(), "non-mule-file.xml"));
  }

  @Test
  @DisplayName("Source directory rendering with one config")
  void singleFileRendering() {
    DiagramRenderer renderer = Mockito
        .spy(new DiagramRenderer(getCommandModel(Paths.get("src/test/resources/renderer/single"))));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).as("Flow container list").hasSize(1)
        .extracting(FlowContainer::getType, FlowContainer::getName)
        .containsExactly(tuple("flow", "test-hello-appFlow"));
  }

  @Test
  @DisplayName("Create drawing context from command model")
  void toDrawingContext() {
    CommandModel commandModel = getCommandModel();
    commandModel.setFlowName("test-flow");
    assertThat(new DiagramRenderer(commandModel).drawingContext(commandModel))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile,
            DrawingContext::getFlowName)
        .containsExactly(DiagramType.GRAPH,
            new File(commandModel.getTargetPath().toFile(), commandModel.getOutputFilename()),
            "test-flow");
  }

  @Test
  @DisplayName("Prepare components from csv file")
  void prepareKnownComponents() {
    assertThat(new DiagramRenderer(getCommandModel()).prepareKnownComponents()).isNotEmpty();
  }

  @Test
  @DisplayName("Single config file as source")
  void sourcePathXmlFile() {
    Path sourcePath = Paths.get("src/test/resources/renderer/single/example-config.xml");
    assertThat(new DiagramRenderer(getCommandModel(sourcePath)).getMuleSourcePath())
        .as("Resolved mule configuration path").isEqualTo(sourcePath);
    logs.assertContains("Reading source file " + sourcePath.toString());
  }

  @Test
  @DisplayName("Mule 4 source directory")
  void sourcePathMule4Directory() {
    Path sourcePath = Paths.get("src/test/resources/renderer/mule4-example");
    assertThat(new DiagramRenderer(getCommandModel(sourcePath)).getMuleSourcePath())
        .as("Resolved mule configuration path")
        .isEqualTo(Paths.get(sourcePath.toString(), "src/main/mule"));
    logs.assertContains(
        "Found standard Mule 4 source structure 'src/main/mule'. Source is a Mule-4 project.");
  }

  @Test
  @DisplayName("Mule 3 - non-maven source directory")
  void sourcePathMule3NonMavenDirectory() {
    Path sourcePath = Paths.get("src/test/resources/renderer/mule3-example");
    assertThat(new DiagramRenderer(getCommandModel(sourcePath)).getMuleSourcePath())
        .as("Resolved mule configuration path")
        .isEqualTo(Paths.get(sourcePath.toString(), "src/main/app"));
    logs.assertContains(
        "Found standard Mule 3 source structure 'src/main/app'. Source is a Mule-3 project.");
  }

  @Test
  @DisplayName("Mule 3 - maven source directory")
  void sourcePathMule3MavenDirectory() {
    Path sourcePath = Paths.get("src/test/resources/renderer/mule3-maven-example");
    assertThat(new DiagramRenderer(getCommandModel(sourcePath)).getMuleSourcePath())
        .as("Resolved mule configuration path")
        .isEqualTo(Paths.get(sourcePath.toString(), "src/main/app"));
    logs.assertContains(
        "Found standard Mule 3 source structure 'src/main/app'. Source is a Mule-3 project.");
  }
}
