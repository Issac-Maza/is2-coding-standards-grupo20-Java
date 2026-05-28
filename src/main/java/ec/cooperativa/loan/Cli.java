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
            String arg = args[i];
            if (arg.equals("--income")) {
                income = Double.parseDouble(args[i + 1]);
                i += 2;
            } else if (arg.equals("--debt")) {
                debt = Double.parseDouble(args[i + 1]);
                i += 2;
            } else if (arg.equals("--tenure-months")) {
                tenure = Integer.parseInt(args[i + 1]);
                i += 2;
            } else if (arg.equals("--age")) {
                age = Integer.parseInt(args[i + 1]);
                i += 2;
            } else if (arg.equals("--savings-balance")) {
                savings = Double.parseDouble(args[i + 1]);
                i += 2;
            } else if (arg.equals("--late-payments")) {
                late = Integer.parseInt(args[i + 1]);
                i += 2;
            } else if (arg.equals("--dependents")) {
                deps = Integer.parseInt(args[i + 1]);
                i += 2;
            } else if (arg.equals("--name")) {
                name = args[i + 1];
                i += 2;
            } else {
                i++;
            }
        }

        Map<?, ?> r = Eligibility.evaluate(
            income, debt, tenure, age, savings,
            late, deps, true, false, false
        );
        System.out.println(Eligibility.formatReport(r, name)); // NOSONAR
    }
}