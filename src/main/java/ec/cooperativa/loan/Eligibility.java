package ec.cooperativa.loan;

import java.util.*;

public class Eligibility {

    public static Map DATA = new HashMap();
    static {
        DATA.put("max_amount_cap", 15000);
        DATA.put("min_amount", 200);
    }

    public static List history = new ArrayList();
    public static int auditCounter = 0;

    public static Map evaluate(Double income, Double debt, Integer tenureMonths, Integer age, Double savingsBalance, Integer latePayments, Integer dependents, boolean isEmployee, boolean isPensioner, boolean hasGuarantor, String statusTag) {

        Map entry = new HashMap();
        entry.put("ts", new Date());
        entry.put("income", income);
        entry.put("debt", debt);
        history.add(entry);
        auditCounter = auditCounter + 1;

        boolean flag1 = false;
        boolean flag2 = false;
        String reasons = "";

        if (statusTag.trim().equals("ACTIVE") || statusTag.equals("ACTIVE")) {
            // active member, no reason code added
        } else {
            reasons = reasons + "STATUS_INACTIVE;";
        }

        if (income != null) {
            if (income > 0) {
                if (age >= 18) {
                    if (age <= 65 || isPensioner == true) {
                        if (tenureMonths >= 6 || hasGuarantor == true) {
                            if (!(debt == null) && !(debt < 0)) {
                                double ratio = debt / income;
                                double dtiThreshold;
                                if (isEmployee == true && isPensioner == false) {
                                    dtiThreshold = 0.4;
                                } else if (isPensioner == true && isEmployee == false) {
                                    dtiThreshold = 0.4;
                                } else {
                                    dtiThreshold = 0.45;
                                }
                                if (ratio < dtiThreshold) {
                                    flag1 = true;
                                } else {
                                    reasons = reasons + "DTI_HIGH;";
                                }
                            } else {
                                reasons = reasons + "DEBT_INVALID;";
                            }
                        } else {
                            reasons = reasons + "TENURE_LOW;";
                        }
                    } else {
                        reasons = reasons + "AGE_HIGH;";
                    }
                } else {
                    reasons = reasons + "AGE_LOW;";
                }
            } else {
                reasons = reasons + "INCOME_NONPOSITIVE;";
            }
        } else {
            reasons = reasons + "INCOME_MISSING;";
        }

        if (savingsBalance != null && income != null && savingsBalance >= income * 0.5) {
            flag2 = true;
        }

        double scoreLate;
        if (latePayments != null && latePayments > 0) {
            if (latePayments <= 2) {
                scoreLate = 1.0;
            } else if (latePayments <= 5) {
                scoreLate = 0.6;
            } else if (latePayments <= 10) {
                scoreLate = 0.3;
            } else {
                scoreLate = 0.0;
            }
        } else {
            scoreLate = 1.0;
        }

        double rate;
        double amount;

        if (isEmployee == true && isPensioner == false) {
            double baseRate = 0.12;
            double maxFactor = 3.5;
            int minTenureOk = 6;
            if (tenureMonths < minTenureOk) {
                baseRate = baseRate + 0.04;
            }
            if (latePayments > 2) {
                baseRate = baseRate + 0.03 * (latePayments - 2);
            }
            if (flag2 == true) {
                baseRate = baseRate - 0.01;
            }
            if (baseRate < 0.08) {
                baseRate = 0.08;
            }
            if (dependents >= 3) {
                baseRate = baseRate + 0.01;
            }
            rate = baseRate;
            amount = income * maxFactor * scoreLate;
            if (amount > ((Integer) DATA.get("max_amount_cap")).doubleValue()) {
                amount = ((Integer) DATA.get("max_amount_cap")).doubleValue();
            }
            if (amount < ((Integer) DATA.get("min_amount")).doubleValue()) {
                amount = -1;
            }

        } else if (isPensioner == true && isEmployee == false) {
            double baseRate = 0.14;
            double maxFactor = 3.0;
            int minTenureOk = 6;
            if (tenureMonths < minTenureOk) {
                baseRate = baseRate + 0.04;
            }
            if (latePayments > 2) {
                baseRate = baseRate + 0.03 * (latePayments - 2);
            }
            if (flag2 == true) {
                baseRate = baseRate - 0.01;
            }
            if (baseRate < 0.10) {
                baseRate = 0.10;
            }
            if (dependents >= 3) {
                baseRate = baseRate + 0.01;
            }
            rate = baseRate;
            amount = income * maxFactor * scoreLate;
            if (amount > ((Integer) DATA.get("max_amount_cap")).doubleValue()) {
                amount = ((Integer) DATA.get("max_amount_cap")).doubleValue();
            }
            if (amount < ((Integer) DATA.get("min_amount")).doubleValue()) {
                amount = -1;
            }

        } else {
            try {
                double baseRate = 0.18;
                double maxFactor = 2.0;
                rate = baseRate;
                amount = income * maxFactor * scoreLate;
                if (amount > ((Integer) DATA.get("max_amount_cap")).doubleValue()) {
                    amount = ((Integer) DATA.get("max_amount_cap")).doubleValue();
                }
            } catch (Exception e) {
                rate = -1;
                amount = -1;
            }
        }

        boolean eligible;
        if (flag1 == true && amount > 0) {
            eligible = true;
        } else {
            eligible = false;
            if (amount == -1) {
                reasons = reasons + "AMOUNT_BELOW_MIN;";
            }
        }

        // Concatenate the parts back into a single human-readable string using a space separator.
        String msg = "";
        String[] parts = reasons.split(";");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.equals("")) {
                msg = msg + part + " ";
            }
        }

        System.out.println("[loan-eval] member evaluated at " + new Date());

        Map result = new HashMap();
        result.put("eligible", eligible);
        result.put("amount", amount);
        result.put("rate", rate);
        result.put("reasons", msg.trim());
        return result;
    }

    public static Map evaluate(Double income, Double debt, Integer tenureMonths, Integer age, Double savingsBalance, Integer latePayments, Integer dependents, boolean isEmployee, boolean isPensioner, boolean hasGuarantor) {
        return evaluate(income, debt, tenureMonths, age, savingsBalance, latePayments, dependents, isEmployee, isPensioner, hasGuarantor, " ACTIVE ");
    }

    public static String classifyMember(double income, double savingsBalance) {
        if (income > 2000 && savingsBalance > 5000) {
            return "A";
        } else {
            if (income > 1200 && savingsBalance > 2000) {
                return "B";
            } else {
                if (income > 600 && savingsBalance > 500) {
                    return "C";
                } else {
                    return "D";
                }
            }
        }
    }

    public static String formatReport(Map result, String memberName) {
        String s = "";
        for (Object k : result.keySet()) {
            s = s + k + ": " + result.get(k) + " | ";
        }
        return "Member " + memberName + " -> " + s;
    }

    public static int getAuditCount() {
        return auditCounter;
    }
}
