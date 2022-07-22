## 1. Build container image
```
# set the arguments inside the build script first
bash build-docker-image.sh
```

## 2. Run container

If image is ready, you can run the container and enroll by using `run-docker-container.sh` in order to get a appid and appkey pair like below:

```bash
export KMS_TYPE=an_optional_kms_type # KMS_TYPE can be (1) ehsm, (2) simple
export EHSM_KMS_IP=your_ehsm_kms_ip # if ehsm
export EHSM_KMS_PORT=your_ehsm_kms_port # if ehsm
export ENROLL_IMAGE_NAME=your_enroll_image_name_built
export ENROLL_CONTAINER_NAME=your_enroll_container_name_to_run
export PCCS_URL=your_pccs_url # format like https://x.x.x.x:xxxx/sgx/certification/v3/

sudo docker run -itd \
    --privileged \
    --net=host \
    --name=$ENROLL_CONTAINER_NAME \
    -v /dev/sgx/enclave:/dev/sgx/enclave \
    -v /dev/sgx/provision:/dev/sgx/provision \
    -v $local_data_folder_path:/home/data \
    -v $local_key_folder_path:/home/key \
    -e EHSM_KMS_IP=$EHSM_KMS_IP \ # optional
    -e EHSM_KMS_PORT=$EHSM_KMS_PORT \ # optional
    -e KMS_TYPE=$KMS_TYPE \
    -e PCCS_URL=$PCCS_URL
    $ENROLL_IMAGE_NAME bash
    
docker exec -i $ENROLL_CONTAINER_NAME bash -c "bash /home/entrypoint.sh enroll"
INFO [main.cpp(46) -> main]: ehsm-kms enroll app start.
INFO [main.cpp(86) -> main]: First handle:  send msg0 and get msg1.
INFO [main.cpp(99) -> main]: First handle success.
INFO [main.cpp(101) -> main]: Second handle:  send msg2 and get msg3.
INFO [main.cpp(118) -> main]: Second handle success.
INFO [main.cpp(120) -> main]: Third handle:  send att_result_msg and get ciphertext of the APP ID and API Key.

appid: d792478c-f590-4073-8ed6-2d15e714da78

apikey: bSMN3dAQGEwgx297Ff1H2umBzwzv6W34

INFO [main.cpp(155) -> main]: decrypt APP ID and API Key success.
INFO [main.cpp(156) -> main]: Third handle success.
INFO [main.cpp(159) -> main]: ehsm-kms enroll app end.

export kms_type=ehsm_or_simple_or_azure

# Generatekeys
docker exec -i $ENROLL_CONTAINER_NAME bash -c "bash /home/entrypoint.sh $kms_type generatekeys"

# Encrypt a single data file
docker exec -i $ENROLL_CONTAINER_NAME bash -c "bash /home/entrypoint.sh $kms_type encrypt $appid $appkey $primary_key_name_in_key_folder $data_key_name_in_key_folder $plaintext_data_file_name_in_data_shared_folder"

# Decrypt a single data file
docker exec -i $ENROLL_CONTAINER_NAME bash -c "bash /home/entrypoint.sh $kms_type decrypt $appid $appkey $primary_key_name_in_key_folder $data_key_name_in_key_folder $encrypted_data_file_name_in_data_shared_folder"

# SpliteAndEncrypt
docker exec -i $ENROLL_CONTAINER_NAME bash -c "bash /home/entrypoint.sh $kms_type splitandencrypt $appid $appkey $primary_key_name_in_key_folder $data_key_name_in_key_folder $plaintext_data_file_name_in_data_shared_folder $to_save_encrypted_file_name_in_data_shared_folder"

```

## 3. Stop container:
```
docker stop $ENROLL_CONTAINER_NAME
```

