package com.optic.apirest.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 🎯 PRINCIPIO SOLID APLICADO: Single Responsibility (SRP)
 * 
 * Esta clase tiene UNA SOLA responsabilidad:
 * Realizar todos los cálculos financieros del sistema.
 * 
 * ✅ VENTAJAS:
 * - Código reutilizable (no se repite en varios métodos)
 * - Fácil de testear cada cálculo individualmente
 * - Si cambia una fórmula, solo se modifica aquí
 * - Mejor legibilidad del código principal
 * Desventajas:
 * - Ninguna, esta clase está bien enfocada en su propósito.
 * 
 */

public class CalculadoraFinanciera {

    // Precisión para operaciones con decimales
    private static final int PRECISION = 20;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_EVEN;

    /**
     * Calcula el monto de la cuota inicial.
     * 
     * @param monto Monto total del préstamo
     * @param porcentajeCuotaInicial Porcentaje de cuota inicial (ej: 20 para 20%)
     * @return Monto de la cuota inicial
     */
    public static BigDecimal calcularMontoCuotaInicial(BigDecimal monto, BigDecimal porcentajeCuotaInicial) {
        BigDecimal porcentajeDecimal = porcentajeCuotaInicial.divide(BigDecimal.valueOf(100), PRECISION, REDONDEO);
        return monto.multiply(porcentajeDecimal);
    }

    /**
     * Calcula el monto a financiar (monto - cuota inicial).
     * 
     * @param monto Monto total del préstamo
     * @param montoCuotaInicial Monto de la cuota inicial
     * @return Monto a financiar
     */
    public static BigDecimal calcularMontoFinanciar(BigDecimal monto, BigDecimal montoCuotaInicial) {
        return monto.subtract(montoCuotaInicial);
    }

    /**
     * Convierte años a meses.
     * 
     * @param plazoAnios Plazo en años
     * @return Plazo en meses
     */
    public static int calcularPlazoMeses(int plazoAnios) {
        return plazoAnios * 12;
    }

    /**
     * Calcula la tasa de interés mensual a partir de la tasa anual.
     * 
     * @param tasaAnual Tasa de interés anual (ej: 7.5 para 7.5%)
     * @return Tasa mensual como decimal
     */
    public static BigDecimal calcularTasaMensual(BigDecimal tasaAnual) {
        return tasaAnual
                .divide(BigDecimal.valueOf(100), PRECISION, REDONDEO)
                .divide(BigDecimal.valueOf(12), PRECISION, REDONDEO);
    }

    /**
     * Calcula la cuota mensual usando la fórmula francesa (sistema de amortización).
     * 
     * 📐 FÓRMULA:
     * Cuota = (MontoFinanciar × TasaMensual) / (1 - (1 + TasaMensual)^(-plazoMeses))
     * 
     * @param montoFinanciar Monto a financiar
     * @param tasaMensual Tasa de interés mensual
     * @param plazoMeses Plazo en meses
     * @return Cuota mensual
     */
    public static BigDecimal calcularCuotaMensual(BigDecimal montoFinanciar, BigDecimal tasaMensual, int plazoMeses) {
        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoMasTasa.pow(plazoMeses);
        BigDecimal divisor = BigDecimal.ONE.divide(potencia, PRECISION, REDONDEO);
        
        return montoFinanciar.multiply(tasaMensual)
                .divide(BigDecimal.ONE.subtract(divisor), PRECISION, REDONDEO);
    }

    /**
     * Calcula la TCEA (Tasa de Costo Efectivo Anual).
     * 
     * 📐 FÓRMULA:
     * TCEA = ((1 + TasaMensual)^12 - 1) × 100
     * 
     * @param tasaMensual Tasa de interés mensual
     * @return TCEA como porcentaje
     */
    public static BigDecimal calcularTCEA(BigDecimal tasaMensual) {
        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        return unoMasTasa.pow(12)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 🎁 MÉTODO DE CONVENIENCIA: Calcula todos los valores financieros de una vez.
     * 
     * @param monto Monto total del préstamo
     * @param porcentajeCuotaInicial Porcentaje de cuota inicial
     * @param plazoAnios Plazo en años
     * @param tasaInteresAnual Tasa de interés anual
     * @return Objeto con todos los cálculos
     */
    public static ResultadoCalculo calcularTodo(
            BigDecimal monto,
            BigDecimal porcentajeCuotaInicial,
            int plazoAnios,
            BigDecimal tasaInteresAnual) {
        
        BigDecimal montoCuotaInicial = calcularMontoCuotaInicial(monto, porcentajeCuotaInicial);
        BigDecimal montoFinanciar = calcularMontoFinanciar(monto, montoCuotaInicial);
        int plazoMeses = calcularPlazoMeses(plazoAnios);
        BigDecimal tasaMensual = calcularTasaMensual(tasaInteresAnual);
        BigDecimal cuotaMensual = calcularCuotaMensual(montoFinanciar, tasaMensual, plazoMeses);
        BigDecimal tcea = calcularTCEA(tasaMensual);

        return new ResultadoCalculo(
                montoCuotaInicial,
                montoFinanciar,
                cuotaMensual,
                tcea,
                tasaInteresAnual
        );
    }

    /**
     * 📦 Clase interna para agrupar todos los resultados del cálculo.
     * Esto evita tener que llamar múltiples métodos.
     */
    public static class ResultadoCalculo {
        private final BigDecimal montoCuotaInicial;
        private final BigDecimal montoFinanciar;
        private final BigDecimal cuotaMensual;
        private final BigDecimal tcea;
        private final BigDecimal tasaInteresAnual;

        public ResultadoCalculo(BigDecimal montoCuotaInicial, BigDecimal montoFinanciar,
                               BigDecimal cuotaMensual, BigDecimal tcea, BigDecimal tasaInteresAnual) {
            this.montoCuotaInicial = montoCuotaInicial;
            this.montoFinanciar = montoFinanciar;
            this.cuotaMensual = cuotaMensual;
            this.tcea = tcea;
            this.tasaInteresAnual = tasaInteresAnual;
        }

        public BigDecimal getMontoCuotaInicial() { return montoCuotaInicial; }
        public BigDecimal getMontoFinanciar() { return montoFinanciar; }
        public BigDecimal getCuotaMensual() { return cuotaMensual; }
        public BigDecimal getTcea() { return tcea; }
        public BigDecimal getTasaInteresAnual() { return tasaInteresAnual; }
    }
}
