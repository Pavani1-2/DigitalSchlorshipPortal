
async function checkEligibility() {
  setLoading(true);
  setError("");

  try {
    const studyYearValue = String(
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
      throw new Error("Enter a percentage between 0 and 100.");
    }

    if (
      eligibilityForm.annualFamilyIncome === "" ||
      !Number.isFinite(incomeValue) ||
      incomeValue < 0
    ) {
      throw new Error("Enter a valid annual family income.");
    }

    if (!eligibilityForm.category || !eligibilityForm.state ||
        !eligibilityForm.course || !studyYearValue) {
      throw new Error("Please fill in all student details.");
    }

    const requestData = {
      category: eligibilityForm.category,
      state: eligibilityForm.state,
      percentage: percentageValue,
      annualFamilyIncome: incomeValue,
      course: eligibilityForm.course,
      studyYear: Number(studyYearValue[0])
    };

    const result = await api("/api/eligibility/check", {
      method: "POST",
      body: JSON.stringify(requestData)
    });

    const resultsList = Array.isArray(result)
      ? result
      : Array.isArray(result?.results)
        ? result.results
        : [];

    setEligibilityResults(resultsList);
    setEligibilityChecked(true);
  } catch (err) {
    setError(err.message || "Eligibility check failed.");
    setEligibilityResults([]);
    setEligibilityChecked(false);
  } finally {
    setLoading(false);
  }
}
