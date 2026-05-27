package ec.cooperativa.loan;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EligibilityTest {

    @Test
    void employeeEligibleBasic() {
        Map r = Eligibility.evaluate(1500.0, 400.0, 24, 30, 800.0, 0, 0, true, false, false);
        assertEquals(true, r.get("eligible"));
        assertTrue(((Double) r.get("amount")) > 0);
    }

    @Test
    void employeeHighDtiRejected() {
        Map r = Eligibility.evaluate(1000.0, 500.0, 24, 30, 200.0, 0, 0, true, false, false);
        assertEquals(false, r.get("eligible"));
        assertTrue(((String) r.get("reasons")).contains("DTI_HIGH"));
    }

    @Test
    void ageTooLow() {
        Map r = Eligibility.evaluate(1500.0, 200.0, 12, 17, 500.0, 0, 0, true, false, false);
        assertEquals(false, r.get("eligible"));
        assertTrue(((String) r.get("reasons")).contains("AGE_LOW"));
    }

    @Test
    void ageTooHighUnlessPensioner() {
        Map r = Eligibility.evaluate(1500.0, 200.0, 12, 70, 500.0, 0, 0, true, false, false);
        assertEquals(false, r.get("eligible"));
        assertTrue(((String) r.get("reasons")).contains("AGE_HIGH"));
    }

    @Test
    void pensionerOver65Accepted() {
        Map r = Eligibility.evaluate(1500.0, 200.0, 12, 70, 500.0, 0, 0, false, true, false);
        assertEquals(true, r.get("eligible"));
    }

    @Test
    void shortTenureWithGuarantorPasses() {
        Map r = Eligibility.evaluate(1500.0, 200.0, 3, 30, 500.0, 0, 0, true, false, true);
        assertEquals(true, r.get("eligible"));
    }

    @Test
    void shortTenureWithoutGuarantorRejected() {
        Map r = Eligibility.evaluate(1500.0, 200.0, 3, 30, 500.0, 0, 0, true, false, false);
        assertEquals(false, r.get("eligible"));
        assertTrue(((String) r.get("reasons")).contains("TENURE_LOW"));
    }

    @Test
    void employeeRateFloor() {
        Map r = Eligibility.evaluate(3000.0, 300.0, 60, 40, 5000.0, 0, 0, true, false, false);
        assertTrue(((Double) r.get("rate")) >= 0.08);
    }

    @Test
    void pensionerRateFloor() {
        Map r = Eligibility.evaluate(2000.0, 200.0, 60, 70, 5000.0, 0, 0, false, true, false);
        assertTrue(((Double) r.get("rate")) >= 0.10);
    }

    @Test
    void latePaymentsIncreaseRate() {
        Map a = Eligibility.evaluate(1500.0, 200.0, 24, 30, 300.0, 0, 0, true, false, false);
        Map b = Eligibility.evaluate(1500.0, 200.0, 24, 30, 300.0, 8, 0, true, false, false);
        assertTrue(((Double) b.get("rate")) > ((Double) a.get("rate")));
    }

    @Test
    void amountCapped() {
        Map r = Eligibility.evaluate(20000.0, 100.0, 60, 40, 50000.0, 0, 0, true, false, false);
        assertTrue(((Double) r.get("amount")) <= 15000.0);
    }

    @Test
    void classifyMemberTop() {
        assertEquals("A", Eligibility.classifyMember(2500, 6000));
    }

    @Test
    void classifyMemberBottom() {
        assertEquals("D", Eligibility.classifyMember(300, 100));
    }
}
