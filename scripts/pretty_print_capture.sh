#!/usr/bin/env bash

echo "Pretty printing JSON files"

FILES=$(find . -type f -regex "^\./out/capture/.*\.json$")

for original in $FILES
do
echo "Processing: $original"

new=$original"_pretty"

cat $original | jq '.' > $new
rm $original
mv $new $original

done