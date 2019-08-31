#!/bin/bash

set -eo pipefail

services=(
 'server'
 'client'
)

if [[ "$#" -gt 0 ]]; then
  services=("$@")
fi

(
  docker buildx bake --progress='plain' "${services[@]}"
)
