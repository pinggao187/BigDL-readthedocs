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

package com.intel.analytics.bigdl.ppml.crypto

import com.intel.analytics.bigdl.ppml.crypto.CryptoCodec.CryptoDecompressStream
import org.apache.hadoop.conf.{Configurable, Configuration}
import org.apache.hadoop.io.compress._
import org.apache.hadoop.io.compress.zlib.ZlibFactory

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CryptoCodec
  extends Configurable with CompressionCodec with DirectDecompressionCodec {
  protected var conf: Configuration = null

  override def setConf(conf: Configuration): Unit = {
    this.conf = conf
  }

  override def getConf: Configuration = conf

  @throws[IOException]
  override def createOutputStream(out: OutputStream): CompressionOutputStream = {
    // TODO: CompressionCodec.Util.createOutputStreamWithCodecPool(this, conf, out)
    val compressor = this.createCompressor()
    this.createOutputStream(out, compressor)
  }

  @throws[IOException]
  override def createOutputStream(out: OutputStream, compressor: Compressor): CompressorStream = {
    new CompressorStream(out, compressor, conf.getInt("io.file.buffer.size", 4 * 1024))
  }

  override def getCompressorType: Class[_ <: Compressor] = {
    BigDLEncryptCompressor.getCompressorType(conf)
//    ZlibFactory.getZlibCompressorType(conf)
  }

  override def createCompressor: Compressor = {
    BigDLEncryptCompressor(conf)
//    ZlibFactory.getZlibCompressor(conf)
  }

  @throws[IOException]
  override def createInputStream(in: InputStream): CompressionInputStream = {
    // TODO:  CompressionCodec.Util.createInputStreamWithCodecPool(this, conf, in)
//    val decompressor = this.createDecompressor()
//    this.createInputStream(in, decompressor)
    CryptoDecompressStream(conf, in)
  }

  @throws[IOException]
  override def createInputStream(
        in: InputStream,
        decompressor: Decompressor): CryptoDecompressStream = {
    //    new DecompressorStream(in, decompressor, conf.getInt("io.file.buffer.size", 4 * 1024))
    CryptoDecompressStream(conf, in)
  }

  override def getDecompressorType: Class[_ <: Decompressor] = {
    ZlibFactory.getZlibDecompressorType(conf)
    null
  }

  override def createDecompressor: Decompressor = {
    ZlibFactory.getZlibDecompressor(conf)
    null
  }

  /**
   * {@inheritDoc }
   */
  override def createDirectDecompressor: DirectDecompressor = {
    ZlibFactory.getZlibDirectDecompressor(conf)
    null
  }

  override def getDefaultExtension: String = {
    CryptoCodec.getDefaultExtension()
  }
}

object CryptoCodec {
  def getDefaultExtension(): String = {
    ".cbc"
  }

  class CryptoDecompressStream(
        in: InputStream,
        bufferSize: Int,
        cryptoMode: CryptoMode,
        dataKeyPlaintext: String) extends DecompressorStream(in) {
    buffer = new Array[Byte](bufferSize)
    val bigdlEncrypt = new BigDLEncrypt()
    bigdlEncrypt.init(cryptoMode, DECRYPT, dataKeyPlaintext)
    var headerVerified = false

    override def decompress(b: Array[Byte], off: Int, len: Int): Int = {
      if (!headerVerified) {
        bigdlEncrypt.verifyHeader(in)
        headerVerified = true
      }

      if (in.available() == 0) { // apparently the previous end-of-stream was also end-of-file:
        // return success, as if we had never called getCompressedData()
        eof = true
        return -1
      }
      val decompressed = bigdlEncrypt.decryptPart(in, buffer)

      decompressed.copyToArray(b, 0)
      decompressed.length
    }
  }

  object CryptoDecompressStream{
    def apply(conf: Configuration, in: InputStream): CryptoDecompressStream = {
      val dataKey = conf.get("bigdl.kms.data.key")
      val cryptoMode = CryptoMode.parse(conf.get("bigdl.kms.crypto.mode",
        AES_CBC_PKCS5PADDING.encryptionAlgorithm))
      val bufferSize = conf.getInt("io.file.buffer.size", 4 * 1024)
      // TODO
      new CryptoDecompressStream(in, bufferSize, cryptoMode, dataKey)
    }
  }

}
