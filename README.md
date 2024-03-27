<!--
SPDX-FileCopyrightText: Contributors to the GXF project

SPDX-License-Identifier: Apache-2.0
-->
# Crest Device Service

## PSK change flow

When a shipment file is imported, the key and secret in the shipment file will be set as the active
PSK.
Also, a new PSK will be generated and set to status `READY`.

To test this flow locally:

1. If you have previously run the crest device service with the `test-data` profile, and you wish to
   test this new flow, delete the database container to clear the existing data, then run the
   service without the `test-data` profile.
2. Set `crest-device-service.psk.change-initial-psk` to `true`.
3. Run the crest device service, the shipment file processor and maki connect light.
4. Upload the `test_shipment_file.json` file to the shipment file processor.
5. There should be an active and a ready psk in the database.
6. Run the coap-http proxy.
7. Run the simulator.
8. When the crest device service receives a message from the device (simulator), a psk set command will be sent in the downlink response to the device (simulator). The new key will be set to pending.
    1. When the crest device service receives a success result code in the subsequent message from the device (simulator), the pending key will be set to active and the old key will become inactive.
    2. When the crest device service receives a failure result code in the subsequent message from the device (simulator), the pending key will be set to invalid.
