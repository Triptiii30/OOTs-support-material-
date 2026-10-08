#!/bin/sh
# Maven wrapper script for Unix/Linux
if [ -x "$(command -v mvn)" ]; then
    exec mvn "$@"
else
    echo "Maven not found on PATH. Please install maven."
    exit 1
fi
