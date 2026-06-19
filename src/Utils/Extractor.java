package Utils;


public class Extractor {

    public static Email getEmail(String plainText){
        return new Email(getFrom(plainText), getSubject(plainText));
    }

    private static String getFrom(String plainText){
        String returnPath = cleanAddress(getHeader(plainText, "Return-Path"));
        if (!returnPath.isEmpty()) {
            return returnPath;
        }
        return cleanAddress(getHeader(plainText, "From"));
    }

    private static String getSubject(String plainText){
        return MimeCodec.decodeText(getHeader(plainText, "Subject").trim());
    }

    private static String getHeader(String plainText, String headerName) {
        String[] lines = unfoldHeaders(plainText).split("\\r?\\n");
        String prefix = headerName.toLowerCase() + ":";
        boolean headerStarted = false;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (headerStarted) {
                    break;
                }
                continue;
            }
            headerStarted = true;
            if (line.toLowerCase().startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private static String unfoldHeaders(String plainText) {
        String[] lines = plainText == null ? new String[0] : plainText.split("\\r?\\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && result.length() > 0) {
                result.append(' ').append(line.trim());
            } else {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
        }
        return result.toString();
    }

    private static String cleanAddress(String value) {
        if (value == null) {
            return "";
        }
        String address = value.trim();
        int start = address.indexOf('<');
        int end = address.indexOf('>', start + 1);
        if (start >= 0 && end > start) {
            address = address.substring(start + 1, end);
        }
        return address.trim();
    }
}
