package Utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeCodec {

    private static final Pattern ENCODED_WORD = Pattern.compile("=\\?([^?]+)\\?([bBqQ])\\?([^?]*)\\?=");
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private MimeCodec() {
    }

    public static String decodeText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        Matcher matcher = ENCODED_WORD.matcher(value);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        boolean lastWasEncoded = false;

        while (matcher.find()) {
            String between = value.substring(lastEnd, matcher.start());
            if (!(lastWasEncoded && between.trim().isEmpty())) {
                result.append(between);
            }
            result.append(decodeWord(matcher.group(1), matcher.group(2), matcher.group(3)));
            lastEnd = matcher.end();
            lastWasEncoded = true;
        }

        result.append(value.substring(lastEnd));
        return result.toString();
    }

    public static String encodeText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (isAsciiHeaderSafe(value)) {
            return value;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder encoded = new StringBuilder("=?UTF-8?Q?");
        for (byte current : bytes) {
            int b = current & 0xFF;
            if (b == 0x20) {
                encoded.append('_');
            } else if (isQSafe(b)) {
                encoded.append((char) b);
            } else {
                encoded.append('=').append(HEX[(b >> 4) & 0x0F]).append(HEX[b & 0x0F]);
            }
        }
        encoded.append("?=");
        return encoded.toString();
    }

    private static String decodeWord(String charsetName, String encoding, String encodedText) {
        try {
            Charset charset = Charset.forName(charsetName.trim());
            byte[] bytes;
            if ("B".equalsIgnoreCase(encoding)) {
                bytes = Base64.getDecoder().decode(encodedText);
            } else {
                bytes = decodeQuotedPrintableHeader(encodedText);
            }
            return new String(bytes, charset);
        } catch (Exception ex) {
            return "=?" + charsetName + "?" + encoding + "?" + encodedText + "?=";
        }
    }

    private static byte[] decodeQuotedPrintableHeader(String value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '_') {
                output.write(' ');
            } else if (ch == '=' && i + 2 < value.length()) {
                int high = hexValue(value.charAt(i + 1));
                int low = hexValue(value.charAt(i + 2));
                if (high >= 0 && low >= 0) {
                    output.write((high << 4) + low);
                    i += 2;
                } else {
                    output.write((byte) ch);
                }
            } else {
                output.write((byte) ch);
            }
        }
        return output.toByteArray();
    }

    private static int hexValue(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        char upper = Character.toUpperCase(ch);
        if (upper >= 'A' && upper <= 'F') {
            return upper - 'A' + 10;
        }
        return -1;
    }

    private static boolean isAsciiHeaderSafe(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 32 || ch >= 127) {
                return false;
            }
        }
        return true;
    }

    private static boolean isQSafe(int b) {
        if (b >= 'A' && b <= 'Z') {
            return true;
        }
        if (b >= 'a' && b <= 'z') {
            return true;
        }
        if (b >= '0' && b <= '9') {
            return true;
        }
        String safe = "!*+-/.,:;";
        return safe.indexOf((char) b) >= 0;
    }
}
