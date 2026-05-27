# loan-eligibility-java

Loan eligibility calculator for a cooperativa de ahorro y crédito. Computes whether a member is eligible for a loan and at what rate, based on income, debt, employment, and savings history.

## Setup

Requires Java 21 and Maven 3.9+.

```bash
mvn -B install
```

## Run the tests

```bash
mvn test
```

## Use it from the CLI

```bash
mvn -q exec:java -Dexec.mainClass="ec.cooperativa.loan.Cli" \
  -Dexec.args="--income 1200 --debt 320 --tenure-months 18 --age 34 --savings-balance 850"
```
