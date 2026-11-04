#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src -name '*.java')
echo "Готово: классы собраны в out/"
