package com.folioreader.ui.base;

import android.content.Context;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.FontFinder;

import java.io.File;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {

        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");

        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jquery-3.4.1.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-core.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-highlighter.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-classapplier.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-serializer.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/Bridge.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangefix.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/readium-cfi.umd.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag_method_call),
                "setMediaOverlayStyleColors('#C0ED72','#C0ED72')") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String fontName = config.getFont();

        System.out.println("Font family: " + fontName);

        // Inject CSS & user font style
        String toInject = "\n" + cssPath + "\n" + jsPath + "\n";

        if (fontName != null) {
            String filePath = "file:///android_asset/fonts/"+fontName;
            System.out.println("Injected user font into CSS");
            System.out.println("  - path: " + filePath);
            System.out.println("  - family: '" + fontName + "'");
            toInject += "<style>\n";
            toInject += "@font-face {\n";
            toInject += "  font-family: '" + fontName + "';\n";
            toInject += "  src: url('" + filePath + "');\n";
            toInject += "}\n";
            toInject += ".custom-font, .custom-font p, .custom-font div, .custom-font span, .custom-font h1, .custom-font strong {\n";
            toInject += "  font-family: '" + fontName + "', sans-serif;\n";
            toInject += "}\n";
            toInject += "\n</style>";
        }
        //修改
        //行距
        String textSpace = " 1em ";
        switch (config.getTextSpace()) {
            case 0:
                textSpace = " 1em ";
                break;
            case 1:
                textSpace = " 1.5em ";
                break;
            case 2:
                textSpace = " 2em ";
                break;
            case 3:
                textSpace = " 2.5em ";
                break;
            case 4:
                textSpace = " 3em ";
                break;
            default:
                break;
        }

        toInject += "<style>\n";
        toInject += "p,body {\n";
        toInject += " line-height: " + textSpace + " !important;\n";
        toInject += "}\n";
        toInject += "\n</style>";


        toInject += "</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "custom-font";

        if (config.isNightMode()) {
            classes += " nightMode";
        }
        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        String styles = "font-family: '" + fontName + "';";
        styles +="background-color: "+config.getBackgroundColor()+";";
        htmlContent = htmlContent.replace("<html",
                "<html class=\"" + classes + "\"" +
                        " style=\"" + styles + "\"" +
                        " onclick=\"onClickHtml()\"");
        String padding = "10px";
        switch (config.getBodyPadding()) {
            case 0:
                padding = " 10px ";
                break;
            case 1:
                padding = " 20px ";
                break;
            case 2:
                padding = " 30px ";
                break;
            case 3:
                padding = " 40px ";
                break;
            case 4:
                padding = " 50px ";
                break;
            default:
                break;
        }
        String bodyStyles = "padding: "+ padding +";";
        htmlContent = htmlContent.replace("<body",
                        "<body style=\"" + bodyStyles + "\"");

        return htmlContent;
    }
}
