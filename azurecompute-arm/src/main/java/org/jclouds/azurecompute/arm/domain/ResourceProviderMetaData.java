/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.azurecompute.arm.domain;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import org.jclouds.json.SerializedNames;

import java.util.List;

@AutoValue
public abstract class ResourceProviderMetaData {

   public abstract String resourceType();

   public abstract List<String> locations();

   public abstract List<String> apiVersions();

   @SerializedNames({"resourceType", "locations", "apiVersions"})
   public static ResourceProviderMetaData create(final String resourceType, final List<String> locations, final List<String> apiVersions) {
      ResourceProviderMetaData.Builder builder = ResourceProviderMetaData.builder()
              .resourceType(resourceType)
              .locations(locations == null ? ImmutableList.<String>of() : ImmutableList.copyOf(locations))
              .apiVersions(apiVersions == null ? ImmutableList.<String>of() : ImmutableList.copyOf(apiVersions));

      return builder.build();
   }
   
   public abstract Builder toBuilder();

   public static Builder builder() {
      return new AutoValue_ResourceProviderMetaData.Builder();
   }

   @AutoValue.Builder
   public abstract static class Builder {
      public abstract Builder resourceType(String resourceType);

      public abstract Builder locations(List<String> locations);

      public abstract Builder apiVersions(List<String> apiVersions);

      abstract List<String> locations();

      abstract List<String> apiVersions();

      abstract ResourceProviderMetaData autoBuild();

      public ResourceProviderMetaData build() {
         locations(locations() != null ? ImmutableList.copyOf(locations()) : ImmutableList.<String>of());
         apiVersions(apiVersions() != null ? ImmutableList.copyOf(apiVersions()) : ImmutableList.<String>of());
         return autoBuild();
      }
   }
}
