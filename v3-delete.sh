#!/bin/sh

APP=index-import-task

cf curl -X DELETE `cf curl /v3/service_bindings | jq  -r .resources[0].links.self.href`
cf v3-delete $APP