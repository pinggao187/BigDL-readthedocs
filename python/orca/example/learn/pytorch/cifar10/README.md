# PyTorch Cifar10 example
We demonstrate how to easily run synchronous distributed PyTorch training using PyTorch Estimator of Project Orca in Analytics Zoo. We use a simple convolutional nueral network model to train on Cifar10 dataset, which is a dataset for image classification. See [here](https://pytorch.org/tutorials/beginner/blitz/cifar10_tutorial.html) for the original single-node version of this example provided by PyTorch. We provide three distributed PyTorch training backends for this example, namely "bigdl", "ray" and "spark". You can run with either backend as you wish.

## Prepare the environment

We recommend you to use Anaconda to prepare the environment, especially if you want to run on a yarn cluster:

```
conda create -n bigdl python=3.7  # "bigdl" is conda environment name, you can use any name you like.
conda activate bigdl
pip install torch
pip install torchvision
pip install matplotlib

# For bigdl backend:
pip install bigdl-orca
pip install jep==3.9.0
pip install six cloudpickle

# For ray backend:
pip install bigdl-orca[ray]
pip install tqdm  # progress bar

# For spark backend
pip install bigdl-orca
pip install tqdm  # progress bar
```

## Run on local after pip install

The default backend is `bigdl`.

```
python cifar10.py
```

You can run with `ray` backend via:

```
python cifar10.py --backend ray
```

You can run with `spark` backend via:

```
python cifar10.py --backend spark
```

## Run on yarn cluster for yarn-client mode after pip install

```
export HADOOP_CONF_DIR=the directory of the hadoop and yarn configurations
python cifar10.py --cluster_mode yarn-client
```

The default backend is `bigdl`. You can also run with `ray` or `spark` by specifying the backend.

## Results

**For "bigdl" backend**

You can find the logs for training as follows:
```
2020-12-03 15:25:30 INFO  DistriOptimizer$:426 - [Epoch 2 47680/50000][Iteration 24420][Wall Clock 497.634203315s] Trained 4 records in 0.022554577 seconds. Throughput is 177.3476 records/second. Loss is 0.82751834.
```

Final test results will be printed at the end:
```
Accuracy of the network on the test images: {'Top1Accuracy': 0.541100025177002}
```

**For "ray" and "spark" backend**

Final test results will be printed at the end:
```
num_samples : 10000
Accuracy : tensor(0.5378)
val_loss : 1.3078322240829467
```
