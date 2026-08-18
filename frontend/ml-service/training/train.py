import pandas as pd
import numpy as np
import sklearn
import joblib

def verify_environment():
    print("Verifying ML training environment dependencies...")
    print(f"Pandas version: {pd.__version__}")
    print(f"NumPy version: {np.__version__}")
    print(f"Scikit-Learn version: {sklearn.__version__}")
    print(f"Joblib version: {joblib.__version__}")
    print("ML training environment is ready.")

if __name__ == "__main__":
    verify_environment()
