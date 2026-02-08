import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error
import joblib
import os

def load_data():
    # Load training data (replace with actual data loading)
    data = {
        'weather': ['CLEAR', 'CLOUDY', 'RAINY', 'SNOWY', 'STORM', 'CLEAR', 'CLOUDY', 'RAINY', 'SNOWY', 'STORM'],
        'traffic': ['LIGHT', 'MODERATE', 'HEAVY', 'JAM', 'LIGHT', 'MODERATE', 'HEAVY', 'JAM', 'LIGHT', 'MODERATE'],
        'battery': ['HIGH', 'MEDIUM', 'LOW', 'HIGH', 'MEDIUM', 'LOW', 'HIGH', 'MEDIUM', 'LOW', 'HIGH'],
        'payload': ['LIGHT', 'MEDIUM', 'HEAVY', 'LIGHT', 'MEDIUM', 'HEAVY', 'LIGHT', 'MEDIUM', 'HEAVY', 'LIGHT'],
        'risk_score': [0.1, 0.2, 0.5, 0.7, 0.9, 0.15, 0.25, 0.55, 0.75, 0.95]
    }
    df = pd.DataFrame(data)
    return df

def preprocess_data(df):
    # Convert categorical features to numerical
    df_encoded = pd.get_dummies(df, columns=['weather', 'traffic', 'battery', 'payload'])
    return df_encoded

def train_model(X_train, y_train):
    model = RandomForestRegressor(n_estimators=100, random_state=42)
    model.fit(X_train, y_train)
    return model

def evaluate_model(model, X_test, y_test):
    y_pred = model.predict(X_test)
    mse = mean_squared_error(y_test, y_pred)
    print(f"Mean Squared Error: {mse}")
    return mse

def save_model(model, filepath):
    joblib.dump(model, filepath)
    print(f"Model saved to {filepath}")

def main():
    # Load and preprocess data
    df = load_data()
    df_encoded = preprocess_data(df)

    # Split data into features and target
    X = df_encoded.drop('risk_score', axis=1)
    y = df_encoded['risk_score']

    # Split into train and test sets
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Train model
    model = train_model(X_train, y_train)

    # Evaluate model
    mse = evaluate_model(model, X_test, y_test)

    # Save model
    if not os.path.exists('models'):
        os.makedirs('models')
    save_model(model, 'models/risk_model.joblib')

    print("Training completed successfully")

if __name__ == "__main__":
    main()