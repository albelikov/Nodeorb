class MaintenanceModel:
    def __init__(self):
        self.maintenance_thresholds = {
            "LOW": 0.2,
            "MEDIUM": 0.5,
            "HIGH": 0.8
        }

    def predict(self, request):
        # Calculate maintenance probability
        distance = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(request.waypoints[1:], request.waypoints[:-1])
        ) * 1000  # Convert to meters

        payload_factor = request.parameters.payload_kg * 0.001
        age_factor = 0.001  # Assuming age factor is 0.001 per day

        maintenance_probability = distance * 0.0001 + payload_factor + age_factor

        return min(maintenance_probability, 1.0)