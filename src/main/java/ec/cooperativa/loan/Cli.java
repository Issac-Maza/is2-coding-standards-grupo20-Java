package ec.cooperativa.loan;

import java.util.Map;

/**
 * Interfaz de línea de comandos para la evaluación de préstamos.
 */
public final class Cli {

    /**
     * Constructor privado para ocultar el constructor por defecto.
     */
    private Cli() {
        // No instanciar clases de utilidad
    }

    /**
     * Método principal que procesa los argumentos de la línea de comandos.
     *
     * @param args Argumentos pasados por la terminal.
     */
    public static void main(final String/index.html] args) {
        double income = 0;
        double debt = 0;
        double savings = 0;
        int tenure = 0;
        int age = 0;
        int late = 0;
        int deps = 0;
        String name = "Member";

        int i = 0;
    while (i < args.length) {
        if (args[i].equals("--income") && (i + 1 < args.length)) {
            income = Double.parseDouble(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--debt") && (i + 1 < args.length)) {
            debt = Double.parseDouble(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--tenure-months") && (i + 1 < args.length)) {
            tenure = Integer.parseInt(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--age") && (i + 1 < args.length)) {
            age = Integer.parseInt(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--savings-balance") && (i + 1 < args.length)) {
            savings = Double.parseDouble(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--late-payments") && (i + 1 < args.length)) {
            late = Integer.parseInt(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--dependents") && (i + 1 < args.length)) {
            deps = Integer.parseInt(args[i + 1]);
            i += 2;
        } else if (args[i].equals("--name") && (i + 1 < args.length)) {
            name = args[i + 1];
            i += 2;
        } else {
            i++; // Avanza si encuentra un argumento desconocido para no quedarse en bucle infinito
        }
    }

        Map<?, ?> r = Eligibility.evaluate(
            income, debt, tenure, age, savings,
            late, deps, true, false, false
        );
        System.out.println(Eligibility.formatReport(r, name)); // NOSONAR
    }
}