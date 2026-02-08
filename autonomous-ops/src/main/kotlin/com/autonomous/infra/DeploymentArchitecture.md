# Deployment Architecture for Autonomous-OP

## Overview
This document describes the deployment architecture for Autonomous-OP, including Kubernetes topology, horizontal scaling strategy, GPU scheduling for ML inference, and observability setup.

## Design Principles
1. **High Availability** - Ensure 24/7 operation with no single point of failure
2. **Scalability** - Horizontal scaling based on resource utilization and traffic
3. **Performance** - Optimized for ML inference with GPU support
4. **Observability** - Comprehensive monitoring, logging, and alerting
5. **Security** - Secure communication between components
6. **Cost Efficiency** - Right-sizing of resources and auto-scaling

## Deployment Diagram

```mermaid
graph TD
    subgraph Kubernetes Cluster
        subgraph Ingress Layer
            Ingress[Ingress Controller<br>(Nginx/ALB)]
        end
        
        subgraph Services
            Gateway[API Gateway<br>(Spring Cloud Gateway)]
            FMS[FMS Service<br>(Spring Boot)]
            TMS[TMS Service<br>(Spring Boot)]
            SCM[SCM Service<br>(Spring Boot)]
            AutonomousOP[Autonomous-OP<br>(Spring Boot)]
            Kafka[Apache Kafka<br>3.5.0]
            PostgreSQL[PostgreSQL<br>15]
            Redis[Redis Cluster<br>7.0]
            Prometheus[Prometheus<br>2.49.0]
            Grafana[Grafana<br>10.2.0]
            Jaeger[Jaeger Tracing<br>1.48.0]
        end
        
        subgraph ML Inference
            MLService[ML Service<br>(TensorFlow/PyTorch)]
            GPUNodes[GPU Nodes<br>(NVIDIA Tesla V100)]
        end
        
        subgraph Horizontal Scaling
            HPA[Horizontal Pod Autoscaler]
            KEDA[KEDA<br>2.11.0]
        end
    end
    
    subgraph External Integrations
        S3[S3 Storage<br>(MinIO)]
        Auth[Authentication<br>(Keycloak)]
        Monitoring[External Monitoring<br>(Datadog)]
    end
    
    Ingress --> Gateway
    Gateway --> FMS
    Gateway --> TMS
    Gateway --> SCM
    Gateway --> AutonomousOP
    
    AutonomousOP --> Kafka
    AutonomousOP --> PostgreSQL
    AutonomousOP --> Redis
    AutonomousOP --> MLService
    
    FMS --> Kafka
    FMS --> PostgreSQL
    
    TMS --> Kafka
    TMS --> PostgreSQL
    
    SCM --> Kafka
    SCM --> PostgreSQL
    
    MLService --> GPUNodes
    
    Prometheus --> Grafana
    Prometheus --> Alertmanager[Alertmanager<br>0.26.0]
    
    Kafka --> Jaeger
    FMS --> Jaeger
    TMS --> Jaeger
    SCM --> Jaeger
    AutonomousOP --> Jaeger
    
    KEDA --> HPA
    HPA --> AutonomousOP
    HPA --> FMS
    HPA --> TMS
    HPA --> SCM
    HPA --> MLService
    
    PostgreSQL --> S3
    Kafka --> S3
```

## Kubernetes Topology

### Cluster Architecture
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: autonomous-op
  labels:
    name: autonomous-op
    environment: production
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: autonomous-op-sa
  namespace: autonomous-op
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: autonomous-op-role
  namespace: autonomous-op
rules:
- apiGroups: [""]
  resources: ["pods", "services", "endpoints"]
  verbs: ["get", "watch", "list"]
