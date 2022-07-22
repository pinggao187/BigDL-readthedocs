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

package com.intel.analytics.bigdl.ppml.fl.algorithms

import com.intel.analytics.bigdl.ppml.fl.FLContext

import java.util
import java.util.concurrent.TimeoutException
import com.intel.analytics.bigdl.ppml.fl.psi.HashingUtils
import com.intel.analytics.bigdl.ppml.fl.utils.FLClientClosable
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.DataFrame

import scala.collection.JavaConverters._
import scala.util.control.Breaks._

class PSI() extends FLClientClosable {
  private var hashIdToId: Map[String, String] = Map[String, String]()
  val logger = LogManager.getLogger(getClass)

  def getSalt(secureCode: String = ""): String = {
    flClient.psiStub.getSalt(secureCode)
  }

  def uploadSet(ids: util.List[String], salt: String): Unit = {
    val hashedIdArray = HashingUtils.parallelToSHAHexString(ids, salt)
    hashIdToId = hashedIdArray.asScala.zip(ids.asScala).toMap
    flClient.psiStub.uploadSet(hashedIdArray)
  }


  def downloadIntersection(maxTry: Int = 100, retry: Long = 3000): util.List[String] = {
    var intersectionHashed: util.List[String] = null
    breakable {
      for (i <- 0 until maxTry) {
        intersectionHashed = flClient.psiStub.downloadIntersection
        if (intersectionHashed == null) {
          if (i == maxTry - 1) {
            throw new TimeoutException("Max retry reached, could not get intersection, exited.")
          }
          logger.info(s"Got empty intersection, retry in $retry ms")
          Thread.sleep(retry)
        }
        else {
          logger.info("Intersection successful. Intersection's size is "
            + intersectionHashed.size + ".")
          break
        }
      }
    }
    intersectionHashed.asScala.map(h => hashIdToId(h)).toList.asJava
  }
  def getIntersection(ids: util.List[String],
                      maxTry: Int = 100,
                      retry: Long = 3000): util.List[String] = {
    val salt = getSalt()
    uploadSet(ids, salt)
    downloadIntersection(maxTry, retry)
  }

  def getIntersectionDataFrame(df: DataFrame,
                               rowKeyName: String,
                               intersection: util.List[String]): DataFrame = {
    val intersectionSet = intersection.asScala.toSet
    val dataSet = df.filter(r => intersectionSet.contains(r.getAs[String](rowKeyName)))
    dataSet
  }
  def uploadSetAndDownloadIntersectionDataFrame(df: DataFrame,
                                                salt: String,
                                                rowKeyName: String = "ID",
                                                maxTry: Int = 100,
                                                retry: Long = 3000): DataFrame = {
    val spark = FLContext.getSparkSession()
    import spark.implicits._
    val ids = df.select(rowKeyName).as[String].collect().toList.asJava
    uploadSet(ids, salt)
    val intersection = downloadIntersection(maxTry, retry)
    getIntersectionDataFrame(df, rowKeyName, intersection)
  }
}
