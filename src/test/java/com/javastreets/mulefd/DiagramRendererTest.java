package com.javastreets.mulefd;

import static com.javastreets.mulefd.DiagramRendererTestUtil.getCommandModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.javastreets.mulefd.cli.CommandModel;
import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.drawings.DrawingContext;
import com.javastreets.mulefd.model.Component;

import io.github.netmikey.logunit.api.LogCapturer;

class DiagramRendererTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DiagramRenderer.class, Level.DEBUG);

  @TempDir
  File tempDir;

  @Test
  @DisplayName("Empty source directory rendering return false")
  void emptySourceDirRendering() {
    DiagramRenderer renderer = Mockito.spy(new DiagramRenderer(getCommandModel(tempDir.toPath())));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(any(DrawingContext.class));
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<DrawingContext> captor = ArgumentCaptor.forClass(DrawingContext.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue().getComponents()).isEmpty();
  }


  @Test
  @DisplayName("Skips rendering non-mule file")
  void skipsNonMuleFileRendering() {
    Path sourcePath = Paths.get("src/test/resources/renderer/non-mule");
    DiagramRenderer renderer = Mockito.spy(new DiagramRenderer(getCommandModel(sourcePath)));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(any(DrawingContext.class));
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<DrawingContext> captor = ArgumentCaptor.forClass(DrawingContext.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue().getComponents()).isEmpty();
    logs.assertContains(
        "Not a mule configuration file: " + Paths.get(sourcePath.toString(), "non-mule-file.xml"));
  }

  @Test
  @DisplayName("Source directory rendering with one config")
  void singleFileRendering() {
    DiagramRenderer renderer = Mockito
        .spy(new DiagramRenderer(getCommandModel(Paths.get("src/test/resources/renderer/single"))));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(any(DrawingContext.class));
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<DrawingContext> captor = ArgumentCaptor.forClass(DrawingContext.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue().getComponents()).as("Flow container list").hasSize(1)
        .extracting(Component::getType, Component::getName)
        .containsExactly(tuple("flow", "test-hello-appFlow"));
  }

  @Test
  @DisplayName("Create drawing context from command model")
  void toDrawingContext() {
    CommandModel commandModel = getCommandModel(tempDir.toPath());
    commandModel.setFlowName("test-flow");
    assertThat(new DiagramRenderer(commandModel).drawingContext(commandModel))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile,
            DrawingContext::getFlowName, DrawingContext::isGenerateSingles)
        .containsExactly(DiagramType.GRAPH,
            new File(commandModel.getTargetPath().toFile(), commandModel.getOutputFilename()),
            "test-flow", false);
  }

  @Test
  @DisplayName("Create drawing context from command model with single generation as true")
  void toDrawingContextForSingles() {
    CommandModel commandModel = getCommandModel(tempDir.toPath());
    commandModel.setFlowName("test-flow");
    commandModel.setGenerateSingles(true);
    assertThat(new DiagramRenderer(commandModel).drawingContext(commandModel))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile,
            DrawingContext::getFlowName, DrawingContext::isGenerateSingles)
        .containsExactly(DiagramType.GRAPH,
            new File(commandModel.getTargetPath().toFile(), commandModel.getOutputFilename()),
            "test-flow", true);
  }

  @Test
  @DisplayName("Prepare components from csv file")
  void prepareKnownComponents() {
    assertThat(new DiagramRenderer(getCommandModel(tempDir.toPath())).prepareKnownComponents())
        .isNotEmpty();
  }

  @Test
  @DisplayName("Prepare components from mulefd csv file")
  void prepareKnownComponentsWithMulefdFile() throws IOException {
    Files.copy(Paths.get("src/test/resources/mulefd-components.csv"),
        tempDir.toPath().resolve("mulefd-components.csv"), StandardCopyOption.REPLACE_EXISTING);
    assertThat(new DiagramRenderer(getCommandModel(tempDir.toPath())).prepareKnownComponents())
        .isNotEmpty().containsKey("kafka:message-listener");
    Files.deleteIfExists(Paths.get("./mulefd-components.csv"));
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
