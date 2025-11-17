package com.optic.apirest.Client;

import com.optic.apirest.dto.apiValidarHistorial.ValidacionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TasaInteresApiClient {

    private final RestTemplate restTemplate;

    public TasaInteresApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // Método que obtiene riesgo y resultado_validacion desde MockAPI según el DNI
    public ValidacionResponse obtenerValidacionCliente(String dni) {
        try {
            // Incluimos el filtro por DNI
            String url = "https://6905b47eee3d0d14c1337027.mockapi.io/validaciones?dni=" + dni;

            // Llamada HTTP
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONArray jsonArray = new JSONArray(response.getBody());

                if (jsonArray.length() > 0) {
                    JSONObject data = jsonArray.getJSONObject(0);

                    // Extraemos los datos
                    ValidacionResponse result = new ValidacionResponse();
                    result.setDni(data.getString("dni"));
                    result.setRiesgo(Double.parseDouble(data.getString("riesgo")));
                    result.setResultadoValidacion(data.getString("resultado_validacion"));

                    System.out.println("✅ API Mock obtenida para DNI " + dni + ": Riesgo " + result.getRiesgo() + ", Resultado " + result.getResultadoValidacion());
                    return result;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error al consultar MockAPI: " + e.getMessage());
        }

        // Si falla la consulta, devolvemos un objeto por defecto
        ValidacionResponse fallback = new ValidacionResponse();
        fallback.setDni(dni);
        fallback.setRiesgo(3.0); // riesgo alto
        fallback.setResultadoValidacion("RECHAZADO");
        return fallback;
    }
}
