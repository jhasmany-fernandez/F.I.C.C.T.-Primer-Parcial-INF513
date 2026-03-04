/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import Datos.DReportes;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pedri
 */
public class NReportes {

    private final DReportes dReportes;

    private final static String COMPONENT_HEAD = "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <title>SSEMM</title>\n"
            + "    <style>\n"
            + "        @import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap');\n"
            + "        * {\n"
            + "            font-family: 'Nunito', sans-serif;\n"
            + "        }\n"
            + "        h1 {\n"
            + "            font-size: 24px;\n"
            + "            margin: 0px;\n"
            + "            margin-left: 5px;\n"
            + "        }\n"
            + "        h2 {\n"
            + "            font-size: 18px;\n"
            + "            margin-top: 0;\n"
            + "        }\n"
            + "        .container-flex {\n"
            + "            display: flex;\n"
            + "            align-items: center;\n"
            + "            justify-content: center;\n"
            + "        }\n"
            + "        .container {\n"
            + "            display: flex;\n"
            + "            text-align: center;\n"
            + "        }\n"
            + "        .logo {\n"
            + "            width: 50px;\n"
            + "            height: 50px;\n"
            + "        }\n"
            + "        .text {\n"
            + "            font-size: 24px;\n"
            + "            font-weight: bold;\n"
            + "            padding-left: 5px;\n"
            + "            margin: auto;\n"
            + "        }\n"
            + "        .titlelogo {\n"
            + "            width: 30px;\n"
            + "            height: 30px;\n"
            + "        }\n"
            + "        .text-gray {\n"
            + "            color: #f0f0f0;\n"
            + "        }\n"
            + "        .text-green {\n"
            + "            color: rgb(167, 205, 255);\n"
            + "        }\n"
            + "        .text-green-200 {\n"
            + "            color: #0ddf6f;\n"
            + "        }\n"
            + "        .graphic{\n"
            + "            border-radius: 20px;\n"
            + "            height: 400px;\n"
            + "        }\n"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "    <div style=\"border-radius: 20px; background-color: rgb(3, 48, 107); padding: 10px;\">\n"
            + "        <div class=\"container-flex\">\n"
            + "            <div class=\"container\">\n"
            + "                <img class=\"logo\"\n"
            + "                    src=\"https://lh3.googleusercontent.com/u/0/drive-viewer/AITFw-xyNrTsHzJFfoT6ZDqSj33jQeYW173wB-aufa-c4N70gjmT4HNygEBBRe6RWtPEczbHM3BJP5DdH-vYfgePi6ktTbyUKA=w1365-h663\"\n"
            + "                    alt=\"logoSSEM\">\n"
            + "                <div class=\"text text-gray\">SSEM</div>\n"
            + "            </div>\n"
            + "        </div>\n";
    private final static String COMPONENT_FOOT = "</div>\n"
            + "    </div>\n"
            + "</body>\n"
            + "</html>";

    public NReportes() {
        dReportes = new DReportes();
    }

    public String ver(List<String> parametros) throws SQLException {
        String response = "";

        switch (parametros.get(0).trim()) {
            case "PacienteGenero":
                response = verPacientesGenero(dReportes.verPacientesGenero());
                break;
            case "PacienteEnfermedad":
                response = verPacienteEnfermedad(dReportes.numPacienteEnfermedad());
                break;
            case "MaxCasoFecha":
                response = verMaxCasos(dReportes.maxCasosFecha());
                break;
            case "CasosPorMes":
                response = verCasosMes(dReportes.casosPorMes());
                break;
            default:
                response = defaultMessage();
        }
        return response;
    }

    private String verPacientesGenero(Map<String, Integer> data) {
        String[] headcontent = getHeaderAndContent(data);
        String text = setText("CU Reportes y Estadisticas PacienteEnfermedad",
                "El siguiente grafico muestra la cantidad de usuarios en calidad de pacientes por genero en el sistema");
        String graphic = "<img class=\"graphic\" src=\"https://quickchart.io/chart?bkg=rgb(167, 205, 255)&"
                + "c={ type: 'pie', data: { "
                + "labels: [" + headcontent[0] + "], "
                + "datasets: [{ data: [" + headcontent[1] + "], "
                + "backgroundColor: ['rgb(239, 158, 229)', 'rgb(132, 180, 243)'] }]}}\" "
                + "alt=\"Graphic\">";
        return getHtml(text, graphic);
    }

