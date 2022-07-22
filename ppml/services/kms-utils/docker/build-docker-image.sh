# set -x
export HTTP_PROXY_HOST=your_http_proxy_host
export HTTP_PROXY_PORT=your_http_proxy_port
export HTTPS_PROXY_HOST=your_https_proxy_host
export HTTPS_PROXY_PORT=your_https_proxy_port
export SPARK_JAR_REPO_URL=your_spark_jar_repo_url
export ENROLL_IMAGE_VERSION=latest
export ENROLL_IMAGE_NAME=bigdl-ppml-e2e-enroll


sudo docker build \
    --no-cache=true \
    --build-arg http_proxy=http://${HTTP_PROXY_HOST}:${HTTP_PROXY_PORT} \
    --build-arg https_proxy=http://${HTTPS_PROXY_HOST}:${HTTPS_PROXY_PORT} \
    --build-arg HTTP_PROXY_HOST=${HTTP_PROXY_HOST} \
    --build-arg HTTP_PROXY_PORT=${HTTP_PROXY_PORT} \
    --build-arg HTTPS_PROXY_HOST=${HTTPS_PROXY_HOST} \
    --build-arg HTTPS_PROXY_PORT=${HTTPS_PROXY_PORT} \
    --build-arg SPARK_JAR_REPO_URL=${SPARK_JAR_REPO_URL} \
    -t $ENROLL_IMAGE_NAME:$ENROLL_IMAGE_VERSION -f ./Dockerfile .
