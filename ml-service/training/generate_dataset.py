import os
import random
import numpy as np
import pandas as pd

RANDOM_SEED = 42
NUM_SAMPLES = 10000

def set_seed(seed=RANDOM_SEED):
    random.seed(seed)
    np.random.seed(seed)

def generate_synthetic_dataset():
    set_seed(RANDOM_SEED)

    print(f"Generating synthetic recommendation dataset ({NUM_SAMPLES} samples)...")

    # Define Careers & Required Skills with priority (1.0=CRITICAL, 0.75=HIGH, 0.5=MEDIUM, 0.25=LOW)
    careers = {
        "Java Backend Developer": {
            "skills": {
                "Java": (1.0, True),
                "OOP": (0.75, False),
                "Data Structures & Algorithms": (0.75, False),
                "SQL": (0.75, False),
                "Spring Boot": (1.0, True),
                "REST APIs": (1.0, True),
                "JPA/Hibernate": (0.75, False),
                "Spring Security": (0.5, False),
                "Testing": (0.5, False),
                "Docker": (0.5, False)
            }
        },
        "Frontend Developer": {
            "skills": {
                "HTML": (1.0, True),
                "CSS": (1.0, True),
                "JavaScript": (1.0, True),
                "TypeScript": (0.75, False),
                "React": (1.0, True),
                "Git": (0.75, False),
                "REST APIs": (0.75, False),
                "Testing": (0.5, False)
            }
        },
        "Full Stack Developer": {
            "skills": {
                "Java": (0.75, False),
                "Spring Boot": (0.75, True),
                "JavaScript": (0.75, True),
                "React": (0.75, True),
                "SQL": (0.75, False),
                "REST APIs": (1.0, True),
                "Git": (0.75, False),
                "Docker": (0.5, False)
            }
        },
        "Data Scientist": {
            "skills": {
                "Python": (1.0, True),
                "SQL": (1.0, True),
                "Statistics": (1.0, True),
                "Pandas": (0.75, False),
                "NumPy": (0.75, False),
                "Data Visualization": (0.75, False),
                "Machine Learning": (0.75, False)
            }
        },
        "Machine Learning Engineer": {
            "skills": {
                "Python": (1.0, True),
                "Machine Learning": (1.0, True),
                "Deep Learning": (1.0, True),
                "TensorFlow/PyTorch": (0.75, False),
                "Data Structures & Algorithms": (0.75, False),
                "MLOps": (0.75, False),
                "Docker": (0.5, False),
                "SQL": (0.5, False)
            }
        }
    }

    experience_levels = ["BEGINNER", "INTERMEDIATE", "ADVANCED"]
    content_types = ["VIDEO", "INTERACTIVE", "TEXT", "PROJECT"]

    records = []

    for i in range(NUM_SAMPLES):
        # 1. Randomly sample learner background
        learner_exp = np.random.choice(experience_levels, p=[0.45, 0.40, 0.15])
        pref_content = np.random.choice(content_types)
        pref_free = np.random.choice([True, False], p=[0.6, 0.4])

        # 2. Select target career
        career_name = random.choice(list(careers.keys()))
        career_info = careers[career_name]
        career_skills = career_info["skills"]

        # 3. Simulate learner current skill proficiencies (0.0 to 1.0)
        # Beginners have low proficiencies, advanced have higher
        exp_bias = 0.2 if learner_exp == "BEGINNER" else (0.5 if learner_exp == "INTERMEDIATE" else 0.8)
        learner_proficiencies = {
            s: float(np.clip(np.random.normal(loc=exp_bias, scale=0.25), 0.0, 1.0))
            for s in career_skills.keys()
        }

        # 4. Simulate candidate course
        num_taught_skills = random.randint(1, min(4, len(career_skills)))
        taught_skills = random.sample(list(career_skills.keys()), num_taught_skills)
        course_diff = np.random.choice(experience_levels, p=[0.4, 0.4, 0.2])
        course_type = np.random.choice(content_types)
        course_is_free = np.random.choice([True, False], p=[0.4, 0.6])
        course_rating = float(np.clip(np.random.normal(4.3, 0.4), 3.0, 5.0))
        course_duration_h = random.randint(5, 60)

        # 5. Compute Normalized Features (0.0 to 1.0)

        # Skill Gap Score & Coverage
        missing_skills = [s for s, prof in learner_proficiencies.items() if prof < 0.6]
        gap_skills_taught = [s for s in taught_skills if learner_proficiencies[s] < 0.6]

        total_gaps = max(1, len(missing_skills))
        skill_coverage = len(gap_skills_taught) / total_gaps

        gap_weights_sum = sum(career_skills[s][0] * (1.0 - learner_proficiencies[s]) for s in gap_skills_taught)
        max_possible_gap_weights = sum(career_skills[s][0] for s in missing_skills) if missing_skills else 1.0
        skill_gap_score = np.clip(gap_weights_sum / max_possible_gap_weights, 0.0, 1.0)

        # Career Priority Score
        taught_priorities = [career_skills[s][0] for s in taught_skills]
        career_priority_score = np.mean(taught_priorities) if taught_priorities else 0.0

        # Mandatory Skill Match
        mandatory_gaps = [s for s in missing_skills if career_skills[s][1]]
        mandatory_taught = [s for s in gap_skills_taught if career_skills[s][1]]
        mandatory_skill_match = (len(mandatory_taught) / len(mandatory_gaps)) if mandatory_gaps else 1.0

        # Proficiency Gap Match
        prof_gaps = [1.0 - learner_proficiencies[s] for s in taught_skills]
        proficiency_gap = float(np.mean(prof_gaps)) if prof_gaps else 0.0

        # Difficulty Match
        level_map = {"BEGINNER": 1, "INTERMEDIATE": 2, "ADVANCED": 3}
        diff_delta = abs(level_map[learner_exp] - level_map[course_diff])
        difficulty_match = 1.0 if diff_delta == 0 else (0.75 if diff_delta == 1 else 0.4)

        # Normalized Course Rating (3.0..5.0 -> 0.0..1.0)
        course_rating_norm = (course_rating - 3.0) / 2.0

        # Preference Match
        content_match = 1.0 if course_type == pref_content else 0.0
        price_match = 1.0 if course_is_free == pref_free else 0.5
        preference_match = 0.6 * content_match + 0.4 * price_match

        # Course Duration Match (15h..40h optimal = 1.0)
        if 15 <= course_duration_h <= 40:
            course_duration_match = 1.0
        elif course_duration_h < 15:
            course_duration_match = 0.7 + (course_duration_h / 15.0) * 0.3
        else:
            course_duration_match = max(0.5, 1.0 - (course_duration_h - 40) / 40.0)

        # Course Quality Score
        course_quality_score = 0.7 * course_rating_norm + 0.3 * course_duration_match

        # 6. Calculate Underlying Recommendation Score with Gaussian Noise
        raw_score = (
            0.25 * skill_gap_score +
            0.20 * career_priority_score +
            0.15 * mandatory_skill_match +
            0.12 * skill_coverage +
            0.10 * difficulty_match +
            0.08 * course_quality_score +
            0.05 * preference_match +
            0.05 * proficiency_gap
        )

        # Add Gaussian noise (sigma = 0.08)
        noise = np.random.normal(loc=0.0, scale=0.08)
        noisy_score = raw_score + noise

        # Sigmoid probability transform
        prob = 1.0 / (1.0 + np.exp(-12.0 * (noisy_score - 0.52)))
        recommendation_label = 1 if prob >= 0.5 else 0

        records.append({
            "skill_gap_score": round(float(skill_gap_score), 4),
            "career_priority_score": round(float(career_priority_score), 4),
            "skill_coverage": round(float(skill_coverage), 4),
            "proficiency_gap": round(float(proficiency_gap), 4),
            "difficulty_match": round(float(difficulty_match), 4),
            "course_rating": round(float(course_rating_norm), 4),
            "preference_match": round(float(preference_match), 4),
            "mandatory_skill_match": round(float(mandatory_skill_match), 4),
            "course_duration_match": round(float(course_duration_match), 4),
            "course_quality_score": round(float(course_quality_score), 4),
            "recommendation_label": recommendation_label
        })

    df = pd.DataFrame(records)

    # Output directory
    output_dir = os.path.join(os.path.dirname(__file__), "..", "data")
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, "training_data.csv")

    df.to_csv(output_path, index=False)
    print(f"Dataset successfully saved to: {os.path.abspath(output_path)}\n")

    # Dataset Quality Verification & Descriptive Statistics
    print("=" * 60)
    print("DATASET STATISTICS & VERIFICATION")
    print("=" * 60)
    print(f"Dataset Shape: {df.shape}")
    print(f"Missing Values: {df.isnull().sum().sum()}")
    print(f"Infinite Values: {np.isinf(df.values).sum()}")

    pos_count = (df['recommendation_label'] == 1).sum()
    neg_count = (df['recommendation_label'] == 0).sum()
    pos_rate = (pos_count / len(df)) * 100.0

    print(f"Positive Labels (1): {pos_count}")
    print(f"Negative Labels (0): {neg_count}")
    print(f"Positive Label Rate: {pos_rate:.2f}%\n")

    print("Feature Summary:")
    print(df.describe().T[['min', 'mean', 'max', 'std']])
    print("=" * 60)

if __name__ == "__main__":
    generate_synthetic_dataset()
