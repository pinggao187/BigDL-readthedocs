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

package com.intel.analytics.bigdl.ppml.examples

import com.intel.analytics.bigdl.ppml.kms.{AzureKeyManagementService, EHSMKeyManagementService, KMS_CONVENTION, SimpleKeyManagementService}
import com.intel.analytics.bigdl.ppml.crypto.{AES_CBC_PKCS5PADDING, BigDLEncrypt, ENCRYPT, DECRYPT, EncryptRuntimeException}
import com.intel.analytics.bigdl.ppml.utils.Supportive
import com.intel.analytics.bigdl.ppml.utils.EncryptIOArguments
import org.slf4j.LoggerFactory

object Encrypt extends App with Supportive{
  val logger = LoggerFactory.getLogger(getClass)

  val arguments = timing("parse arguments") {
    EncryptIOArguments.parser.parse(args, EncryptIOArguments()) match {
      case Some(arguments) => logger.info(s"starting with $arguments"); arguments
      case None => EncryptIOArguments.parser.failure("miss args, please see the usage info"); null
    }
  }

  val kms = arguments.kmsType match {
    case KMS_CONVENTION.MODE_EHSM_KMS =>
      new EHSMKeyManagementService(arguments.kmsServerIP, arguments.kmsServerPort,
        arguments.ehsmAPPID, arguments.ehsmAPPKEY)
    case KMS_CONVENTION.MODE_SIMPLE_KMS =>
      SimpleKeyManagementService(arguments.simpleAPPID, arguments.simpleAPPKEY)
    case KMS_CONVENTION.MODE_AZURE_KMS =>
      new AzureKeyManagementService(arguments.keyVaultName, arguments.managedIdentityClientId)
    case _ =>
      throw new EncryptRuntimeException("Wrong kms type")
  }

  val primaryKeyPath = arguments.primaryKeyPath
  val dataKeyPath = arguments.dataKeyPath
  val encryptedFilePath = arguments.inputPath + ".encrypted"

  logger.info(s"$arguments.inputPath will be encrypted and saved at $encryptedFilePath")

  logger.info(s"Primary key will be saved at $primaryKeyPath," +
    s" and data key will be saved at $dataKeyPath")

  val dataKeyPlaintext = kms.retrieveDataKeyPlainText(primaryKeyPath, dataKeyPath)

  logger.info("The cryptos initializing for encryption...")
  val encrypt = new BigDLEncrypt()
  encrypt.init(AES_CBC_PKCS5PADDING, ENCRYPT, dataKeyPlaintext)

  logger.info("Start to encrypt the file.")
  encrypt.doFinal(arguments.inputPath, encryptedFilePath)
  logger.info("Finish encryption successfully!")

}
