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

set -e

$CONDA/bin/conda create -n benchmark-resnet-50 -y python==3.7.10 setuptools==58.0.4
$CONDA/bin/activate benchmark-resnet-50

bash $ANALYTICS_ZOO_ROOT/python/nano/dev/build_and_install.sh linux default false pytorch

cd $ANALYTICS_ZOO_ROOT/python/nano/benchmark/pytorch/

echo "Nano_Perf: Running PyTorch Baseline"
python pytorch-cat-dog.py --batch_size 32 --name "PyTorch Baseline"

source bigdl-nano-init
echo "Nano_Perf: Running Nano default"
python pytorch-cat-dog.py --batch_size 32 --name "Nano default env"
source bigdl-nano-unset-env

source bigdl-nano-init
echo "Nano_Perf: Running Nano default with ipex"
python pytorch-cat-dog.py --use_ipex true --batch_size 32 --name "Nano default env with ipex"
source bigdl-nano-unset-env

source bigdl-nano-init
echo "Nano_Perf: Running Nano default with ipex, nano data"
python pytorch-cat-dog.py --nano_data true --use_ipex true --batch_size 32 --name "Nano default env with ipex, nano data"
source bigdl-nano-unset-env

source bigdl-nano-init
echo "Nano_Perf: Running Nano default with ipex 4 processes"
python pytorch-cat-dog.py --nproc 4 --nano_data true --use_ipex true --batch_size 32 --name "Nano default env with ipex, nano data, 4 process"
source bigdl-nano-unset-env

source $CONDA/bin/deactivate
$CONDA/bin/conda remove -n benchmark-resnet-50 --all
