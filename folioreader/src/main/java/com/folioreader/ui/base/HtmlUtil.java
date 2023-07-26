package com.folioreader.ui.base;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.FontFinder;
import com.folioreader.util.ScreenUtils;

import org.springframework.util.StringUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        //添加图片样式
        ScreenUtils screenUtils = new ScreenUtils(context);
        int maxHeight = (int)(screenUtils.getRealHeight()) -70;
        int maxWidth = (int)(screenUtils.getRealWidth()/config.getColumnCount()) - getBodyPadding(config.getBodyPadding())*2;
        toInject += "<style>\n";
        toInject += " img,image,svg, audio, video {\n";
        toInject += " max-height: "+maxHeight +"px !important;\n";
        toInject += " max-width: "+maxWidth +"px !important;\n";
        toInject += "}\n";
        toInject += "\n</style>";

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

        htmlContent = htmlContent.replace("<html",
                "<html class=\"" + classes + "\"" +
                        " style=\"" + styles + "\"" +
                        " onclick=\"onClickHtml()\"");
        Integer padding = 10;
        switch (config.getBodyPadding()) {
            case 0:
                padding = 30;
                break;
            case 1:
                padding = 40;
                break;
            case 2:
                padding = 50;
                break;
            case 3:
                padding = 60;
                break;
            case 4:
                padding = 70;
                break;
            default:
                break;
        }
        padding = (int)(padding/1.75);
        String bodyStyles = "padding-left: "+ padding +"px;";
         bodyStyles += "padding-right: "+ padding +"px;";
        htmlContent = htmlContent.replace("<body",
                        "<body style=\"" + bodyStyles + "\"");
   //     htmlContent =  replaceImageHeight(htmlContent,maxHeight);
    //    htmlContent =  replaceImageWidth(htmlContent,maxWidth);
        return htmlContent;
    }

    /**替换最大高度**/
    private static String replaceImageHeight(String content,int maxHeight){
        // 待匹配替换文本
        String html = content;
        // 正则表达式
        String regex = "<image[^>]*?height=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        StringBuffer stringBuffer = new StringBuffer();

        // 将html中的下划线替换为该input标签
        while (matcher.find()) {
            // 匹配区间
            String groupStr = matcher.group(1);
            Integer height = null;
            try {
                 height = Integer.parseInt(groupStr);
                 if(height > maxHeight){
                     matcher.appendReplacement(stringBuffer, matcher.group().replace(groupStr,maxHeight+"px"));
                 }else{
                     matcher.appendReplacement(stringBuffer,  matcher.group());
                 }
            }catch (Exception e){
                matcher.appendReplacement(stringBuffer,  matcher.group());
            }
        }
        // 最终结果追加到尾部
        matcher.appendTail(stringBuffer);
        // 最终完成替换后的结果
       return stringBuffer.toString();
    }
    /**替换最大高度**/
    private static String replaceImageWidth(String content,int maxWidth){
        // 待匹配替换文本
        String html = content;
        // 正则表达式
        String regex = "<image[^>]*?width=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        StringBuffer stringBuffer = new StringBuffer();

        // 将html中的下划线替换为该input标签
        while (matcher.find()) {
            // 匹配区间
            String groupStr = matcher.group(1);
            Integer width = null;
            try {
                width = Integer.parseInt(groupStr);
                if(width > maxWidth){
                    matcher.appendReplacement(stringBuffer, matcher.group().replace(groupStr,maxWidth+"px"));
                }else{
                    matcher.appendReplacement(stringBuffer, matcher.group());
                }
            }catch (Exception e){
                matcher.appendReplacement(stringBuffer, matcher.group());
            }
        }
        // 最终结果追加到尾部
        matcher.appendTail(stringBuffer);
        // 最终完成替换后的结果
        return stringBuffer.toString();
    }

    public static int getFontSize(int progress){
        switch (progress) {
            case 0:
                return 12;
            case 1:
                return 16;
            case 2:
                return 20;
            case 3:
                return 24;
            case 4:
                return 28;
            default:
                break;
        }
        return 20;
    }
    public static int getBodyPadding(int progress){
        int padding = 0;
        switch (progress) {
            case 0:
                padding = 30;
                break;
            case 1:
                padding = 40;
                break;
            case 2:
                padding = 50;
                break;
            case 3:
                padding = 60;
                break;
            case 4:
                padding = 70;
                break;
            default:
                break;
        }
        return padding;
    }
}
