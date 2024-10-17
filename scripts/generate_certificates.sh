#!/bin/bash
# SPDX-FileCopyrightText: Copyright Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

HOST_NAME=localhost

RESOURCE_DIR=application/src/main/resources/ssl
DEVICE_SERVICE_RESOURCE_DIR="./${RESOURCE_DIR}"
COAP_HTTP_PROXY_RESOURCE_DIR="../sng-coap-http-proxy/${RESOURCE_DIR}"

echo "Generating new proxy certificate"

openssl req -newkey rsa:4096 -new -nodes -x509 -days 3650 \
            -keyout dev-proxy-key.pem -out dev-proxy-cert.pem \
            -subj "/C=NL/ST=Gelderland/L=Arnhem/O=Alliander/OU=IT/CN=${HOST_NAME}"

openssl req -newkey rsa:4096 -new -nodes -x509 -days 3650 \
            -keyout dev-device-service-key.pem -out dev-device-service-cert.pem \
            -subj "/C=NL/ST=Gelderland/L=Arnhem/O=Alliander/OU=IT/CN=${HOST_NAME}"

# Device service files
cp dev-device-service-key.pem "${DEVICE_SERVICE_RESOURCE_DIR}"
cp dev-device-service-cert.pem "${DEVICE_SERVICE_RESOURCE_DIR}"
cp dev-proxy-cert.pem "${DEVICE_SERVICE_RESOURCE_DIR}"

# Proxy files
cp dev-proxy-key.pem "${COAP_HTTP_PROXY_RESOURCE_DIR}"
cp dev-proxy-cert.pem "${COAP_HTTP_PROXY_RESOURCE_DIR}"
cp dev-device-service-cert.pem "${COAP_HTTP_PROXY_RESOURCE_DIR}"

rm ./*.pem

echo "Generated all certificates and stores"
