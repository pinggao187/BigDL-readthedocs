# Train DIEN using the Amazon Book Reviews dataset
This folder showcases how to use BigDL Friesian to preprocess and train a [DIEN](https://arxiv.org/pdf/1809.03672.pdf) model. 
Model definition is based on [ai-matrix](https://github.com/alibaba/ai-matrix/tree/master/macro_benchmark/DIEN) and
[Amazon Book Reviews](http://snap.stanford.edu/data/amazon/productGraph/categoryFiles/reviews_Books.json.gz) dataset is used in this example.

## Prepare the environment
We recommend you to use [Anaconda](https://www.anaconda.com/distribution/#linux) to prepare the environments, especially if you want to run on a yarn cluster (yarn-client mode only).
```
conda create -n bigdl python=3.7  # "bigdl" is the conda environment name, you can use any name you like.
conda activate bigdl
pip install tensorflow==1.15.0
pip install --pre --upgrade bigdl-friesian
```

## Prepare the data
1. Download meta_Books data from [here](http://snap.stanford.edu/data/amazon/productGraph/categoryFiles/meta_Books.json.gz). 
2. Download full reviews_Books data from [here](http://snap.stanford.edu/data/amazon/productGraph/categoryFiles/reviews_Books.json.gz) which contains 22,507,155 records, or you can start from the [small dataset](http://snap.stanford.edu/data/amazon/productGraph/categoryFiles/reviews_Books_5.json.gz) which contains 8,898,041 records.
3. Use the following script to convert `meta_Books.json` to `meta_Books.csv`:
```bash
python meta_to_csv.py --input_meta /path/to/meta_Books.json
```

## Preprocess the data  
* Spark local, example command:
```bash
python dien_preprocessing.py \
    --executor_cores 8 \
    --executor_memory 50g \
    --input_meta /path/to/meta_Books.csv \
    --input_transaction /path/to/reviews_Books.json \
    --output /path/to/the/folder/to/save/preprocessed/parquet/files
```

* Spark standalone, example command:
```bash
python dien_preprocessing.py \
    --cluster_mode standalone \
    --master spark://master-url:port \
    --executor_cores 40 \
    --executor_memory 50g \
    --num_executors 8 \
    --input_meta /path/to/meta_Books.csv \
    --input_transaction /path/to/reviews_Books.json \
    --output /path/to/the/folder/to/save/preprocessed/parquet/files
```

* Spark yarn client mode, example command:
```bash
python dien_preprocessing.py \
    --cluster_mode yarn \
    --executor_cores 40 \
    --executor_memory 50g \
    --num_executors 8 \
    --input_meta /path/to/meta_Books.csv \
    --input_transaction /path/to/reviews_Books.json \
    --output /path/to/the/folder/to/save/preprocessed/parquet/files
```

__Options:__
* `input_meta`: __Required.__ The path to `meta_Books.csv`, either a local path or an HDFS path.
* `input_transaction`: __Required.__ The path to `reviews_Books.json`, either a local path or an HDFS path.
* `cluster_mode`: The cluster mode to run the data preprocessing, one of local, yarn, standalone or spark-submit. Default to be local.
* `master`: The master URL, only used when cluster_mode is standalone.
* `executor_cores`: The number of cores to use on each node. Default to be 48.
* `executor_memory`: The amount of memory to allocate on each node. Default to be 160g.
* `num_executors`: The number of nodes to use in the cluster. Default to be 8.
* `driver_cores`: The number of cores to use for the driver. Default to be 4.
* `driver_memory`: The amount of memory to allocate for the driver. Default to be 36g.
* `output`: The path to save the preprocessed data to parquet files. HDFS path is recommended. Default to be the current working directory.

## Train DIEN
* Spark local:
```bash
python dien_train.py \
    --executor_cores 8 \
    --executor_memory 50g \
    --batch_size 128 \
    --data_dir /path/to/the/folder/to/save/preprocessed/parquet/files \
    --model_dir /path/to/the/folder/to/save/trained/model 
```

* Spark standalone, example command:
```bash
python dien_train.py \
    --cluster_mode standalone \
    --master spark://master-url:port \
    --executor_cores 8 \
    --executor_memory 50g \
    --num_executors 8 \
    --batch_size 128 \
    --data_dir /path/to/the/folder/to/save/preprocessed/parquet/files \
    --model_dir /path/to/the/folder/to/save/trained/model 
```

* Spark yarn client mode, example command:
```bash
python dien_train.py \
    --cluster_mode yarn \
    --executor_cores 8 \
    --executor_memory 50g \
    --num_executors 8 \
    --batch_size 128 \
    --data_dir /path/to/the/folder/to/save/preprocessed/parquet/files \
    --model_dir /path/to/the/folder/to/save/trained/model 
```

__Options:__
* `cluster_mode`: The cluster mode to run the training, one of local, yarn, standalone, or spark-submit. Default to be local.
* `master`: The master URL, only used when cluster_mode is standalone.
* `executor_cores`: The number of cores to use on each node. Default to be 48.
* `executor_memory`: The amount of memory to allocate on each node. Default to be 240g.
* `num_executors`: The number of nodes to use in the cluster. Default to be 40.
* `driver_cores`: The number of cores to use for the driver. Default to be 4.
* `driver_memory`: The amount of memory to allocate for the driver. Default to be 36g.
* `batch_size`: The batch size for training. Default to be 8.
* `data_dir`: The path to the preprocessed data.
* `model_dir`: The path to save the trained model.
