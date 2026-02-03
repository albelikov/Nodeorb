#!/bin/bash

# Create a temporary directory
mkdir -p /tmp/jar-extract
cd /tmp/jar-extract

# Extract the JAR
jar -xf /app/freight-marketplace/build/libs/freight-marketplace-0.1.0-SNAPSHOT.jar

# Remove spatial dependencies from the JAR
find . -name "*hibernate-spatial*" -delete
find . -name "*postgis*" -delete
find . -name "*jts*" -delete

# Recreate the JAR
jar -cf /app/freight-marketplace/build/libs/freight-marketplace-0.1.0-SNAPSHOT-clean.jar .

echo "JAR file cleaned successfully"