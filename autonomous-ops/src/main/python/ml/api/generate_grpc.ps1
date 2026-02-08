# Generate Python gRPC files from proto files
Set-Location ../../../../
New-Item -ItemType Directory -Path autonomous-ops/src/main/python/ml/api/gen -Force | Out-Null
python -m grpc_tools.protoc -I autonomous-ops/src/main/kotlin/com/autonomous/api/grpc --python_out=autonomous-ops/src/main/python/ml/api/gen --grpc_python_out=autonomous-ops/src/main/python/ml/api/gen autonomous-ops/src/main/kotlin/com/autonomous/api/grpc/ml_bridge.proto
Write-Host "gRPC files generated successfully"