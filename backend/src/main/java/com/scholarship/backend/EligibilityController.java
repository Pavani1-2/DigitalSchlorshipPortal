package com.scholarship.backend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eligibility")
@CrossOrigin(origins = "http://localhost:5173")
public class EligibilityController {

    private final ScholarshipCatalogRepository catalogRepository;

    public EligibilityController(
            ScholarshipCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @PostMapping("/check")
    public List<EligibilityResult> check(
            @RequestBody EligibilityRequest request) {

        List<EligibilityResult> results = new ArrayList<>();

        if (request == null) {
            return results;
        }

        for (ScholarshipCatalog scholarship : catalogRepository.findAll()) {
            EligibilityResult result = evaluate(scholarship, request);

            if (result.isPotentialMatch()) {
                results.add(result);
            }
        }

        results.sort(
            Comparator.comparing(
                EligibilityResult::getScholarshipName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            )
        );

        return results;
    }

    private EligibilityResult evaluate(
            ScholarshipCatalog scholarship,
            EligibilityRequest request) {

        String criteria = normalize(scholarship.getEligibility());
        String category = normalize(request.getCategory());
        String state = normalize(request.getState());
        String course = normalize(request.getCourse());

        List<String> reasons = new ArrayList<>();
        boolean match = true;

        // 1. Percentage validation and eligibility
        Double percentage = request.getPercentage();

        if (percentage != null) {
            if (percentage < 0 || percentage > 100) {
                match = false;
                reasons.add("Percentage must be between 0 and 100.");
            } else {
                Double minimumMarks = findMinimumPercentage(criteria);

                if (minimumMarks != null) {
                    if (percentage < minimumMarks) {
                        match = false;
                        reasons.add(
                            "Minimum required percentage is "
                            + minimumMarks + "%."
                        );
                    } else {
                        reasons.add(
                            "Percentage meets the detected minimum of "
                            + minimumMarks + "%."
                        );
                    }
                }
            }
        }

        // 2. Annual family income
        Double income = request.getAnnualFamilyIncome();

        if (income != null) {
            if (income < 0) {
                match = false;
                reasons.add("Annual family income cannot be negative.");
            } else {
                Double incomeLimit = findIncomeLimit(criteria);

                if (incomeLimit != null) {
                    if (income > incomeLimit) {
                        match = false;
                        reasons.add(
                            "Annual family income exceeds the detected limit of Rs. "
                            + incomeLimit + "."
                        );
                    } else {
                        reasons.add(
                            "Annual family income is within the detected limit."
                        );
                    }
                }
            }
        }

        // 3. Category restrictions
        if (!category.isBlank()) {
            boolean scOnly = criteria.contains("sc only")
                    || criteria.contains("sc/st only")
                    || criteria.contains("scheduled caste only");

            boolean stOnly = criteria.contains("st only")
                    || criteria.contains("scheduled tribe only");

            boolean obcOnly = criteria.contains("obc only")
                    || criteria.contains("bc only")
                    || criteria.contains("backward class only");

            boolean generalOnly = criteria.contains("general category only")
                    || criteria.contains("general candidates only");

            if (scOnly && !category.equals("sc")) {
                match = false;
                reasons.add("This scholarship appears restricted to SC.");
            }

            if (stOnly && !category.equals("st")) {
                match = false;
                reasons.add("This scholarship appears restricted to ST.");
            }

            if (obcOnly
                    && !category.equals("obc")
                    && !category.equals("bc")) {
                match = false;
                reasons.add("This scholarship appears restricted to OBC/BC.");
            }

            if (generalOnly
                    && !category.equals("general")
                    && !category.equals("open")) {
                match = false;
                reasons.add("This scholarship appears restricted to General category.");
            }
        }

        // 4. State restrictions
        if (!state.isBlank()) {
            if (criteria.contains("telangana only")
                    && !state.equals("telangana")) {
                match = false;
                reasons.add("This scholarship appears restricted to Telangana.");
            }

            if (criteria.contains("andhra pradesh only")
                    && !state.equals("andhra pradesh")) {
                match = false;
                reasons.add("This scholarship appears restricted to Andhra Pradesh.");
            }
        }

        // 5. Course restrictions
        if (!course.isBlank()) {
            boolean studentIsPG = isPostgraduate(course);
            boolean studentIsUG = isUndergraduate(course);

            boolean ugOnly = criteria.contains("undergraduate only")
                    || criteria.contains("under graduate only")
                    || criteria.contains("ug students only");

            boolean pgOnly = criteria.contains("postgraduate only")
                    || criteria.contains("post graduate only")
                    || criteria.contains("pg students only");

            if (ugOnly && studentIsPG) {
                match = false;
                reasons.add(
                    "This scholarship appears restricted to undergraduate students."
                );
            }

            if (pgOnly && studentIsUG) {
                match = false;
                reasons.add(
                    "This scholarship appears restricted to postgraduate students."
                );
            }

            boolean eceOnly = criteria.contains("ece only")
                    || criteria.contains("electronics and communication engineering only");

            if (eceOnly
                    && !course.contains("ece")
                    && !course.contains("electronics and communication")) {
                match = false;
                reasons.add("This scholarship appears restricted to ECE.");
            }

            boolean engineeringOnly = criteria.contains("engineering only");

            if (engineeringOnly
                    && !course.contains("engineering")
                    && !course.contains("b.tech")
                    && !course.contains("btech")
                    && !course.contains("b.e")) {
                match = false;
                reasons.add("This scholarship appears restricted to engineering.");
            }
        }

        // 6. Study year restrictions
        Integer year = request.getStudyYear();

        if (year != null) {
            if (year < 1 || year > 8) {
                match = false;
                reasons.add("Study year must be between 1 and 8.");
            } else {
                Integer requiredYear = findRequiredStudyYear(criteria);

                if (requiredYear != null && !year.equals(requiredYear)) {
                    match = false;
                    reasons.add(
                        "This scholarship appears to require study year "
                        + requiredYear + "."
                    );
                }
            }
        }

        if (reasons.isEmpty()) {
            reasons.add(
                "No definite mismatch detected. Verify the official eligibility criteria."
            );
        }

        return new EligibilityResult(
            scholarship.getId(),
            scholarship.getName(),
            scholarship.getProvider(),
            scholarship.getAmount(),
            scholarship.getDeadline(),
            match,
            match ? "Potential match" : "Not eligible based on detected rules",
            reasons
        );
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private Double findMinimumPercentage(String criteria) {
        Pattern pattern = Pattern.compile(
            "(?:minimum|min\\.?|at least|not less than)"
            + "\\s*(\\d{1,3}(?:\\.\\d+)?)\\s*(?:%|percent)",
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(criteria);

        if (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));

            if (value >= 0 && value <= 100) {
                return value;
            }
        }

        return null;
    }

    private Double findIncomeLimit(String criteria) {
        Pattern pattern = Pattern.compile(
            "(?:annual family income|family income|annual income|"
            + "family's income|income limit|income should not exceed)"
            + "[^0-9]{0,35}(?:rs\\.?\\s*|inr\\s*|₹\\s*)?"
            + "([0-9][0-9,]*)",
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(criteria);

        if (matcher.find()) {
            try {
                return Double.valueOf(
                    matcher.group(1).replace(",", "")
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private Integer findRequiredStudyYear(String criteria) {
        Pattern pattern = Pattern.compile(
            "(?:only for|for students in|students studying in|"
            + "studying in|currently in|pursuing in)"
            + "\\s*(first|1st|second|2nd|third|3rd|fourth|4th)"
            + "\\s*(?:year)",
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(criteria);

        if (matcher.find()) {
            String year = matcher.group(1).toLowerCase(Locale.ROOT);

            return switch (year) {
                case "first", "1st" -> 1;
                case "second", "2nd" -> 2;
                case "third", "3rd" -> 3;
                case "fourth", "4th" -> 4;
                default -> null;
            };
        }

        return null;
    }

    private boolean isPostgraduate(String course) {
        return course.contains("m.tech")
                || course.contains("mtech")
                || course.contains("m.e.")
                || course.contains("mba")
                || course.contains("mca")
                || course.contains("m.sc")
                || course.contains("master");
    }

    private boolean isUndergraduate(String course) {
        return course.contains("b.tech")
                || course.contains("btech")
                || course.contains("b.e.")
                || course.contains("b.e ")
                || course.contains("bachelor")
                || course.contains("undergraduate")
                || course.equals("degree");
    }

    public static class EligibilityRequest {

        private String category;
        private String state;
        private Double percentage;
        private Double annualFamilyIncome;
        private String course;
        private Integer studyYear;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public Double getAnnualFamilyIncome() {
            return annualFamilyIncome;
        }

        public void setAnnualFamilyIncome(Double annualFamilyIncome) {
            this.annualFamilyIncome = annualFamilyIncome;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public Integer getStudyYear() {
            return studyYear;
        }

        public void setStudyYear(Integer studyYear) {
            this.studyYear = studyYear;
        }
    }

    public static class EligibilityResult {

        private final Long scholarshipId;
        private final String scholarshipName;
        private final String provider;
        private final BigDecimal amount;
        private final LocalDate deadline;
        private final boolean potentialMatch;
        private final String result;
        private final List<String> reasons;

        public EligibilityResult(
                Long scholarshipId,
                String scholarshipName,
                String provider,
                BigDecimal amount,
                LocalDate deadline,
                boolean potentialMatch,
                String result,
                List<String> reasons) {

            this.scholarshipId = scholarshipId;
            this.scholarshipName = scholarshipName;
            this.provider = provider;
            this.amount = amount;
            this.deadline = deadline;
            this.potentialMatch = potentialMatch;
            this.result = result;
            this.reasons = reasons;
        }

        public Long getScholarshipId() {
            return scholarshipId;
        }

        public String getScholarshipName() {
            return scholarshipName;
        }

        public String getProvider() {
            return provider;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDate getDeadline() {
            return deadline;
        }

        public boolean isPotentialMatch() {
            return potentialMatch;
        }

        public String getResult() {
            return result;
        }

        public List<String> getReasons() {
            return reasons;
        }
    }
}
