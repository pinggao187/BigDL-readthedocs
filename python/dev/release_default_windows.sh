#!/usr/bin/env bash

#
# Copyright 2016 The BigDL Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This is the default script with maven parameters to release all the bigdl sub-packages
# built on top of Spark 2.4.6 for linux.

set -e
RUN_SCRIPT_DIR=$(cd $(dirname $0) ; pwd)
echo $RUN_SCRIPT_DIR
BIGDL_DIR="$(cd ${RUN_SCRIPT_DIR}/../..; pwd)"
echo $BIGDL_DIR

if (( $# < 3)); then
  echo "Usage: release_default_windows.sh version quick_build upload mvn_parameters"
  echo "Notice: This script only build bigdl-nano for windows"
  echo "Usage example: bash release_default_windows.sh default false true"
  echo "Usage example: bash release_default_windows.sh 0.14.0.dev1 false false"
  echo "Usage example: bash release_default_windows.sh 0.14.0.dev1 false false -Ddata-store-url=.."
  exit -1
fi

version=$1
quick=$2
upload=$3
profiles=${*:4}

NANO_SCRIPT_DIR="$(cd ${BIGDL_DIR}/python/nano/dev; pwd)"
echo $NANO_SCRIPT_DIR
bash ${NANO_SCRIPT_DIR}/release_default_windows.sh ${version} ${upload}


# TODO: may need to upload all whls in the very end at the same time in case any build fails?
