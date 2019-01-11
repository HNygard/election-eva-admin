#!/usr/bin/env bash
# Nullstiller sertifikatene til bruk for testing. Nyttig når man ønsker å endre noe. Sjekk inn p12-filene.

rm -rf certs private *.p12 *.pem certindex.* serial serial.old

mkdir certs private
echo '100001' >serial
touch certindex.txt

printf '\n\n\n\n\n\nTest CA\n' | openssl req -new -sha256 -nodes -keyout private/cakey.pem -x509 -extensions v3_ca -out cacert.pem -days 3600 -config ./openssl.conf
printf '\n\n\n\n\n\nTest 1\n' | openssl req -new -sha256 -nodes -keyout private/test1-key.pem -out test1-req.pem -days 3600 -config ./openssl.conf
printf '\n\n\n\n\n\nTest 2\n' | openssl req -new -sha256 -nodes -keyout private/test2-key.pem -out test2-req.pem -days 3600 -config ./openssl.conf

yes | openssl ca -out test1-cert.pem -days 3600 -extensions v3_req -config ./openssl.conf -infiles test1-req.pem
yes | openssl ca -out test2-cert.pem -days 3600 -extensions v3_req -config ./openssl.conf -infiles test2-req.pem

printf 'testpassord' | openssl pkcs12 -export -inkey private/test1-key.pem -in test1-cert.pem -certfile cacert.pem -password stdin -out test1.p12
printf 'testpassord' | openssl pkcs12 -export -inkey private/test2-key.pem -in test2-cert.pem -certfile cacert.pem -password stdin -out test2.p12
printf 'testpassord' | openssl pkcs12 -export -inkey private/cakey.pem -in cacert.pem -password stdin -out ca.p12

rm -f *.old test*.pem
