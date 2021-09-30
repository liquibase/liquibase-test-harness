#!/usr/bin/env bash

set -ex

VERSION=$1

curl -Ls https://github.com/titan-data/titan/releases/download/$VERSION/titan-cli-$VERSION-linux_amd64.tar -o titan.tar
tar -xvf titan.tar
cp $PWD/titan /usr/local/bin/titan
titan install
sleep 30s