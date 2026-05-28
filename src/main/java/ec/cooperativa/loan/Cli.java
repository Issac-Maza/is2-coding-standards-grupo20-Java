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
    public static void main(final String[] args) {
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
            String key = args[i];
            
            // Si es el último elemento aislado, no hay valor que procesar para las banderas
            if (i + 1 >= args.length) {
                break;
            }
            
            String val = args[i + 1];

            switch (key) {
                case "--income":
                    income = Double.parseDouble(val);
                    i += 2;
                    break;
                case "--debt":
                    debt = Double.parseDouble(val);
                    i += 2;
                    break;
                case "--tenure-months":
                    tenure = Integer.parseInt(val);
                    i += 2;
                    break;
                case "--age":
                    age = Integer.parseInt(val);
                    i += 2;
                    break;
                case "--savings-balance":
                    savings = Double.parseDouble(val);
                    i += 2;
                    break;
                case "--late-payments":
                    late = Integer.parseInt(val);
                    i += 2;
                    break;
                case "--dependents":
                    deps = Integer.parseInt(val);
                    i += 2;
                    break;
                case "--name":
                    name = val;
                    i += 2;
                    break;
                default:
                    i++; // Argumento no reconocido, avanzar uno de forma segura
                    break;
            }
        }

        Map<String, Object> r = Eligibility.evaluate(
            income, debt, tenure, age, savings,
            late, deps, true, false, false
        );
        System.out.println(Eligibility.formatReport(r, name)); // NOSONAR
    }
}