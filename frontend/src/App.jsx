import { useState } from "react";

const API_URL = "http://localhost:8080";

function App() {
  const [eligibilityForm, setEligibilityForm] = useState({
    category: "",
    state: "",
    percentage: "",
    annualFamilyIncome: "",
    course: "",
    studyYear: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [eligibilityResults, setEligibilityResults] = useState([]);
  const [eligibilityChecked, setEligibilityChecked] = useState(false);

  async function api(path, options = {}) {
    const response = await fetch(`${API_URL}${path}`, {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      },
      ...options
    });

    if (!response.ok) {
      throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();
  }

  async function checkEligibility() {
    setLoading(true);
    setError("");

    try {
      const yearMatch = String(
        eligibilityForm.studyYear ?? ""
      ).match(/\d+/);

      const percentageValue = Number(eligibilityForm.percentage);
      const incomeValue = Number(eligibilityForm.annualFamilyIncome);

      if (
        eligibilityForm.percentage === "" ||
        !Number.isFinite(percentageValue) ||
        percentageValue < 0 ||
        percentageValue > 100
      ) {
        throw new Error("Enter percentage between 0 and 100.");
      }

      if (
        eligibilityForm.annualFamilyIncome === "" ||
        !Number.isFinite(incomeValue) ||
        incomeValue < 0
      ) {
        throw new Error("Enter a valid annual family income.");
      }

      if (
        !eligibilityForm.category ||
        !eligibilityForm.state ||
        !eligibilityForm.course ||
        !yearMatch
      ) {
        throw new Error("Please fill in all student details.");
      }

      const requestData = {
        category: eligibilityForm.category,
        state: eligibilityForm.state,
        percentage: percentageValue,
        annualFamilyIncome: incomeValue,
        course: eligibilityForm.course,
        studyYear: Number(yearMatch[0])
      };

      const response = await api("/api/eligibility/check", {
        method: "POST",
        body: JSON.stringify(requestData)
      });

      const results = Array.isArray(response)
        ? response
        : Array.isArray(response?.results)
          ? response.results
          : [];

      setEligibilityResults(results);
      setEligibilityChecked(true);
    } catch (error) {
      setError(error.message || "Eligibility check failed.");
      setEligibilityResults([]);
      setEligibilityChecked(false);
    } finally {
      setLoading(false);
    }
  }

  function updateField(event) {
    const { name, value } = event.target;

    setEligibilityForm((previous) => ({
      ...previous,
      [name]: value
    }));
  }

  return (
    <main style={{
      maxWidth: "850px",
      margin: "30px auto",
      padding: "24px",
      fontFamily: "Arial, sans-serif"
    }}>
      <h1>Digital Scholarship Eligibility Portal</h1>
      <p>Enter your details to check potential scholarship matches.</p>

      <form
        onSubmit={(event) => {
          event.preventDefault();
          checkEligibility();
        }}
        style={{
          display: "grid",
          gap: "14px",
          maxWidth: "520px"
        }}
      >
        <label>
          Category
          <select
            name="category"
            value={eligibilityForm.category}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          >
            <option value="">Select category</option>
            <option value="SC">SC</option>
            <option value="ST">ST</option>
            <option value="OBC">OBC</option>
            <option value="BC">BC</option>
            <option value="General">General</option>
          </select>
        </label>

        <label>
          State
          <select
            name="state"
            value={eligibilityForm.state}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          >
            <option value="">Select state</option>
            <option value="Telangana">Telangana</option>
            <option value="Andhra Pradesh">Andhra Pradesh</option>
            <option value="Other">Other</option>
          </select>
        </label>

        <label>
          Percentage
          <input
            type="number"
            name="percentage"
            min="0"
            max="100"
            step="any"
            value={eligibilityForm.percentage}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          />
        </label>

        <label>
          Annual Family Income (₹)
          <input
            type="number"
            name="annualFamilyIncome"
            min="0"
            value={eligibilityForm.annualFamilyIncome}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          />
        </label>

        <label>
          Course
          <select
            name="course"
            value={eligibilityForm.course}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          >
            <option value="">Select course</option>
            <option value="B.Tech ECE">B.Tech ECE</option>
            <option value="B.Tech Engineering">B.Tech Engineering</option>
            <option value="Degree">Degree</option>
            <option value="M.Tech">M.Tech</option>
            <option value="MBA">MBA</option>
            <option value="MCA">MCA</option>
          </select>
        </label>

        <label>
          Study Year
          <select
            name="studyYear"
            value={eligibilityForm.studyYear}
            onChange={updateField}
            required
            style={{ display: "block", width: "100%", padding: "10px" }}
          >
            <option value="">Select year</option>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((year) => (
              <option key={year} value={year}>
                Year {year}
              </option>
            ))}
          </select>
        </label>

        <button
          type="submit"
          disabled={loading}
          style={{
            padding: "12px",
            background: "#155eef",
            color: "white",
            border: 0,
            borderRadius: "6px",
            cursor: "pointer"
          }}
        >
          {loading ? "Checking..." : "Check Eligibility"}
        </button>
      </form>

      {error && (
        <p style={{ color: "red" }}>{error}</p>
      )}

      {eligibilityChecked && (
        <section>
          <h2>Eligibility Results</h2>

          {eligibilityResults.length === 0 ? (
            <p>No potential matches found. Check the official scholarship criteria.</p>
          ) : (
            eligibilityResults.map((item, index) => (
              <article
                key={item.scholarshipId ?? index}
                style={{
                  border: "1px solid #ddd",
                  borderRadius: "8px",
                  padding: "16px",
                  marginBottom: "12px"
                }}
              >
                <h3>{item.scholarshipName}</h3>
                <p>Provider: {item.provider || "Not specified"}</p>
                <p>Amount: {item.amount ?? "Not specified"}</p>
                <p>Deadline: {item.deadline || "Not specified"}</p>
                <p>{item.result}</p>

                {Array.isArray(item.reasons) && (
                  <ul>
                    {item.reasons.map((reason, i) => (
                      <li key={i}>{reason}</li>
                    ))}
                  </ul>
                )}
              </article>
            ))
          )}
        </section>
      )}
    </main>
  );
}

export default App;
