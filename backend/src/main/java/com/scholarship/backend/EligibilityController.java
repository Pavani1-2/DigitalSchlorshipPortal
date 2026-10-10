
package com.scholarship.backend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        if (request == null) {
            return new ArrayList<>();
        }

        return catalogRepository.findAll()
                .stream()
                .map(s -> evaluate(s, request))
                .filter(EligibilityResult::isPotentialMatch)
                .sorted(Comparator.comparing(
                        EligibilityResult::getScholarshipName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }

    private EligibilityResult evaluate(
            ScholarshipCatalog scholarship,
            EligibilityRequest request) {

        String criteria = lower(scholarship.getEligibility());
        String category = lower(request.getCategory());
        String state = lower(request.getState());
        String course = lower(request.getCourse());

        List<String> reasons = new ArrayList<>();
        boolean match = true;

        // 1. Check minimum percentage mentioned in the criteria.
        Matcher marksMatcher = Pattern.compile(
                "(\\d{1,3}(?:\\.\\d+)?)\\s*%")
                .matcher(criteria);

        while (marksMatcher.find()) {
            double minimumMarks;

            try {
                minimumMarks = Double.parseDouble(marksMatcher.group(1));
            } catch (NumberFormatException ex) {
                continue;
            }

            if (minimumMarks <= 0 || minimumMarks > 100) {
                continue;
            }

            if (request.getPercentage() == null) {
                match = false;
                reasons.add("Enter your percentage to verify the minimum marks.");
                break;
            }

            if (request.getPercentage() < minimumMarks) {
                match = false;
                reasons.add("Your percentage is below the stated minimum of "
                        + minimumMarks + "%.");
                break;
            }
        }

        // 2. Check an income limit stated in the eligibility text.
        Matcher incomeMatcher = Pattern.compile(
                "(?:annual\\s+family\\s+income|family\\s+income|annual\\s+income|income)"
                + "[^0-9]{0,40}(?:rs\\.?\\s*|inr\\s*|₹\\s*)?"
                + "([0-9][0-9,]*)",
                Pattern.CASE_INSENSITIVE)
                .matcher(criteria);

        if (incomeMatcher.find()) {
            try {
                double incomeLimit = Double.parseDouble(
                        incomeMatcher.group(1).replace(",", ""));

                if (incomeLimit > 0) {
                    if (request.getAnnualFamilyIncome() == null) {
                        match = false;
                        reasons.add("Enter annual family income to verify the limit.");
                    } else if (request.getAnnualFamilyIncome() > incomeLimit) {
                        match = false;
                        reasons.add("Annual family income exceeds the stated limit.");
                    } else {
                        reasons.add("Annual family income is within the detected limit.");
                    }
                }
            } catch (NumberFormatException ignored) {
                // Ignore an income value that cannot be parsed.
            }
        }

        // 3. Check explicit Telangana restriction.
        if (criteria.contains("telangana")
                && !state.isBlank()
                && !state.contains("telangana")) {
            match = false;
            reasons.add("This criterion mentions Telangana, but your selected state differs.");
        }

        // 4. Check explicit SC/ST-only restrictions.
        boolean scOnly = criteria.contains("sc/st")
                || criteria.contains("sc or st")
                || criteria.contains("scheduled caste")
                || criteria.contains("scheduled tribe");

        if (scOnly && criteria.contains("only") && !category.isBlank()) {
            boolean categoryMatches =
                    (category.equals("sc")
                            && (criteria.contains("sc")
                            || criteria.contains("scheduled caste")))
                    || (category.equals("st")
                            && (criteria.contains("st")
                            || criteria.contains("scheduled tribe")));

            if (!categoryMatches) {
                match = false;
                reasons.add("The stated criteria may restrict this scholarship to SC/ST categories.");
            }
        }

        // 5. Check explicit undergraduate/postgraduate restrictions.
        boolean asksForPG = criteria.contains("postgraduate")
                || criteria.contains("post graduate")
                || criteria.contains("post-graduate")
                || criteria.contains("post-graduation")
                || criteria.contains("master's degree")
                || criteria.contains("masters degree")
                || criteria.contains("m.tech")
                || criteria.contains("m.e.")
                || criteria.contains("mca")
                || criteria.contains("mba");

        boolean asksForUG = criteria.contains("undergraduate")
                || criteria.contains("under graduate")
                || criteria.contains("under-graduate")
                || criteria.contains("b.tech")
                || criteria.contains("b.e.")
                || criteria.contains("bachelor's degree")
                || criteria.contains("bachelors degree");

        boolean studentIsPG = course.contains("m.tech")
                || course.contains("m.e.")
                || course.equals("mca")
                || course.equals("mba")
                || course.contains("postgraduate")
                || course.contains("post graduate");

        boolean studentIsUG = course.contains("b.tech")
                || course.contains("b.e.")
                || course.contains("bachelor")
                || course.contains("undergraduate")
                || course.contains("under graduate");

        if (asksForPG && studentIsUG) {
            match = false;
            reasons.add("The scholarship criteria indicate postgraduate study.");
        } else if (asksForUG && studentIsPG) {
            match = false;
            reasons.add("The scholarship criteria indicate undergraduate study.");
        }

        if (reasons.isEmpty()) {
            reasons.add(
                    "No conflicting rule was detected in the stored criteria. Verify the official requirements.");
        }

        return new EligibilityResult(
                scholarship.getId(),
                scholarship.getName(),
                scholarship.getProvider(),
                scholarship.getAmount(),
                scholarship.getDeadline(),
                match,
                match
                        ? "Potential match — verify official criteria"
                        : "Not matched against the entered details",
                reasons);
    }

    private String lower(String value) {
        return Optional.ofNullable(value)
                .orElse("")
                .trim()
                .toLowerCase(Locale.ROOT);
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

        private Long scholarshipId;
        private String scholarshipName;
        private String provider;
        private BigDecimal amount;
        private LocalDate deadline;
        private boolean potentialMatch;
        private String result;
        private List<String> reasons;

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
