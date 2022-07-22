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

from __future__ import print_function
import os
from os.path import exists
from os import makedirs
import argparse
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import datasets, transforms
from bigdl.orca import init_orca_context, stop_orca_context
from bigdl.orca.torch import TorchModel, TorchLoss, TorchOptim
from bigdl.orca.common import *
from bigdl.dllib.estimator import *
from bigdl.dllib.optim.optimizer import SGD, Adam
from bigdl.dllib.feature.common import FeatureSet
from bigdl.dllib.keras.metrics import Accuracy


class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.conv1 = nn.Conv2d(1, 20, 5, 1)
        self.conv2 = nn.Conv2d(20, 50, 5, 1)
        self.fc1 = nn.Linear(4*4*50, 500)
        self.fc2 = nn.Linear(500, 10)

    def forward(self, x):
        x = F.relu(self.conv1(x))
        x = F.max_pool2d(x, 2, 2)
        x = F.relu(self.conv2(x))
        x = F.max_pool2d(x, 2, 2)
        x = x.view(-1, 4*4*50)
        x = F.relu(self.fc1(x))
        x = self.fc2(x)
        return F.log_softmax(x, dim=1)


def main():
    # Training settings
    parser = argparse.ArgumentParser(description='PyTorch MNIST Example')
    parser.add_argument('--dir', default='/tmp/data', metavar='N',
                        help='the folder store mnist data')
    parser.add_argument('--batch-size', type=int, default=256, metavar='N',
                        help='input batch size for training per executor(default: 256)')
    parser.add_argument('--test-batch-size', type=int, default=1000, metavar='N',
                        help='input batch size for testing per executor(default: 1000)')
    parser.add_argument('--epochs', type=int, default=2, metavar='N',
                        help='number of epochs to train (default: 2)')
    parser.add_argument('--lr', type=float, default=0.001, metavar='LR',
                        help='learning rate (default: 0.001)')
    parser.add_argument('--seed', type=int, default=1, metavar='S',
                        help='random seed (default: 1)')
    parser.add_argument('--save-model', action='store_true', default=False,
                        help='For Saving the current Model')
    parser.add_argument('--deploy-mode', default="local",
                        help='supported deploy mode is local, yarn-client, yarn-cluster')
    parser.add_argument('--download', type=bool, default=True,
                        help='download dataset or prepare by yourself')

    args = parser.parse_args()

    torch.manual_seed(args.seed)
    if not exists(args.dir):
        makedirs(args.dir)
    train_loader = torch.utils.data.DataLoader(
        datasets.MNIST(args.dir, train=True, download=args.download,
                       transform=transforms.Compose([
                           transforms.ToTensor(),
                           transforms.Normalize((0.1307,), (0.3081,))
                       ])),
        batch_size=args.batch_size, shuffle=True)
    test_loader = torch.utils.data.DataLoader(
        datasets.MNIST(args.dir, train=False,download=args.download,
                       transform=transforms.Compose([
                           transforms.ToTensor(),
                           transforms.Normalize((0.1307,), (0.3081,))
                       ])),
        batch_size=args.test_batch_size, shuffle=False)

    # init on yarn when HADOOP_CONF_DIR and ZOO_CONDA_NAME is provided.
    if args.deploy_mode == "local":
        sc = init_orca_context()
    else:
        sc = init_orca_context(cluster_mode=args.deploy_mode)

    model = Net()
    model.train()
    criterion = nn.NLLLoss()

    adam = torch.optim.Adam(model.parameters(), lr=args.lr)
    zoo_model = TorchModel.from_pytorch(model)
    zoo_criterion = TorchLoss.from_pytorch(criterion)
    zoo_optim = TorchOptim.from_pytorch(adam)
    zoo_estimator = Estimator(zoo_model, optim_methods=zoo_optim)
    train_featureset = FeatureSet.pytorch_dataloader(train_loader)
    test_featureset = FeatureSet.pytorch_dataloader(test_loader)
    from bigdl.dllib.optim.optimizer import MaxEpoch, EveryEpoch
    zoo_estimator.train_minibatch(train_featureset, zoo_criterion,
                                  end_trigger=MaxEpoch(args.epochs),
                                  checkpoint_trigger=EveryEpoch(),
                                  validation_set=test_featureset,
                                  validation_method=[Accuracy()])


if __name__ == '__main__':
    main()

