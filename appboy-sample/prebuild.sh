#!/usr/bin/env bash

cd ../appboy-segment-integration
./gradlew publishToMavenLocal -PenableSigning=false
