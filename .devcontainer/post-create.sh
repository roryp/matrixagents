#!/usr/bin/env bash
set -euo pipefail

NPM_REGISTRY="${NPM_CONFIG_REGISTRY:-https://packagefeedproxy.microsoft.io/npm/}"

echo "Configuring npm registry to $NPM_REGISTRY..."
npm config set registry "$NPM_REGISTRY"

# Keep all project-local and global npm installs below the registry configuration above.
sudo chown -R "$(id -u):$(id -g)" frontend/node_modules
npm --prefix frontend ci