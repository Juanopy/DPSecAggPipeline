#!/usr/bin/env sh

#export PRIVEE_PATH=/asc-dl-pipeline/

trap '' INT
#. /usr/local/nvm/nvm.sh && \
. ~/.bashrc && \
cd "$PRIVEE_PATH/webserver" && \
nvm use && \
yarn start -l 2>&1 | tee /tmp/privee.log
