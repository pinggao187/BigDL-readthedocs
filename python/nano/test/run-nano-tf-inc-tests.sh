#!/bin/bash

export ANALYTICS_ZOO_ROOT=${ANALYTICS_ZOO_ROOT}
export NANO_HOME=${ANALYTICS_ZOO_ROOT}/python/nano/src
export INC_TF_NANO_TEST_DIR=${ANALYTICS_ZOO_ROOT}/python/nano/test/inc/tf

set -e
echo "# Start testing"
start=$(date "+%s")
python -m pytest -s ${INC_TF_NANO_TEST_DIR}

now=$(date "+%s")
time=$((now-start))

echo "Bigdl-nano tests finished"
echo "Time used:$time seconds"

