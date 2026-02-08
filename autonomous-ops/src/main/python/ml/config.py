class Config:
    """Configuration settings for the ML service"""
    
    # Server settings
    GRPC_PORT = 50051
    GRPC_HOST = 'localhost'
    
    # Model settings
    RISK_MODEL_PATH = 'models/risk_model.joblib'
    MAINTENANCE_MODEL_PATH = 'models/maintenance_model.joblib'
    WEATHER_MODEL_PATH = 'models/weather_model.joblib'
    
    # Simulation settings
    DEFAULT_SIMULATION_ITERATIONS = 1000
    MAX_SIMULATION_ITERATIONS = 10000
    
    # Logging settings
    LOG_FILE = 'ml_service.log'
    LOG_LEVEL = 'INFO'
    
    # Feature engineering settings
    NUMERIC_FEATURES = [
        'max_speed_kmh',
        'max_altitude_m',
        'payload_kg',
        'current_battery_voltage',
        'predicted_battery_voltage',
        'predicted_energy_consumption_wh',
        'delay_seconds',
        'energy_increase_percentage',
        'risk_score'
    ]
    
    CATEGORICAL_FEATURES = [
        'weather_condition',
        'risk_level',
        'traffic_condition',
        'battery_level',
        'payload_level'
    ]
    
    # Risk levels
    RISK_LEVELS = {
        'LOW': 0.3,
        'MEDIUM': 0.6,
        'HIGH': 0.9
    }
    
    # Battery levels
    BATTERY_LEVELS = {
        'LOW': 20,
        'MEDIUM': 24,
        'HIGH': 28
    }
    
    # Payload levels
    PAYLOAD_LEVELS = {
        'LIGHT': 5,
        'MEDIUM': 10,
        'HEAVY': 20
    }
    
    # Weather conditions
    WEATHER_CONDITIONS = [
        'CLEAR',
        'CLOUDY',
        'RAINY',
        'SNOWY',
        'STORM'
    ]
    
    # Traffic conditions
    TRAFFIC_CONDITIONS = [
        'LIGHT',
        'MODERATE',
        'HEAVY',
        'JAM'
    ]
    
    @classmethod
    def get_risk_level(cls, score):
        """Get risk level based on score"""
        if score < cls.RISK_LEVELS['LOW']:
            return 'LOW'
        elif score < cls.RISK_LEVELS['MEDIUM']:
            return 'MEDIUM'
        else:
            return 'HIGH'
    
    @classmethod
    def get_battery_level(cls, voltage):
        """Get battery level based on voltage"""
        if voltage <= cls.BATTERY_LEVELS['LOW']:
            return 'LOW'
        elif voltage <= cls.BATTERY_LEVELS['MEDIUM']:
            return 'MEDIUM'
        else:
            return 'HIGH'
    
    @classmethod
    def get_payload_level(cls, payload):
        """Get payload level based on weight"""
        if payload <= cls.PAYLOAD_LEVELS['LIGHT']:
            return 'LIGHT'
        elif payload <= cls.PAYLOAD_LEVELS['MEDIUM']:
            return 'MEDIUM'
        else:
            return 'HEAVY'