#!/bin/bash

ARGS=""

if [ ! -z "$MAX_MEMORY" ]; then
    ARGS="$ARGS --maxMemory=$MAX_MEMORY"
fi
if [ ! -z "$MAX_CONNECTIONS" ]; then
    ARGS="$ARGS --maxConnections=$MAX_CONNECTIONS"
fi
if [ ! -z "$BASE_CURRENCY_NETWORK" ]; then
    ARGS="$ARGS --baseCurrencyNetwork=$BASE_CURRENCY_NETWORK"
fi
if [ ! -z "$APP_NAME" ]; then
    ARGS="$ARGS --appName=$APP_NAME"
fi
if [ ! -z "$NODE_PORT" ]; then
    ARGS="$ARGS --nodePort=$NODE_PORT"
fi
if [ ! -z "$DAO_ACTIVATED" ]; then
    ARGS="$ARGS --daoActivated=$DAO_ACTIVATED"
fi
if [ ! -z "$FULL_DAO_NODE" ]; then
    ARGS="$ARGS --fullDaoNode=$FULL_DAO_NODE"
fi
if [ ! -z "$RPC_HOST" ]; then
    ARGS="$ARGS --rpcHost=$RPC_HOST"
fi
if [ ! -z "$RPC_PORT" ]; then
    ARGS="$ARGS --rpcPort=$RPC_PORT"
fi
if [ ! -z "$RPC_USER" ]; then
    ARGS="$ARGS --rpcUser=$RPC_USER"
fi
if [ ! -z "$RPC_PASSWORD" ]; then
    ARGS="$ARGS --rpcPassword=$RPC_PASSWORD"
fi
if [ ! -z "$RPC_BLOCKNOTIFY_PORT" ]; then
    ARGS="$ARGS --rpcBlockNotificationPort=$RPC_BLOCKNOTIFY_PORT"
fi
if [ ! -z "$RPC_BLOCKNOTIFY_HOST" ]; then
    ARGS="$ARGS --rpcBlockNotificationHost=$RPC_BLOCKNOTIFY_HOST"
fi


# not used in production atm
if [ ! -z "$SEED_NODES" ]; then
    ARGS="$ARGS --seedNodes=$SEED_NODES"
fi
if [ ! -z "$BTC_NODES" ]; then
    ARGS="$ARGS --btcNodes=$BTC_NODES"
fi
if [ ! -z "$USE_LOCALHOST_FOR_P2P" ]; then
    ARGS="$ARGS --useLocalhostForP2P=$USE_LOCALHOST_FOR_P2P"
fi

while true
do

    echo `date` "(Re)-starting node"

    JAVA_OPTS='-Xms8192M -Xmx8192M -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6969 -Dcom.sun.management.jmxremote.rmi.port=6969 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false' ./bin/bisq-seednode $ARGS 2>./error.log

    echo `date` "node terminated unexpectedly!!"

    sleep 10
done
