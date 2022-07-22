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

package com.intel.analytics.bigdl.ppml.fl

import com.intel.analytics.bigdl.ppml.fl.algorithms.PSI
import com.intel.analytics.bigdl.ppml.fl.utils.{PortUtils, TestUtils}
import org.apache.logging.log4j.LogManager
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.TimeoutException

class PSISpec extends FLSpec {
  "PSI get salt" should "work" in {
    val flServer = new FLServer()
    flServer.setPort(port)
    flServer.build()
    flServer.start()
    FLContext.initFLContext("1", target)
    val pSI = new PSI()
    val salt = pSI.getSalt()
    flServer.stop()
    TestUtils.conditionFailTest(salt != null, "Get salt failed.")
  }
  "PSI upload set" should "work" in {
    val flServer = new FLServer()
    flServer.setPort(port)
    flServer.build()
    flServer.start()
    FLContext.initFLContext("1", target)
    val pSI = new PSI()
    val set = List("key1", "key2")
    val salt = pSI.getSalt()
    pSI.uploadSet(set.asJava, salt)
    flServer.stop()

    TestUtils.conditionFailTest(salt != null, "Get salt failed.")
  }
  "PSI download intersection" should "work" in {
    val flServer = new FLServer()
    flServer.setPort(port)
    flServer.setClientNum(2)
    flServer.build()
    flServer.start()
    FLContext.initFLContext("1", target)
    val pSI1 = new PSI()
    val pSI2 = new PSI()
    val set1 = List("key1", "key2")
    val set2 = List("key2", "key3")
    val salt1 = pSI1.getSalt()
    val salt2 = pSI2.getSalt()
    pSI1.uploadSet(set1.asJava, salt1)
    pSI2.uploadSet(set2.asJava, salt2)
    val intersection = pSI1.downloadIntersection()
    flServer.stop()
    TestUtils.conditionFailTest(intersection.size() == 1, "Intersection number is wrong.")
  }
  "PSI download null intersection" should "work" in {
    val flServer = new FLServer()
    flServer.setPort(port)
    flServer.build()
    flServer.start()
    FLContext.initFLContext("1", target)
    val pSI1 = new PSI()
    val set1 = List("key1", "key2")
    val salt1 = pSI1.getSalt()
    pSI1.uploadSet(set1.asJava, salt1)
    try {
      val intersection = pSI1.downloadIntersection(maxTry = 2)
    } catch {
      case _: TimeoutException => println("Test pass")
      case _ => throw new Error("Test fail")
    } finally {
      flServer.stop()
    }

  }
}
