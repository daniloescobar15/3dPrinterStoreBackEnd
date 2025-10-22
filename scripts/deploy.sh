#!/bin/bash

# Deployment script for Puntored Adapter
set -e

# Variables
SERVER_IP="158.220.99.85"
SERVER_USER="root"
SERVER_PORT="22"
DEPLOY_PORT="1500"
APP_NAME="puntored-adapter"
IMAGE_NAME="puntored-adapter"
IMAGE_TAG="${CIRCLE_SHA1:0:7}"

echo "Starting deployment to $SERVER_IP..."

# Build and tag the Docker image
echo "Building Docker image..."
docker build -t $IMAGE_NAME:$IMAGE_TAG .
docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest

# Save the image to a tar file for transfer
echo "Preparing image for transfer..."
docker save $IMAGE_NAME:$IMAGE_TAG -o /tmp/$IMAGE_NAME-$IMAGE_TAG.tar

# Transfer image to server
echo "Transferring image to server..."
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
    -P $SERVER_PORT \
    /tmp/$IMAGE_NAME-$IMAGE_TAG.tar \
    $SERVER_USER@$SERVER_IP:/tmp/

# Deploy via SSH
echo "Deploying application..."
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
    -p $SERVER_PORT \
    $SERVER_USER@$SERVER_IP << 'EOF'

# Load the docker image
echo "Loading Docker image on server..."
docker load -i /tmp/puntored-adapter-*.tar

# Stop existing container if running
echo "Stopping existing container..."
docker stop puntored-adapter || true
docker rm puntored-adapter || true

# Run the new container
echo "Starting new container..."
docker run -d \
    --name puntored-adapter \
    -p $DEPLOY_PORT:8080 \
    --restart unless-stopped \
    puntored-adapter:latest

# Verify deployment
echo "Verifying deployment..."
sleep 5
curl -f http://localhost:$DEPLOY_PORT/api/hello || exit 1

echo "Deployment completed successfully!"

EOF

# Cleanup
echo "Cleaning up..."
rm -f /tmp/$IMAGE_NAME-$IMAGE_TAG.tar

echo "Deployment finished!"