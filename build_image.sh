#!/bin/bash

##############################################################################
# Script di Build Immagine Docker GovPay GDE API
#
# Costruisce un'immagine Docker per l'API REST GovPay GDE (Giornale degli
# Eventi). I driver JDBC NON sono inclusi nell'immagine e devono essere
# forniti a runtime.
#
# Uso: ./build_image.sh [opzioni]
#
# Opzioni:
#   -v VERSION    Versione GovPay GDE (default: 1.0.2)
#   -t TAG        Tag aggiuntivo per l'immagine (opzionale)
#   -h            Mostra questo messaggio di aiuto
#
# Esempi:
#   ./build_image.sh                      # Build con valori di default
#   ./build_image.sh -v 1.0.2            # Specifica versione
#   ./build_image.sh -t latest           # Aggiungi tag personalizzato
##############################################################################

set -e

# Valori di default
GOVPAY_GDE_VERSION="1.0.2"
IMAGE_NAME="linkitaly/govpay-gde"
ADDITIONAL_TAG=""

# Output colorato
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # Nessun colore

# Funzione per stampare messaggi colorati
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Funzione per mostrare l'uso
show_usage() {
    grep '^#' "$0" | grep -v '#!/bin/bash' | sed 's/^# \?//'
    exit 0
}

# Parsing argomenti da linea di comando
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
            print_error "Opzione non valida: -$OPTARG"
            show_usage
            ;;
    esac
done

# Verifica esistenza Dockerfile
if [ ! -f "Dockerfile.github" ]; then
    print_error "Dockerfile.github non trovato nella directory corrente"
    exit 1
fi

# Verifica esistenza entrypoint.sh
if [ ! -f "entrypoint.sh" ]; then
    print_error "entrypoint.sh non trovato nella directory corrente"
    exit 1
fi

# Costruzione tag immagine (senza suffisso database - driver esterni)
MAIN_TAG="${IMAGE_NAME}:${GOVPAY_GDE_VERSION}"
TAGS="-t ${MAIN_TAG}"

if [ -n "${ADDITIONAL_TAG}" ]; then
    TAGS="${TAGS} -t ${IMAGE_NAME}:${ADDITIONAL_TAG}"
fi

# Stampa informazioni di build
print_info "====================================="
print_info "Build Docker GovPay GDE API"
print_info "====================================="
print_info "Versione GDE:  ${GOVPAY_GDE_VERSION}"
print_info "Tag immagine:  ${MAIN_TAG}"
if [ -n "${ADDITIONAL_TAG}" ]; then
    print_info "               ${IMAGE_NAME}:${ADDITIONAL_TAG}"
fi
print_info "====================================="

# Build dell'immagine
print_info "Costruzione immagine Docker..."
docker build \
    -f Dockerfile.github \
    --build-arg GOVPAY_GDE_VERSION="${GOVPAY_GDE_VERSION}" \
    ${TAGS} \
    .

# Verifica risultato build
if [ $? -eq 0 ]; then
    print_info "====================================="
    print_info "${GREEN}Build completata con successo!${NC}"
    print_info "====================================="
    print_info "Immagine: ${MAIN_TAG}"
    if [ -n "${ADDITIONAL_TAG}" ]; then
        print_info "          ${IMAGE_NAME}:${ADDITIONAL_TAG}"
    fi
    print_info ""
    print_info "IMPORTANTE: I driver JDBC devono essere forniti a runtime!"
    print_info "Posizionare i file JAR dei driver JDBC nella directory ./jdbc-drivers/"
    print_info ""
    print_info "Per eseguire il container:"
    print_info "  1. Copiare .env.template in .env e configurare"
    print_info "  2. Posizionare i driver JDBC in ./jdbc-drivers/"
    print_info "  3. docker-compose up -d"
    print_info ""
    print_info "Oppure manualmente:"
    print_info "  docker run -d \\"
    print_info "    -e GOVPAY_DB_TYPE=postgresql \\"
    print_info "    -e GOVPAY_DB_SERVER=postgres:5432 \\"
    print_info "    -e GOVPAY_DB_NAME=govpay \\"
    print_info "    -e GOVPAY_DB_USER=govpay \\"
    print_info "    -e GOVPAY_DB_PASSWORD=<password> \\"
    print_info "    -v \$(pwd)/jdbc-drivers:/opt/jdbc-drivers:ro \\"
    print_info "    -v govpay-gde-logs:/var/log/govpay \\"
    print_info "    -p 10002:10002 \\"
    print_info "    ${MAIN_TAG}"
    print_info "====================================="
else
    print_error "Build fallita!"
    exit 1
fi
