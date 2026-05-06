package Utils;

import java.util.ArrayList;

public class HtmlBuilder {

    public static String generateError(String title, String subtitle) {
        return page("Error: " + title, subtitle, "#b42318");
    }

    public static String generateSuccess(String title, String subtitle) {
        return page(title, subtitle, "#047857");
    }

    public static String generateHelp() {
        return generateSuccess(
                "Servidor de correo listo",
                "Los casos de uso anteriores fueron retirados. Agrega tus nuevos casos en MailAplication y crea las clases de negocio que necesites."
        );
    }

    public static String generateTable(String title, String subtitle, String[] headers, ArrayList<String[]> data) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;color:#111827}")
                .append("table{border-collapse:collapse;width:100%}")
                .append("th,td{border:1px solid #d1d5db;padding:8px;text-align:left}")
                .append("th{background:#f3f4f6}")
                .append("</style></head><body>")
                .append("<h1>").append(escape(title)).append("</h1>")
                .append("<p>").append(escape(subtitle)).append("</p>")
                .append("<table><thead><tr>");

        for (String header : headers) {
            html.append("<th>").append(escape(header)).append("</th>");
        }

        html.append("</tr></thead><tbody>");
        for (String[] row : data) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(escape(cell)).append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private static String page(String title, String subtitle, String color) {
        return "<!DOCTYPE html>"
                + "<html lang=\"es\">"
                + "<head><meta charset=\"UTF-8\"><title>ProyectoEmail</title></head>"
                + "<body style=\"font-family:Arial,sans-serif;color:#111827\">"
                + "<main style=\"max-width:720px;margin:24px auto;padding:16px\">"
                + "<h1 style=\"color:" + color + "\">" + escape(title) + "</h1>"
                + "<p>" + escape(subtitle) + "</p>"
                + "</main>"
                + "</body>"
                + "</html>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
