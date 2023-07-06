#!/usr/bin/env bash
set -o errexit
IFS=$'\n\t'

# nvm is a bash function, so fake command echoing for nvm commands to reduce noise
echo "+ . ${NVM_DIR}/nvm.sh --no-use"
. "${NVM_DIR}/nvm.sh" --no-use

echo "+ nvm install"
nvm install

echo "+ export SDKMAN_DIR=$HOME/.sdkman"
export SDKMAN_DIR="$HOME/.sdkman"

# it seems like a bug in sdkman that this is needed 🤷
echo "+ mkdir -p ${SDKMAN_DIR}/candidates/java/current/bin"
mkdir -p "${SDKMAN_DIR}/candidates/java/current/bin"

echo "+ . ${SDKMAN_DIR}/bin/sdkman-init.sh"
. "${SDKMAN_DIR}/bin/sdkman-init.sh"

echo "+ sdk env install use"
sdk env install use

set -o xtrace -o nounset -o pipefail
npm install -g npm
npm install -g serverless

sbt "deploy Admin"
