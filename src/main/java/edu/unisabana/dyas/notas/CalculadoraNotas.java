package edu.unisabana.dyas.notas;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula la nota definitiva de una asignatura con tres cortes (30% - 30% - 40%).
 *
 * Reglas:
 * - Cada nota debe estar entre 0.0 y 5.0.
 * - La definitiva se redondea a una cifra decimal (mitad hacia arriba).
 * - Se aprueba con una definitiva mayor o igual a 3.0.
 */
public class CalculadoraNotas {

    static final BigDecimal NOTA_MINIMA = new BigDecimal("0.0");
    static final BigDecimal NOTA_MAXIMA = new BigDecimal("5.0");
    static final BigDecimal NOTA_APROBATORIA = new BigDecimal("3.0");

    private static final BigDecimal PESO_CORTE_1 = new BigDecimal("0.30");
    private static final BigDecimal PESO_CORTE_2 = new BigDecimal("0.30");
    private static final BigDecimal PESO_CORTE_3 = new BigDecimal("0.40");

    public double calcularDefinitiva(double corte1, double corte2, double corte3) {
        BigDecimal definitiva = validar(corte1, "corte 1").multiply(PESO_CORTE_1)
                .add(validar(corte2, "corte 2").multiply(PESO_CORTE_2))
                .add(validar(corte3, "corte 3").multiply(PESO_CORTE_3));
        // BigDecimal evita errores de punto flotante: con double, 3.05 puede quedar en 3.0499999...
        return definitiva.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Nota que se necesita en el tercer corte para llegar justo a 3.0.
     * Se redondea hacia ARRIBA a una décima (si faltan 2.775, hay que sacar 2.8).
     * Se usa BigDecimal a propósito: con double, (3.0, 3.0) da 3.0000000000000004
     * y al subirlo a la décima termina en 3.1 en vez de 3.0.
     *
     * @throws IllegalStateException si ni sacando 5.0 se alcanza a aprobar
     */
    public double notaNecesariaTercerCorte(double corte1, double corte2) {
        BigDecimal acumulado = validar(corte1, "corte 1").multiply(PESO_CORTE_1)
                .add(validar(corte2, "corte 2").multiply(PESO_CORTE_2));
        BigDecimal necesaria = NOTA_APROBATORIA.subtract(acumulado)
                .divide(PESO_CORTE_3, 10, RoundingMode.HALF_UP);

        if (necesaria.compareTo(NOTA_MAXIMA) > 0) {
            throw new IllegalStateException(
                    "Ya no es posible aprobar: haría falta un " + necesaria.setScale(1, RoundingMode.CEILING)
                            + " en el tercer corte");
        }
        if (necesaria.compareTo(NOTA_MINIMA) < 0) {
            return 0.0;
        }
        return necesaria.setScale(1, RoundingMode.CEILING).doubleValue();
    }

    public boolean aprueba(double definitiva) {
        return validar(definitiva, "definitiva").compareTo(NOTA_APROBATORIA) >= 0;
    }

    /** Concepto cualitativo de una definitiva: Excelente, Sobresaliente, Aprobado o Reprobado. */
    public String concepto(double definitiva) {
        BigDecimal nota = validar(definitiva, "definitiva");
        if (nota.compareTo(new BigDecimal("4.5")) >= 0) {
            return "Excelente";
        } else if (nota.compareTo(new BigDecimal("4.0")) >= 0) {
            return "Sobresaliente";
        } else if (nota.compareTo(NOTA_APROBATORIA) >= 0) {
            return "Aprobado";
        }
        return "Reprobado";
    }

    private BigDecimal validar(double nota, String nombre) {
        BigDecimal valor = BigDecimal.valueOf(nota);
        if (valor.compareTo(NOTA_MINIMA) < 0 || valor.compareTo(NOTA_MAXIMA) > 0) {
            throw new IllegalArgumentException(
                    "La nota del " + nombre + " debe estar entre 0.0 y 5.0, pero fue " + nota);
        }
        return valor;
    }
}
