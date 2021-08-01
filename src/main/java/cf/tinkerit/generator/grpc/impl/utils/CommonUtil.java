package cf.tinkerit.generator.grpc.impl.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    private static final Pattern footnotePat = Pattern.compile("(.*?)[,:，：]\\s*(.*?http://.*)");

    private CommonUtil() {
    }

    public static String singularize(String noun) {
        if (noun.endsWith("List")) return noun.substring(0,noun.length() - 4);
        return Inflector.getInstance().singularize(noun);
    }

    public static String wrapFootnote(String name) {
        Matcher matcher = footnotePat.matcher(name);
        if (matcher.matches()) {
            return String.format(
                "%s\\footnote{%s}",
                matcher.group(1),
                matcher.group(2)
            );
        }
        else {
            return name;
        }
    }

    public static String toClassName(String fileName) {
        return fileName.substring(0, fileName.length() - 5).replaceAll("/", ".");
    }

    public static String stripCommentMarker(String line) {
        if (line != null) {
            return line.replaceAll("^//(/)*\\s*", "")
                .replaceAll("^/(\\*)+\\s*", "")
                .replaceAll("\\s*(\\*)+/$", "")
                .replaceAll("^\\s*(\\*)+\\s*", "");
        }
        return line;

    }

    public static String concat(String... args) {
        if (args != null && args.length >= 1) {
            StringBuilder buf = new StringBuilder();
            buf.append(args[0]);
            for (int i = 1; i < args.length ; i++) {
                buf.append(File.separator);
                buf.append(args[i]);
            }
            return buf.toString();
        }
        return null;
    }


    public static String forwardSlashPath(Path path) {
        StringBuilder buf = new StringBuilder();
        buf.append(path.getName(0));
        for (int i = 1; i < path.getNameCount(); i++) {
            buf.append("/");
            buf.append(path.getName(i));
        }
        return buf.toString();
    }

    public static String keepLastAfter(String qualifiedParam, String delimmiter) {
        if (qualifiedParam != null && qualifiedParam.contains(delimmiter) && !qualifiedParam.endsWith(delimmiter)) {
            int idx = qualifiedParam.indexOf(delimmiter);
            return qualifiedParam.substring(idx + 1);
        }
        return qualifiedParam;
    }



}