    private String[] getHeaderAndContent(Map<String, Integer> data) {
        String headers = "";
        String content = "";
        String[] result;
        for (Map.Entry<String, Integer> element : data.entrySet()) {
            String key = element.getKey();
            Integer value = element.getValue();
            headers += "'" + key + "',";
            content += value + ",";
        }
        if (headers.length() > 1) {
            headers = headers.substring(0, headers.length() - 1);
        }

        if (content.length() > 1) {
            content = content.substring(0, content.length() - 1);
        }
        result = new String[]{
            headers,
            content
        };
        return result;
    }

    private String defaultMessage() {
        String text = setText("CU Reportes y Estadisticas",
                "No se encontro tal reporte");
        String graphic = "            <h1 style=\"color: red;\">No existe el reporte: </h1>\n";

        return getHtml(text, graphic);
    }

    private String verCasosMes(Map<String, Integer> data) {
        String[] headcontent = getHeaderAndContent(data);
        String text = setText("CU Reportes y Estadisticas CasosPorMes",
                "Este grafico muestra el numero de casos presentes a lo largo de la existencia del sistema");
        String graphic = "<img class=\"graphic\" src=\"https://quickchart.io/chart?bkg=rgb(167, 205, 255)&"
                + "c={type:'line',data:{"
                + "labels:[" + headcontent[0] + "],"
                + "datasets:[{label:'Cantidad de casos',"
                + "data:[" + headcontent[1] + "],"
                + "fill:false,borderColor:'green'"
                + "}]}}\" alt=\"Graphic\">";
        return getHtml(text, graphic);
    }

    private String verMaxCasos(Map<String, Integer> data) {
        String[] headcontent = getHeaderAndContent(data);
        String text = setText("CU Reportes y Estadisticas MaxCasoFecha", "Esta es la fecha con el numero mas altos de casos registrados");

        String graphic = 
                "            <h1 style=\"color: greenyellow;\">Fecha: <span class=\"text-gray\">"+ headcontent[0] +"</span></h1>\n"
                + "            <h1 style=\"color: greenyellow;\">Cantidad: <span class=\"text-gray\">"+ headcontent[1] +"</span></h1>";

        return getHtml(text, graphic);
    }

    private String verPacienteEnfermedad(Map<String, Integer> data) {
        String[] headcontent = getHeaderAndContent(data);
        String text = setText("CU Reportes y Estadisticas PacienteEnfermedad",
                "Este grafico muestra la cantidad de registros de pacientes con determinada enfermedad en el sistema");
        String graphic = "<img class=\"graphic\" src=\"https://quickchart.io/chart?bkg=rgb(167, 205, 255)&"
                + "c={type:'bar',data:{"
                + "labels:[" + headcontent[0] + "],"
                + "datasets:[{label: 'Cantidad de Casos', "
                + "data:[ " + headcontent[1] + " ]"
                + "}]}}\" alt=\"Graphic\">";

        return getHtml(text, graphic);
    }

    private String setText(String title, String subtitle) {
        return "        <div class=\"container\">\n"
                + "            <img class=\"titlelogo\" src=\"https://lh3.google.com/u/0/d/1ZtooCBSwYg6LJswEXdC_RV-JrQbT2grB=w1920-h878-iv1\"\n"
                + "                alt=\"img help\">\n"
                + "            <h1 class=\"text-green\">\n"
                + "                Aqui tienes tu Reporte! :" + title + "</h1>\n"
                + "        </div>\n"
                + "        <h2 class=\"text-gray\"><span class=\"text-green-200\">Descripción: </span>" + subtitle + "</h2>\n"
                + "        <div>";
    }

    private String getHtml(String text, String graphic) {
        return COMPONENT_HEAD + text + graphic + COMPONENT_FOOT;
    }

}
