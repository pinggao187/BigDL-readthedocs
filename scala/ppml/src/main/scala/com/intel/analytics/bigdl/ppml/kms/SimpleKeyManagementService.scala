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

package com.intel.analytics.bigdl.ppml.kms

import com.intel.analytics.bigdl.dllib.utils.Log4Error

import scala.collection.mutable.HashMap
import scala.util.Random
import com.intel.analytics.bigdl.ppml.utils.KeyReaderWriter

class SimpleKeyManagementService protected(
      simpleAPPID: String,
      simpleAPPKEY: String) extends KeyManagementService {
  val enrollMap = new HashMap[String, String]
  val keyReaderWriter = new KeyReaderWriter
  var _appId: String = _
  var _appKey: String = _

  setAppIdAndKey(simpleAPPID, simpleAPPKEY)

  Log4Error.invalidInputError(simpleAPPID != "", s"simpleAPPID should not be empty string.")
  Log4Error.invalidInputError(simpleAPPKEY != "", s"simpleAPPKEY should not be empty string.")

  def retrievePrimaryKey(primaryKeySavePath: String): Unit = {
    timing("SimpleKeyManagementService retrievePrimaryKey") {
      Log4Error.invalidInputError(enrollMap.keySet.contains(_appId) &&
        enrollMap(_appId) == _appKey, "appid and appkey do not match!")
      Log4Error.invalidInputError(primaryKeySavePath != null && primaryKeySavePath != "",
        "primaryKeySavePath should be specified")
      val suffix = (1 to 4).map { x => Random.nextInt(10) }.mkString
      val encryptedPrimaryKey = _appId + suffix
      keyReaderWriter.writeKeyToFile(primaryKeySavePath, encryptedPrimaryKey)
    }
  }

  def retrieveDataKey(primaryKeyPath: String, dataKeySavePath: String): Unit = {
    timing("SimpleKeyManagementService retrieveDataKey") {
      Log4Error.invalidInputError(enrollMap.keySet.contains(_appId) &&
        enrollMap(_appId) == _appKey, "appid and appkey do not match!")
      Log4Error.invalidInputError(primaryKeyPath != null && primaryKeyPath != "",
        "primaryKeyPath should be specified")
      Log4Error.invalidInputError(dataKeySavePath != null && dataKeySavePath != "",
        "dataKeySavePath should be specified")
      val primaryKeyPlaintext = keyReaderWriter.readKeyFromFile(primaryKeyPath)
      Log4Error.invalidInputError(primaryKeyPlaintext.substring(0, 12) == _appId,
        "appid and primarykey should be matched!")
      val randVect = (1 to 16).map { x => Random.nextInt(10) }
      val dataKeyPlaintext = randVect.mkString
      var dataKeyCiphertext = ""
      for(i <- 0 until 16) {
        dataKeyCiphertext += '0' + ((primaryKeyPlaintext(i) - '0') +
          (dataKeyPlaintext(i) - '0')) % 10
      }
      keyReaderWriter.writeKeyToFile(dataKeySavePath, dataKeyCiphertext)
    }
  }

  def retrieveDataKeyPlainText(primaryKeyPath: String, dataKeyPath: String): String = {
    timing("SimpleKeyManagementService retrieveDataKeyPlaintext") {
      Log4Error.invalidInputError(enrollMap.keySet.contains(_appId) &&
        enrollMap(_appId) == _appKey, "appid and appkey do not match!")
      Log4Error.invalidInputError(primaryKeyPath != null && primaryKeyPath != "",
        "primaryKeyPath should be specified")
      Log4Error.invalidInputError(dataKeyPath != null && dataKeyPath != "",
        "dataKeyPath should be specified")
      val primaryKeyCiphertext = keyReaderWriter.readKeyFromFile(primaryKeyPath)
      Log4Error.invalidInputError(primaryKeyCiphertext.substring(0, 12) == _appId,
        "appid and primarykey should be matched!")
      val dataKeyCiphertext = keyReaderWriter.readKeyFromFile(dataKeyPath)
      var dataKeyPlaintext = ""
      for(i <- 0 until 16) {
        dataKeyPlaintext += '0' + ((dataKeyCiphertext(i) - '0') -
          (primaryKeyCiphertext(i) - '0') + 10) % 10
      }
      dataKeyPlaintext
    }
  }

  private def setAppIdAndKey(appId: String, appKey: String): Unit = {
    _appId = appId
    _appKey = appKey
    enrollMap(_appId) = _appKey
  }

}

object SimpleKeyManagementService {
  def apply(): SimpleKeyManagementService = {
    val appid = (1 to 12).map(x => Random.nextInt(10)).mkString
    val appkey = (1 to 12).map(x => Random.nextInt(10)).mkString
    new SimpleKeyManagementService(appid, appkey)
  }

  def apply(appID: String, appKey: String): SimpleKeyManagementService = {
    new SimpleKeyManagementService(appID, appKey)
  }
}
