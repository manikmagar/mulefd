package com.github.manikmagar.mule.flowdiagrams.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationsTest {

  @Test
  @DisplayName("Returns true")
  void requireTrue() {
    assertThat(Validations.requireTrue(true, "flag must be true")).isTrue();
  }

  @Test
  @DisplayName("Throws Exception when value is not true")
  void requireTrueThrowsException() {
    assertThat(catchThrowableOfType(() -> Validations.requireTrue(false, "flag must be true"),
        IllegalArgumentException.class)).isNotNull().hasMessage("flag must be true");
  }

  @Test
  @DisplayName("Returns true without message")
  void testRequireTrue() {
    assertThat(Validations.requireTrue(true)).isTrue();
  }

  @Test
  @DisplayName("Returns false")
  void requireFalse() {
    assertThat(Validations.requireFalse(false, "flag must be false")).isFalse();
  }

  @Test
  @DisplayName("Throws Exception when value is not false")
  void requireFalseThrowsException() {
    assertThat(catchThrowableOfType(() -> Validations.requireFalse(true, "flag must be false"),
        IllegalArgumentException.class)).isNotNull().hasMessage("flag must be false");
  }

  @Test
  @DisplayName("Returns false without message")
  void testRequireFalse() {
    assertThat(Validations.requireFalse(false)).isFalse();
  }
}
