/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.ppml.fl.utils

import com.intel.analytics.bigdl.dllib.utils.Engine
import com.intel.analytics.bigdl.ppml.fl.FLClient
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * [[FlContextForTest]] is for test only, an instance of this could behave the same with
 * the singleton class FlContext. The code of this should be copied from FlContext.
 */
class FlContextForTest {
  var flClient: FLClient = null
  var sparkSession: SparkSession = null
  def initFLContext(clientId: String, target: String = null): Unit = {
    createSparkSession()
    Engine.init
    this.synchronized {
      if (flClient == null) {
        this.synchronized {
          flClient = new FLClient()
          flClient.setClientId(clientId)
          if (target != null) {
            flClient.setTarget(target)
          }
          flClient.build()
        }
      }
    }
  }
  def getClient(): FLClient = {
    flClient
  }
  def getSparkSession(): SparkSession = {
    if (sparkSession == null) {
      createSparkSession()
    }
    sparkSession
  }
  def createSparkSession(): Unit = {
    this.synchronized {
      if (sparkSession == null) {
        val conf = new SparkConf().setMaster("local[*]")
        sparkSession = SparkSession
          .builder()
          .config(conf)
          .getOrCreate()
      }
    }
  }
}
