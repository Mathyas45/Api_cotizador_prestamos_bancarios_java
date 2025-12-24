package com.optic.apirest.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 🎯 PRINCIPIO SOLID APLICADO: Single Responsibility (SRP)
 * 
 * Esta clase tiene UNA SOLA responsabilidad: 
 * Centralizar la configuración de tasas de interés según el nivel de riesgo.
 * 
 * ✅ VENTAJAS:
 * - Si las tasas cambian, solo modificas ESTE archivo
 * - Fácil de testear
 * - Código más limpio y mantenible
 * 
 * 📌 USO: Se inyecta donde se necesite obtener la tasa según riesgo
 */
@Component
public class TasaInteresConfig {

    // ═══════════════════════════════════════════════════════════════
    // 📊 TASAS DE INTERÉS ANUALES (%) - MODIFICAR AQUÍ SI CAMBIAN
    // ═══════════════════════════════════════════════════════════════
    
    private static final BigDecimal TASA_RIESGO_BAJO = BigDecimal.valueOf(7.5);    // Riesgo 1
    private static final BigDecimal TASA_RIESGO_MEDIO = BigDecimal.valueOf(8.5);   // Riesgo 2
    private static final BigDecimal TASA_RIESGO_ALTO = BigDecimal.valueOf(9.5);    // Riesgo 3+

    /**
     * Obtiene la tasa de interés anual según el nivel de riesgo del cliente.
     *  
     * @param riesgo Nivel de riesgo (1 = bajo, 2 = medio, 3+ = alto)
     * @return Tasa de interés anual como BigDecimal
     */
    public BigDecimal obtenerTasaPorRiesgo(Integer riesgo) {
        if (riesgo == null) {
            return TASA_RIESGO_ALTO; // Por defecto, tasa más alta si no hay riesgo
        }
        
        return switch (riesgo) {
            case 1 -> TASA_RIESGO_BAJO;
            case 2 -> TASA_RIESGO_MEDIO;
            default -> TASA_RIESGO_ALTO;
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // 📋 GETTERS para acceso directo si se necesitan
    // ═══════════════════════════════════════════════════════════════
    
    public BigDecimal getTasaRiesgoBajo() {
        return TASA_RIESGO_BAJO;
    }

    public BigDecimal getTasaRiesgoMedio() {
        return TASA_RIESGO_MEDIO;
    }

    public BigDecimal getTasaRiesgoAlto() {
        return TASA_RIESGO_ALTO;
    }
}
