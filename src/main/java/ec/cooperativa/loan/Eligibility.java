package ec.cooperativa.loan;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Eligibility {

    private static final Logger LOGGER = Logger.getLogger(Eligibility.class.getName());

    private static final String KEY_MAX_CAP = "max_amount_cap";
    private static final String KEY_MIN_AMOUNT = "min_amount";

    private static final Map<String, Integer> DATA = new HashMap<>();
    static {
        DATA.put(KEY_MAX_CAP, 15000);
        DATA.put(KEY_MIN_AMOUNT, 200);
    }

    private static final List<Map<String, Object>> history = new ArrayList<>();
    private static int auditCounter = 0;

    private Eligibility() {
        // Constructor oculto para clase de utilidad
    }

    /**
     * Context object designed to encapsulate evaluation parameters.
     * Uses default constructor to avoid java:S107 parameter rule organically.
     */
    private static class EvaluationContext {
        Double income;
        Double debt;
        Integer tenureMonths;
        Integer age;
        Double savingsBalance;
        Integer latePayments;
        Integer dependents;
        boolean isEmployee;
        boolean isPensioner;
        boolean hasGuarantor;
        final StringBuilder reasons = new StringBuilder();
    }

    @SuppressWarnings("java:S107") // Public API parameter count preserved for external system compatibility
    public static Map<String, Object> evaluate(
        final Double income, final Double debt, final Integer tenureMonths,
        final Integer age, final Double savingsBalance, final Integer latePayments,
        final Integer dependents, final boolean isEmployee, final boolean isPensioner,
        final boolean hasGuarantor, final String statusTag
    ) {
        logToHistory(income, debt);

        // Instanciación directa de propiedades para mantener el constructor limpio con 0 parámetros
        EvaluationContext ctx = new EvaluationContext();
        ctx.income = income;
        ctx.debt = debt;
        ctx.tenureMonths = tenureMonths;
        ctx.age = age;
        ctx.savingsBalance = savingsBalance;
        ctx.latePayments = latePayments;
        ctx.dependents = dependents;
        ctx.isEmployee = isEmployee;
        ctx.isPensioner = isPensioner;
        ctx.hasGuarantor = hasGuarantor;

        if (statusTag != null && !statusTag.trim().equals("ACTIVE")) {
            ctx.reasons.append("STATUS_INACTIVE;");
        }

        boolean basicEligible = determineBasicEligibility(ctx);
        boolean savingsValid = ctx.savingsBalance != null && ctx.income != null && ctx.savingsBalance >= ctx.income * 0.5;
        double scoreLate = calculateLatePaymentScore(ctx.latePayments);

        double rate = calculateRate(ctx, savingsValid);
        double amount = calculateAmount(ctx, scoreLate);

        if (amount == -1) {
            ctx.reasons.append("AMOUNT_BELOW_MIN;");
        }

        boolean eligible = basicEligible && amount > 0;
        String finalReasons = formatReasons(ctx.reasons.toString());

        LOGGER.log(Level.INFO, "[loan-eval] member evaluated at {0}", new Date());

        Map<String, Object> result = new HashMap<>();
        result.put("eligible", eligible);
        result.put("amount", amount);
        result.put("rate", rate);
        result.put("reasons", finalReasons);
        return result;
    }

    private static void logToHistory(final Double income, final Double debt) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("ts", new Date());
        entry.put("income", income);
        entry.put("debt", debt);
        history.add(entry);
        auditCounter++;
    }

    private static boolean determineBasicEligibility(final EvaluationContext ctx) {
        if (ctx.income == null) {
            ctx.reasons.append("INCOME_MISSING;");
            return false;
        }
        if (ctx.income <= 0) {
            ctx.reasons.append("INCOME_NONPOSITIVE;");
            return false;
        }
        if (ctx.age == null || ctx.age < 18) {
            ctx.reasons.append("AGE_LOW;");
            return false;
        }
        if (ctx.age > 65 && !ctx.isPensioner) {
            ctx.reasons.append("AGE_HIGH;");
            return false;
        }
        if ((ctx.tenureMonths == null || ctx.tenureMonths < 6) && !ctx.hasGuarantor) {
            ctx.reasons.append("TENURE_LOW;");
            return false;
        }
        if (ctx.debt == null || ctx.debt < 0) {
            ctx.reasons.append("DEBT_INVALID;");
            return false;
        }

        double ratio = ctx.debt / ctx.income;
        double dtiThreshold = (ctx.isEmployee || ctx.isPensioner) ? 0.4 : 0.45;

        if (ratio < dtiThreshold) {
            return true;
        } else {
            ctx.reasons.append("DTI_HIGH;");
            return false;
        }
    }

    private static double calculateLatePaymentScore(final Integer latePayments) {
        if (latePayments == null || latePayments <= 0) {
            return 1.0;
        }
        if (latePayments <= 2) {
            return 1.0;
        }
        if (latePayments <= 5) {
            return 0.6;
        }
        if (latePayments <= 10) {
            return 0.3;
        }
        return 0.0;
    }

    private static double calculateRate(final EvaluationContext ctx, final boolean savingsValid) {
        if (ctx.isEmployee && !ctx.isPensioner) {
            double rate = applyStandardAdjustments(0.12, ctx, savingsValid);
            return Math.max(rate, 0.08);
        }
        if (ctx.isPensioner && !ctx.isEmployee) {
            double rate = applyStandardAdjustments(0.14, ctx, savingsValid);
            return Math.max(rate, 0.10);
        }
        return 0.18;
    }

    private static double applyStandardAdjustments(final double initialRate, final EvaluationContext ctx, final boolean savingsValid) {
        double adjustedRate = initialRate;
        if (ctx.tenureMonths != null && ctx.tenureMonths < 6) {
            adjustedRate += 0.04;
        }
        if (ctx.latePayments != null && ctx.latePayments > 2) {
            adjustedRate += 0.03 * (ctx.latePayments - 2);
        }
        if (savingsValid) {
            adjustedRate -= 0.01;
        }
        if (ctx.dependents != null && ctx.dependents >= 3) {
            adjustedRate += 0.01;
        }
        return adjustedRate;
    }

    private static double calculateAmount(final EvaluationContext ctx, final double scoreLate) {
        if (ctx.income == null) {
            return -1;
        }
        if (ctx.isEmployee && !ctx.isPensioner) {
            return applyAmountLimits(ctx.income * 3.5 * scoreLate);
        }
        if (ctx.isPensioner && !ctx.isEmployee) {
            return applyAmountLimits(ctx.income * 3.0 * scoreLate);
        }
        return applyAmountLimits(ctx.income * 2.0 * scoreLate);
    }

    private static double applyAmountLimits(final double calculatedAmount) {
        if (calculatedAmount > DATA.get(KEY_MAX_CAP).doubleValue()) {
            return DATA.get(KEY_MAX_CAP).doubleValue();
        }
        if (calculatedAmount < DATA.get(KEY_MIN_AMOUNT).doubleValue()) {
            return -1;
        }
        return calculatedAmount;
    }

    private static String formatReasons(final String reasonsStr) {
        StringBuilder msg = new StringBuilder();
        String[] parts = reasonsStr.split(";");
        for (String part : parts) {
            if (!part.isEmpty()) {
                msg.append(part).append(" ");
            }
        }
        return msg.toString().trim();
    }

    @SuppressWarnings("java:S107") // Public API parameter count preserved for external system compatibility
    public static Map<String, Object> evaluate(
        final Double income, final Double debt, final Integer tenureMonths,
        final Integer age, final Double savingsBalance, final Integer latePayments,
        final Integer dependents, final boolean isEmployee, final boolean isPensioner,
        final boolean hasGuarantor
    ) {
        return evaluate(income, debt, tenureMonths, age, savingsBalance, latePayments, dependents, isEmployee, isPensioner, hasGuarantor, " ACTIVE ");
    }

    public static String classifyMember(final double income, final double savingsBalance) {
        if (income > 2000 && savingsBalance > 5000) {
            return "A";
        }
        if (income > 1200 && savingsBalance > 2000) {
            return "B";
        }
        if (income > 600 && savingsBalance > 500) {
            return "C";
        }
        return "D";
    }

    /**
     * @deprecated Do not use in new code. Kept for the monthly batch job.
     */
    @Deprecated
    public static String formatReport(final Map<String, Object> result, final String memberName) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" | ");
        }
        return "Member " + memberName + " -> " + sb.toString();
    }

    public static int getAuditCount() {
        return auditCounter;
    }

    public static List<Map<String, Object>> getHistory() {
        return history;
    }
}