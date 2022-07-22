#!/bin/bash

set -e
echo "Start ray horovod tf example tests"

echo "#1 tf2 estimator resnet 50 example"
start=$(date "+%s")
python ${BIGDL_ROOT}/python/orca/example/learn/tf2/resnet/resnet-50-imagenet.py --use_dummy_data --benchmark --batch_size_per_worker 4
now=$(date "+%s")
time1=$((now-start))

echo "Ray example tests finished"

echo "Start yoloV3 tf2 example tests"

echo "#2 tf2 estimator yoloV3 example"
start=$(date "+%s")
if [ -f analytics-zoo-models/yolov3.weights ]; then
  echo "analytics-zoo-models/yolov3.weights already exists."
else
  wget -nv $FTP_URI/analytics-zoo-models/yolov3/yolov3.weights \
    -P analytics-zoo-models
fi

if [ -f analytics-zoo-data/voc2012.names ]; then
  echo "analytics-zoo-data/voc2012.names already exists."
else
  wget -nv $FTP_URI/analytics-zoo-data/yolov3/voc2012.names -P analytics-zoo-data
fi

if [ -f tmp/coco.names ]; then
  echo "tmp/coco.names already exists."
else
  wget -nv $FTP_URI/analytics-zoo-data/yolov3/coco.names -P tmp
fi

if [ -f tmp/VOCdevkit.zip ]; then
  echo "tmp/VOCdevkit.zip already exists."
else
  wget -nv $FTP_URI/analytics-zoo-data/yolov3/VOCdevkit.zip -P tmp
  unzip -q tmp/VOCdevkit.zip -d tmp/VOCdevkit
fi

if [ -f models/checkpoints.zip ]; then
  echo "models/checkpoints already exists."
else
  wget -nv $FTP_URI/analytics-zoo-models/yolov3/checkpoints.zip \
    -P models
  unzip -q models/checkpoints.zip -d models
fi

echo "yolov3 train"
python ${BIGDL_ROOT}/python/orca/example/learn/tf2/yolov3/yoloV3.py --data_dir ${BIGDL_ROOT}/python/orca/test/bigdl/orca/resources  --weights analytics-zoo-models/yolov3.weights --class_num 20 --names analytics-zoo-data/voc2012.names --data_year 2007 --split_name_train trainval --split_name_test trainval

echo "yolov3 predict"
python ${BIGDL_ROOT}/python/orca/example/learn/tf2/yolov3/predict.py --checkpoint models/checkpoints/yolov3.tf --names tmp/coco.names --class_num 80 --image tmp/VOCdevkit/VOCdevkit/VOC2007/JPEGImages/000005.jpg

now=$(date "+%s")
time2=$((now-start))

echo "#1 tf2 estimator resnet 50 time used:$time1 seconds"
echo "#2 tf2 estimator yolov3 time used:$time2 seconds"

