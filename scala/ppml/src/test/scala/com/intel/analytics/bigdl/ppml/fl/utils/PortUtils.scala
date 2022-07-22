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

import java.io.IOException
import java.net.{DatagramSocket, ServerSocket}

object PortUtils {
  def findNextPortAvailable(startPort: Int, endPort: Int = -1): Int = {
    def isAvailable(port: Int): Boolean = {
      var ss: ServerSocket = null
      var ds: DatagramSocket = null
      try {
        ss = new ServerSocket(port)
        ds = new DatagramSocket(port)
        true
      }
        catch {
          case e: IOException =>
            false
        } finally {
          if (ss != null) ss.close()
          if (ds != null) ds.close()
        }
      }

    var portAvailable: Int = startPort
    while (!isAvailable(portAvailable)) {
      portAvailable += 1
      if (endPort != -1 && portAvailable > endPort) {
        throw new Exception(s"Can not find avaible port in range [$startPort, $endPort].")
      }
    }
    portAvailable
  }
}
