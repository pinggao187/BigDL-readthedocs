#!/bin/bash

${SPARK_HOME}/bin/spark-submit \
    --master k8s://https://${kubernetes_master_url}:6443 \
    --deploy-mode cluster \
    --name spark-gbt-example \
    --class org.apache.spark.examples.ml.GBTExample \
    --conf spark.executor.instances=1 \
    --conf spark.rpc.netty.dispatcher.numThreads=32 \
    --conf spark.kubernetes.container.image=intelanalytics/bigdl-ppml-trusted-big-data-ml-scala-occlum:2.1.0-SNAPSHOT \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    --conf spark.kubernetes.executor.podNamePrefix="sparkgbt" \
    --conf spark.kubernetes.executor.deleteOnTermination=false \
    --conf spark.kubernetes.driver.podTemplateFile=./driver.yaml \
    --conf spark.kubernetes.executor.podTemplateFile=./executor.yaml \
    --conf spark.kubernetes.sgx.log.level=off \
    --jars local:/opt/spark/examples/jars/scopt_2.12-3.7.1.jar \
    local:/opt/spark/examples/jars/spark-examples_2.12-3.1.2.jar \
    /opt/spark/data/mllib/sample_lda_libsvm_data.txt --algo regression

