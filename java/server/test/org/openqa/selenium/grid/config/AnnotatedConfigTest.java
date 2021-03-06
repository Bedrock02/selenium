// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.config;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AnnotatedConfigTest {

  @Test
  public void shouldAllowConfigsToBeAnnotated() {

    class WithAnnotations {
      @ConfigValue(section = "cheese", name = "type")
      private final String cheese = "brie";
    }

    WithAnnotations obj = new WithAnnotations();
    Config config = new AnnotatedConfig(obj);
    assertEquals(Optional.of("brie"), config.get("cheese", "type"));

  }

  @Test
  public void shouldAllowFieldsToBeSomethingOtherThanStrings() {
    class WithTypes {
      @ConfigValue(section = "types", name = "bool")
      private final boolean boolField = true;
      @ConfigValue(section = "types", name = "int")
      private final int intField = 42;
    }

    Config config = new AnnotatedConfig(new WithTypes());
    assertEquals(Optional.of(true), config.getBool("types", "bool"));
    assertEquals(Optional.of(42), config.getInt("types", "int"));
  }

  @Test(expected = ConfigException.class)
  public void shouldNotAllowCollectionTypeFieldsToBeAnnotated() {
    class WithBadAnnotation {
      @ConfigValue(section = "bad", name = "collection")
      private final Set<String> cheeses = ImmutableSet.of("cheddar", "gouda");
    }

    new AnnotatedConfig(new WithBadAnnotation());
  }

  @Test(expected = ConfigException.class)
  public void shouldNotAllowMapTypeFieldsToBeAnnotated() {
    class WithBadAnnotation {
      @ConfigValue(section = "bad", name = "map")
      private final Map<String, String> cheeses = ImmutableMap.of("peas", "sausage");
    }

    new AnnotatedConfig(new WithBadAnnotation());
  }

  @Test
  public void shouldWalkInheritanceHierarchy() {
    class Parent {
      @ConfigValue(section = "cheese", name = "type")
      private final String value = "cheddar";
    }

    class Child extends Parent {
    }

    Config config = new AnnotatedConfig(new Child());

    assertEquals(Optional.of("cheddar"), config.get("cheese", "type"));
  }

  @Test
  public void configValuesFromChildClassesAreMoreImportant() {
    class Parent {
      @ConfigValue(section = "cheese", name = "type")
      private final String value = "cheddar";
    }

    class Child extends Parent {
      @ConfigValue(section = "cheese", name = "type")
      private final String cheese = "gorgonzola";
    }

    Config config = new AnnotatedConfig(new Child());

    assertEquals(Optional.of("gorgonzola"), config.get("cheese", "type"));

  }

}