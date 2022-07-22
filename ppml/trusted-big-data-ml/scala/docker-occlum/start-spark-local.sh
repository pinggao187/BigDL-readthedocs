# Clean up old container
sudo docker rm -f bigdl-ppml-trusted-big-data-ml-scala-occlum

# Run new command in container
sudo docker run -it \
	--net=host \
	--name=bigdl-ppml-trusted-big-data-ml-scala-occlum \
	--cpuset-cpus 10-14 \
	--device=/dev/sgx/enclave \
	--device=/dev/sgx/provision \
	-v /var/run/aesmd:/var/run/aesmd \
	-v data:/opt/occlum_spark/data \
	-e LOCAL_IP=$LOCAL_IP \
	-e SGX_MEM_SIZE=24GB \
	-e SGX_THREAD=512 \
	-e SGX_HEAP=512MB \
	-e SGX_KERNEL_HEAP=1GB \
	-e PCCS_URL=$PCCS_URL \
	-e ATTESTATION=false \
	-e ATTESTATION_SERVER_IP=$ATTESTATION_SERVER_IP \
	-e ATTESTATION_SERVER_PORT=$ATTESTATION_SERVER_PORT \
	-e SGX_LOG_LEVEL=off \
	intelanalytics/bigdl-ppml-trusted-big-data-ml-scala-occlum:2.1.0-SNAPSHOT \
	bash /opt/run_spark_on_occlum_glibc.sh $1
