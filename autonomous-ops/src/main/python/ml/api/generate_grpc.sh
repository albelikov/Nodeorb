#!/bin/bash

# Generate Python gRPC files from proto files
cd ../../../../
mkdir -p autonomous-ops/src/main/python/ml/api/gen
python -m grpc_tools.protoc -I autonomous-ops/src/main/kotlin/com/autonomous/api/grpc --python_out=autonomous-ops/src/main/python/ml/api/gen --grpc_python_out=autonomous-ops/src/main/python/ml/api/gen autonomous-ops/src/main/kotlin/com/autonomous/api/grpc/ml_bridge.proto
echo "gRPC files generated successfully"