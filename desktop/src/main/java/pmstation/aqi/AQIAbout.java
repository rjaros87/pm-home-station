/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.aqi;

import pmstation.configuration.Constants;

public class AQIAbout {
    
    public static final String AQI_PL_INFO = "https://aqicn.org/faq/2015-09-03/air-quality-scale-in-poland/pl/";
    
    public static String getHtmlTable() {
        StringBuilder table = new StringBuilder("<center><b>Air Quality Index color standard<br/>(Polish AQI - IJP)</b></center><br/>");
        table.append("<table>");
        table.append("<tr bgcolor=\"gray\">");
        table.append("<th>").append("Level name").append("</th>");
        table.append("<th>").append("PM2.5 <small>(").append(Constants.UNITS).append(")</small></th>");
        table.append("<th>").append("PM10 <small>(").append(Constants.UNITS).append(")</small></th>");
        table.append("</tr>");
        for (AQIRiskLevel level : AQIRiskLevel.values()) {
            AQI25Level level25 = AQI25Level.fromRiskLevel(level);
            AQI10Level level10 = AQI10Level.fromRiskLevel(level);
            AQIColor levelColor = AQIColor.fromRiskLevel(level);
            table.append("<tr bgcolor=").append(String.format("#%02x%02x%02x", levelColor.getColor().getRed(), levelColor.getColor().getGreen(), levelColor.getColor().getBlue())).append(">");
            table.append("<td>").append(level.getDescription()).append("</td>");
            table.append("<td>").append(level25.getMin()).append("-").append(intToStr(level25.getMax())).append("</td>");
            table.append("<td>").append(level10.getMin()).append("-").append(intToStr(level10.getMax())).append("</td>");
            table.append("</tr>");
        }
        table.append("</table>");
        table.append("Click the label to visit <i>aqicn.org</i> website<br/>to get more details</p>");
        table.append("</html>");
        return table.toString();
    }
    
    private static String intToStr(int i) {
        return i != Integer.MAX_VALUE ? Integer.toString(i) : "\u221e";
    }
}
