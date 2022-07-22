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

export FTP_URI=$FTP_URI


set -e
mkdir -p result
echo "#1 start example test for dien preprocessing"
#timer
start=$(date "+%s")
if [ -f data/test.json ]; then
  echo "data/test.json already exists"
else
  wget -nv $FTP_URI/analytics-zoo-data/test.json -P data
fi
if [ -f data/test ]; then
  echo "data/test already exists"
else
  wget -nv $FTP_URI/analytics-zoo-data/test -P data
fi

python ../../example/dien/dien_preprocessing.py \
    --executor_cores 6 \
    --executor_memory 50g \
    --input_meta ./data/test \
    --input_transaction ./data/test.json \
    --output ./result/

now=$(date "+%s")
time1=$((now - start))

echo "#2 start example test for dlrm preprocessing"
#timer
start=$(date "+%s")
if [ -d data/day_0.parquet ]; then
  echo "data/day_0.parquet already exists"
else
  wget -nv $FTP_URI/analytics-zoo-data/day0.tar.gz -P data
  tar -xvzf data/day0.tar.gz -C data
fi

python ../../example/dlrm/dlrm_preprocessing.py \
    --cores 6 \
    --memory 50g \
    --days 0-0 \
    --input_folder ./data \
    --output_folder ./result \
    --frequency_limit 15
now=$(date "+%s")
time2=$((now - start))

echo "#3 start example test for wnd preprocessing"
#timer
start=$(date "+%s")
if [ -f data/dac_sample.txt ]; then
  echo "data/dac_sample.txt already exists"
else
  wget -nv $FTP_URI/analytics-zoo-data/dac_sample.tar.gz -P data
  tar -xvzf data/dac_sample.tar.gz -C data
fi
python ../../example/wnd/csv_to_parquet.py \
        --input ./data/dac_sample.txt \
        --output ./data/day_0.parquet
cp -r ./data/day_0.parquet ./data/day_1.parquet
python ../../example/wnd/wnd_preprocessing.py \
    --executor_cores 6 \
    --executor_memory 50g \
    --days 0-1 \
    --input_folder ./data \
    --output_folder ./result \
    --frequency_limit 15 \
    --cross_sizes 10000,10000
now=$(date "+%s")
time3=$((now - start))


if [ -d data/recsys_sample ]; then
  echo "data/recsys_sample already exists"
else
  wget -nv $FTP_URI/analytics-zoo-data/recsys_sample.tar.gz -P data
  tar -xvzf data/recsys_sample.tar.gz -C data
fi
echo "#4 start example test for wnd recsys train data converting"
mkdir -p data/recsys_sample/spark_parquet
#timer
start=$(date "+%s")
python ../../example/wnd/train/convert_train.py \
    --input_folder ./data/recsys_sample/raw_parquet \
    --output_folder ./data/recsys_sample/spark_parquet
now=$(date "+%s")
time4=$((now - start))

echo "#5 start example test for wnd recsys test data converting"
mkdir data/recsys_sample/test_spark_parquet
#timer
start=$(date "+%s")
python ../../example/wnd/train/valid_to_parquet.py \
    --executor_cores 6 \
    --executor_memory 50g \
    --input_file ./data/recsys_sample/valid \
    --output_folder ./data/recsys_sample/test_spark_parquet
now=$(date "+%s")
time5=$((now - start))


echo "#6 start example test for wnd recsys train/test data preprocessing"
#timer
start=$(date "+%s")
python ../../example/wnd/train/wnd_preprocess_recsys.py \
    --executor_cores 6 \
    --executor_memory 50g \
    --train_files 1-1 \
    --input_train_folder ./data/recsys_sample/spark_parquet \
    --input_test_folder ./data/recsys_sample/test_spark_parquet \
    --output_folder ./result
now=$(date "+%s")
time6=$((now - start))


rm -rf data
rm -rf result

echo "#1 dien preprocessing time used: $time1 seconds"
echo "#2 dlrm preprocessing time used: $time2 seconds"
echo "#3 wnd preprocessing time used: $time3 seconds"
echo "#4 wnd recsys train data converting time used: $time4 seconds"
echo "#5 wnd recsys test data converting time used: $time5 seconds"
echo "#6 wnd recsys train/test data preprocessing time used: $time6 seconds"
