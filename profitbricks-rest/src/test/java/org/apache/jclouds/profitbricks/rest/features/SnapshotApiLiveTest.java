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
package org.apache.jclouds.profitbricks.rest.features;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.net.URI;
import java.util.List;
import org.apache.jclouds.profitbricks.rest.domain.DataCenter;
import org.apache.jclouds.profitbricks.rest.domain.ProvisioningState;
import org.apache.jclouds.profitbricks.rest.domain.Snapshot;
import org.apache.jclouds.profitbricks.rest.domain.State;
import org.apache.jclouds.profitbricks.rest.domain.Volume;
import org.apache.jclouds.profitbricks.rest.ids.VolumeRef;
import org.apache.jclouds.profitbricks.rest.internal.BaseProfitBricksLiveTest;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "live", testName = "SnapshotApiLiveTest")
public class SnapshotApiLiveTest extends BaseProfitBricksLiveTest {

   private DataCenter dataCenter;
   private Volume testVolume;
   private Snapshot testSnapshot;

   @BeforeClass
   public void setupTest() {
      dataCenter = createDataCenter();
      assertDataCenterAvailable(dataCenter);

      testVolume = createVolume(dataCenter);
      assertNotNull(testVolume);
      assertVolumeAvailable(testVolume);

      testSnapshot = api.volumeApi().createSnapshot(
              Volume.Request.createSnapshotBuilder()
              .dataCenterId(testVolume.dataCenterId())
              .volumeId(testVolume.id())
              .name("test-snapshot")
              .description("snapshot desc...")
              .build());

      assertRequestCompleted(testSnapshot);
      assertSnapshotAvailable(testSnapshot);
   }

   @AfterClass(alwaysRun = true)
   public void teardownTest() {
      if (dataCenter != null) {
         deleteDataCenter(dataCenter.id());
      }
   }

   @Test
   public void testList() {
      List<Snapshot> snapshots = snapshotApi().list();

      assertNotNull(snapshots);
      assertFalse(snapshots.isEmpty());

      assertTrue(Iterables.any(snapshots, new Predicate<Snapshot>() {
         @Override
         public boolean apply(Snapshot input) {
            return input.id().equals(testSnapshot.id());
         }
      }));
   }

   @Test
   public void testGetSnapshot() {
      Snapshot snapshot = snapshotApi().get(testSnapshot.id());

      assertNotNull(snapshot);
      assertEquals(snapshot.id(), testSnapshot.id());
      assertEquals(snapshot.properties().name(), "test-snapshot");
   }

   @Test(dependsOnMethods = "testGetSnapshot")
   public void testUpdateSnapshot() {
      Snapshot snapshot = snapshotApi().update(
              Snapshot.Request.updatingBuilder()
              .id(testSnapshot.id())
              .name("test-snapshot new name")
              .build()
      );

      assertRequestCompleted(snapshot);
      assertVolumeAvailable(testVolume);
      assertEquals(snapshot.properties().name(), "test-snapshot new name");
   }

   @Test(dependsOnMethods = "testUpdateSnapshot")
   public void testDeleteSnapshot() {
      URI uri = api.volumeApi().deleteVolume(testVolume.dataCenterId(), testVolume.id());
      assertRequestCompleted(uri);
      assertVolumeRemoved(testVolume);
      snapshotApi().delete(testSnapshot.id());
      assertSnapshotRemoved(testSnapshot);
   }

   private SnapshotApi snapshotApi() {
      return api.snapshotApi();
   }

   private void assertVolumeAvailable(Volume volume) {
      assertPredicate(new Predicate<VolumeRef>() {
         @Override
         public boolean apply(VolumeRef volumeRef) {
            Volume volume = api.volumeApi().getVolume(volumeRef.dataCenterId(), volumeRef.volumeId());

            if (volume == null || volume.metadata() == null) {
               return false;
            }

            return volume.metadata().state() == State.AVAILABLE;
         }
      }, VolumeRef.create(volume.dataCenterId(), volume.id()));
   }

   private void assertVolumeRemoved(Volume volume) {
      assertPredicate(new Predicate<VolumeRef>() {
         @Override
         public boolean apply(VolumeRef volumeRef) {
            return api.volumeApi().getVolume(volumeRef.dataCenterId(), volumeRef.volumeId()) == null;
         }
      }, VolumeRef.create(volume.dataCenterId(), volume.id()));
   }

   private void assertSnapshotAvailable(Snapshot snapshot) {
      assertPredicate(new Predicate<String>() {
         @Override
         public boolean apply(String id) {
            Snapshot snapshot = api.snapshotApi().get(id);

            if (snapshot == null || snapshot.metadata() == null) {
               return false;
            }

            return snapshot.metadata().state() == ProvisioningState.AVAILABLE;
         }
      }, snapshot.id());
   }

   private void assertSnapshotRemoved(Snapshot snapshot) {
      assertPredicate(new Predicate<String>() {
         @Override
         public boolean apply(String id) {
            return snapshotApi().get(id) == null;
         }
      }, snapshot.id());
   }

}
