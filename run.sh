#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out crm.Main
