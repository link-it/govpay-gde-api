#!/bin/bash

##############################################################################
# GovPay GDE API Docker Image Build Script
#
# Build a Docker image for GovPay GDE (Giornale degli Eventi) REST API.
# JDBC drivers are NOT included in the image and must be provided at runtime.
#
# Usage: ./build_image.sh [options]
#
# Options:
#   -v VERSION    GovPay GDE version (default: 1.0.2)
#   -t TAG        Additional tag for the image (optional)
#   -h            Show this help message
#
# Examples:
#   ./build_image.sh                      # Build with defaults
#   ./build_image.sh -v 1.0.2            # Specify version
#   ./build_image.sh -t latest           # Add custom tag
##############################################################################

set -e

# Default values
GOVPAY_GDE_VERSION="1.0.2"
IMAGE_NAME="linkitaly/govpay-gde"
ADDITIONAL_TAG=""

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    grep '^#' "$0" | grep -v '#!/bin/bash' | sed 's/^# \?//'
    exit 0
}

# Parse command line arguments
while getopts "v:t:h" opt; do
    case ${opt} in
        v)
            GOVPAY_GDE_VERSION=$OPTARG
            ;;
        t)
            ADDITIONAL_TAG=$OPTARG
            ;;
        h)
            show_usage
            ;;
        \?)
            print_error "Invalid option: -$OPTARG"
            show_usage
            ;;
    esac
done

# Check if Dockerfile exists
if [ ! -f "Dockerfile.github" ]; then
    print_error "Dockerfile.github not found in current directory"
    exit 1
fi

# Check if entrypoint.sh exists
if [ ! -f "entrypoint.sh" ]; then
    print_error "entrypoint.sh not found in current directory"
    exit 1
fi

# Build image tags (no database suffix - drivers are external)
MAIN_TAG="${IMAGE_NAME}:${GOVPAY_GDE_VERSION}"
TAGS="-t ${MAIN_TAG}"

if [ -n "${ADDITIONAL_TAG}" ]; then
    TAGS="${TAGS} -t ${IMAGE_NAME}:${ADDITIONAL_TAG}"
fi

# Print build information
print_info "====================================="
print_info "GovPay GDE API Docker Build"
print_info "====================================="
print_info "GDE Version:  ${GOVPAY_GDE_VERSION}"
print_info "Image tags:   ${MAIN_TAG}"
if [ -n "${ADDITIONAL_TAG}" ]; then
    print_info "              ${IMAGE_NAME}:${ADDITIONAL_TAG}"
fi
print_info "====================================="

# Build the image
print_info "Building Docker image..."
docker build \
    -f Dockerfile.github \
    --build-arg GOVPAY_GDE_VERSION="${GOVPAY_GDE_VERSION}" \
    ${TAGS} \
    .

# Check build result
if [ $? -eq 0 ]; then
    print_info "====================================="
    print_info "${GREEN}Build completed successfully!${NC}"
    print_info "====================================="
    print_info "Image: ${MAIN_TAG}"
    if [ -n "${ADDITIONAL_TAG}" ]; then
        print_info "       ${IMAGE_NAME}:${ADDITIONAL_TAG}"
    fi
    print_info ""
    print_info "IMPORTANT: JDBC drivers must be provided at runtime!"
    print_info "Place JDBC driver JAR files in ./jdbc-drivers/ directory"
    print_info ""
    print_info "To run the container:"
    print_info "  1. Copy .env.template to .env and configure"
    print_info "  2. Place JDBC drivers in ./jdbc-drivers/"
    print_info "  3. docker-compose up -d"
    print_info ""
    print_info "Or manually:"
    print_info "  docker run -d \\"
    print_info "    -e GOVPAY_DB_TYPE=postgresql \\"
    print_info "    -e GOVPAY_DB_SERVER=postgres:5432 \\"
    print_info "    -e GOVPAY_DB_NAME=govpay \\"
    print_info "    -e GOVPAY_DB_USER=govpay \\"
    print_info "    -e GOVPAY_DB_PASSWORD=<password> \\"
    print_info "    -v \$(pwd)/jdbc-drivers:/opt/jdbc-drivers:ro \\"
    print_info "    -v govpay-gde-logs:/var/log/govpay \\"
    print_info "    -p 8080:8080 \\"
    print_info "    ${MAIN_TAG}"
    print_info "====================================="
else
    print_error "Build failed!"
    exit 1
fi
