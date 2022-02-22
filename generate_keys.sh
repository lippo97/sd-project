#!/usr/bin/env sh

keysdir="generated-keys"
publickey="public.pem"
privatekey="private.pkcs8.pem"

[ ! -d "$keysdir" ] && mkdir -p "$keysdir"
(
  cd "$keysdir"
  if [  -f "$publickey" -o -f "$privatekey" ]; then
    echo "Directory $keysdir was not empty."
    exit 1
  fi
  openssl genrsa -out key.pem 2048
  openssl pkcs8 -topk8 -inform PEM -in key.pem -out "$privatekey" -nocrypt
  openssl rsa -in key.pem -outform PEM -pubout -out "$publickey"
  rm key.pem
  echo "Keys generated successfully."
)