- apiGroups: ["apps"]
  resources: ["deployments", "replicasets"]
  verbs: ["get", "watch", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: autonomous-op-rolebinding
  namespace: autonomous-op
subjects:
- kind: ServiceAccount
  name: autonomous-op-sa
  namespace: autonomous-op
roleRef:
  kind: Role
  name: autonomous-op-role
  apiGroup: rbac.authorization.k8s.io
```

### Autonomous-OP Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: autonomous-op
  namespace: autonomous-op
  labels:
    app: autonomous-op
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: autonomous-op
  template:
    metadata:
      labels:
        app: autonomous-op
    spec:
      serviceAccountName: autonomous-op-sa
      containers:
      - name: autonomous-op
        image: nodeorb/autonomous-op:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 8081
          name: management
        resources:
          requests:
            cpu: 1000m
            memory: 2Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: management
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: management
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: autonomous-op-secrets
              key: spring.datasource.url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: autonomous-op-secrets
              key: spring.datasource.username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: autonomous-op-secrets
              key: spring.datasource.password
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka:9092"
        - name: SPRING_REDIS_HOST
          value: "redis"
        - name: SPRING_REDIS_PORT
          value: "6379"
---
apiVersion: v1
kind: Service
metadata:
  name: autonomous-op
  namespace: autonomous-op
  labels:
    app: autonomous-op
spec:
  type: ClusterIP
  selector:
    app: autonomous-op
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  - name: management
    port: 8081
    targetPort: 8081
```

### ML Service Deployment with GPU
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ml-service
  namespace: autonomous-op
  labels:
    app: ml-service
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: ml-service
  template:
    metadata:
      labels:
        app: ml-service
    spec:
      serviceAccountName: autonomous-op-sa
      containers:
      - name: ml-service
        image: nodeorb/ml-service:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8082
          name: http
        - containerPort: 8083
          name: management
        resources:
          requests:
            cpu: 2000m
            memory: 4Gi
            nvidia.com/gpu: 1
          limits:
            cpu: 4000m
            memory: 8Gi
            nvidia.com/gpu: 1
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: management
          initialDelaySeconds: 90
          periodSeconds: 15
          timeoutSeconds: 5
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: management
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka:9092"
        - name: SPRING_REDIS_HOST
          value: "redis"
        - name: SPRING_REDIS_PORT
          value: "6379"
        - name: GPU_ENABLED
          value: "true"
        - name: TF_ENABLE_GPU
          value: "true"
---
apiVersion: v1
kind: Service
metadata:
  name: ml-service
  namespace: autonomous-op
  labels:
    app: ml-service
spec:
  type: ClusterIP
  selector:
    app: ml-service
  ports:
  - name: http
    port: 8082
    targetPort: 8082
  - name: management
    port: 8083
    targetPort: 8083
```

## Horizontal Scaling Strategy

### Horizontal Pod Autoscaler (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: autonomous-op-hpa
  namespace: autonomous-op
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: autonomous-op
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "1000m"
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ml-service-hpa
  namespace: autonomous-op
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ml-service
  minReplicas: 2
  maxReplicas: 6
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: inference_requests_per_second
      target:
        type: AverageValue
        averageValue: "500m"
  - type: Resource
    resource:
      name: nvidia.com/gpu
      target:
        type: Utilization
        averageUtilization: 80
```

### KEDA (Kubernetes Event-driven Autoscaling)
```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: autonomous-op-keda
  namespace: autonomous-op
spec:
  scaleTargetRef:
    name: autonomous-op
  minReplicaCount: 3
  maxReplicaCount: 10
  triggers:
  - type: kafka
    metadata:
      topic: vehicle-telemetry
      bootstrapServers: kafka:9092
      consumerGroup: autonomous-op-keda
      lagThreshold: "1000"
      offsetResetPolicy: earliest
  - type: kafka
    metadata:
      topic: route-updates
      bootstrapServers: kafka:9092
      consumerGroup: autonomous-op-keda
      lagThreshold: "500"
      offsetResetPolicy: earliest
  - type: prometheus
    metadata:
      serverAddress: http://prometheus:9090
      metricName: http_requests_per_second
      query: sum(rate(http_server_requests_seconds_count{app="autonomous-op"}[2m])) / sum(kube_pod_info{app="autonomous-op"})
      threshold: "1000"
---
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: ml-service-keda
  namespace: autonomous-op
spec:
  scaleTargetRef:
    name: ml-service
  minReplicaCount: 2
  maxReplicaCount: 6
  triggers:
  - type: prometheus
    metadata:
      serverAddress: http://prometheus:9090
      metricName: inference_requests_per_second
      query: sum(rate(inference_requests_seconds_count{app="ml-service"}[2m])) / sum(kube_pod_info{app="ml-service"})
      threshold: "500"
  - type: prometheus
    metadata:
      serverAddress: http://prometheus:9090
      metricName: gpu_utilization
      query: sum(rate(nvidia_gpu_duty_cycle{app="ml-service"}[2m])) / sum(kube_pod_info{app="ml-service"})
      threshold: "80"
```

## GPU Scheduling for ML Inference

### Node Labeling and Taints
```bash
# Label GPU nodes
kubectl label nodes gpu-node-01 nvidia.com/gpu.present=true
kubectl label nodes gpu-node-02 nvidia.com/gpu.present=true

# Taint GPU nodes to prevent non-GPU pods from scheduling
kubectl taint nodes gpu-node-01 nvidia.com/gpu:NoSchedule
kubectl taint nodes gpu-node-02 nvidia.com/gpu:NoSchedule
```

### Node Affinity for ML Service
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ml-service
  namespace: autonomous-op
spec:
  template:
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: nvidia.com/gpu.present
                operator: In
                values:
                - "true"
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            preference:
              matchExpressions:
              - key: nvidia.com/gpu.product
                operator: In
                values:
                - Tesla-V100
                - Tesla-T4
  tolerations:
  - key: "nvidia.com/gpu"
    operator: "Exists"
    effect: "NoSchedule"
```

### GPU Resource Limits and Requests
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ml-service
  namespace: autonomous-op
spec:
  template:
    spec:
      containers:
      - name: ml-service
        resources:
          requests:
            cpu: 2000m
            memory: 4Gi
            nvidia.com/gpu: 1
          limits:
            cpu: 4000m
            memory: 8Gi
            nvidia.com/gpu: 1
```

## Observability

### Metrics Collection with Prometheus
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: autonomous-op-monitor
  namespace: autonomous-op
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app: autonomous-op
  endpoints:
  - port: management
    path: /actuator/prometheus
    interval: 30s
---
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: ml-service-monitor
  namespace: autonomous-op
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app: ml-service
  endpoints:
  - port: management
    path: /actuator/prometheus
    interval: 30s
```

### Logging with Loki and Promtail
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: promtail-config
  namespace: autonomous-op
data:
  promtail.yaml: |
    server:
      http_listen_port: 9080
      grpc_listen_port: 0
    positions:
      filename: /tmp/positions.yaml
    clients:
    - url: http://loki:3100/loki/api/v1/push
    scrape_configs:
    - job_name: kubernetes-pods
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        target_label: app
      - source_labels: [__meta_kubernetes_pod_name]
        target_label: pod
      - source_labels: [__meta_kubernetes_namespace]
        target_label: namespace
      - source_labels: [__meta_kubernetes_pod_container_name]
        target_label: container
```

### Tracing with Jaeger
```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: autonomous-op-jaeger
  namespace: autonomous-op
spec:
  strategy: production
  sampler:
    type: probabilistic
    param: 0.1
  collector:
    replicas: 2
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi
  query:
    replicas: 2
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi
  storage:
    type: elasticsearch
    options:
      es:
        server-urls: http://elasticsearch:9200
        index-prefix: jaeger-autonomous-op
        username: elastic
        password: elastic
```

### Alerting with Alertmanager
```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: autonomous-op-alerts
  namespace: autonomous-op
  labels:
    release: prometheus
spec:
  groups:
  - name: autonomous-op.rules
    rules:
    - alert: HighCpuUsage
      expr: sum(rate(container_cpu_usage_seconds_total{container="autonomous-op", namespace="autonomous-op"}[5m])) / sum(kube_pod_container_resource_limits_cpu_cores{container="autonomous-op", namespace="autonomous-op"}) * 100 > 85
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: High CPU usage in Autonomous-OP
        description: CPU usage has been > 85% for 5 minutes
    - alert: HighMemoryUsage
      expr: sum(container_memory_working_set_bytes{container="autonomous-op", namespace="autonomous-op"}) / sum(kube_pod_container_resource_limits_memory_bytes{container="autonomous-op", namespace="autonomous-op"}) * 100 > 90
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: High memory usage in Autonomous-OP
        description: Memory usage has been > 90% for 5 minutes
    - alert: GPUUtilizationTooHigh
      expr: sum(rate(nvidia_gpu_duty_cycle{app="ml-service", namespace="autonomous-op"}[5m])) / count(nvidia_gpu_duty_cycle{app="ml-service", namespace="autonomous-op"}) > 90
      for: 3m
      labels:
        severity: critical
      annotations:
        summary: GPU utilization too high
        description: GPU utilization has been > 90% for 3 minutes
    - alert: KafkaLagTooHigh
      expr: sum(kafka_consumergroup_lag{group="autonomous-op", topic="vehicle-telemetry"}) > 1000
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: Kafka consumer lag too high
        description: Kafka consumer lag for vehicle-telemetry topic is > 1000
```

## Resource Sizing Rules

### General Service Sizing
| Service | CPU Request | CPU Limit | Memory Request | Memory Limit | Replicas (Min/Max) |
|---------|--------------|------------|-----------------|---------------|----------------------|
| Autonomous-OP | 1000m | 2000m | 2Gi | 4Gi | 3/10 |
| FMS Service | 500m | 1000m | 1Gi | 2Gi | 2/5 |
| TMS Service | 500m | 1000m | 1Gi | 2Gi | 2/5 |
| SCM Service | 500m | 1000m | 1Gi | 2Gi | 2/5 |
| ML Service | 2000m | 4000m | 4Gi | 8Gi | 2/6 |

### Infrastructure Sizing
| Component | CPU Request | CPU Limit | Memory Request | Memory Limit | Replicas |
|-----------|--------------|------------|-----------------|---------------|-----------|
| Kafka | 1000m | 2000m | 2Gi | 4Gi | 3 |
| PostgreSQL | 1000m | 2000m | 4Gi | 8Gi | 3 (HA) |
| Redis | 500m | 1000m | 1Gi | 2Gi | 3 (Cluster) |
| Prometheus | 1000m | 2000m | 4Gi | 8Gi | 1 |
| Grafana | 500m | 1000m | 1Gi | 2Gi | 1 |
| Jaeger | 1000m | 2000m | 2Gi | 4Gi | 2 |

### GPU Sizing
| GPU Type | Memory | Inference Speed | Max Throughput | Cost (USD/hr) |
|----------|--------|------------------|-----------------|----------------|
| Tesla V100 | 16GB | 1000 req/sec | 2000 req/sec | $0.70 |
| Tesla T4 | 16GB | 800 req/sec | 1600 req/sec | $0.50 |
| Tesla A100 | 40GB | 1500 req/sec | 3000 req/sec | $1.20 |

## Scaling Triggers

### Autonomous-OP
- **CPU Usage**: > 70% for 5 minutes
- **Memory Usage**: > 80% for 5 minutes
- **HTTP Requests**: > 1000 requests per second
- **Kafka Lag**: > 1000 messages in vehicle-telemetry topic

### ML Service
- **CPU Usage**: > 70% for 5 minutes
- **Memory Usage**: > 80% for 5 minutes
- **Inference Requests**: > 500 requests per second
- **GPU Utilization**: > 80% for 3 minutes

### Infrastructure
- **Kafka**: > 80% partition utilization
- **PostgreSQL**: > 90% connection pool utilization
- **Redis**: > 85% memory usage

## Security

### Network Policies
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: autonomous-op-network-policy
  namespace: autonomous-op
spec:
  podSelector:
    matchLabels:
      app: autonomous-op
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: gateway
    - podSelector:
        matchLabels:
          app: prometheus
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 8081
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: kafka
    ports:
    - protocol: TCP
      port: 9092
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
```

### Secrets Management
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: autonomous-op-secrets
  namespace: autonomous-op
type: Opaque
data:
  spring.datasource.url: <base64 encoded>
  spring.datasource.username: <base64 encoded>
  spring.datasource.password: <base64 encoded>
  spring.kafka.username: <base64 encoded>
  spring.kafka.password: <base64 encoded>
  spring.redis.password: <base64 encoded>
```

## Conclusion
This deployment architecture provides a comprehensive, scalable, and resilient infrastructure for Autonomous-OP with Kubernetes. It includes horizontal scaling based on resource utilization and Kafka lag, GPU scheduling for ML inference, and observability with Prometheus, Grafana, Jaeger, and Loki. The architecture ensures high availability, performance, and security for production deployments.