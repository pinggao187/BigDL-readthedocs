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

import com.intel.analytics.bigdl.dllib.feature.dataset.{LocalDataSet, MiniBatch}
import com.intel.analytics.bigdl.dllib.tensor.Tensor

import scala.collection.mutable.ArrayBuffer

object DataSetUtils {
  def localDataSetToArray(dataSet: LocalDataSet[MiniBatch[Float]]):
  (Array[Tensor[Float]], Array[Float]) = {
    val featureBuffer = new ArrayBuffer[Tensor[Float]]()
    val labelBuffer = new ArrayBuffer[Float]()
    var count = 0
    val data = dataSet.data(true)
    while (count < data.size) {
      val batch = data.next()
      featureBuffer.append(batch.getInput().toTensor[Float])
      labelBuffer.append(batch.getTarget().toTensor[Float].value())
    }
    (featureBuffer.toArray, labelBuffer.toArray)
  }
}
