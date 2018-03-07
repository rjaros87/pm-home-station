/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.helpers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Constants;

public class VersionChecker {

    private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);
    
    private static final int TIMEOUT = 5*1000; 
    private static FastDateFormat DATE_PARSER = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT; 
    
    public class LatestRelease {
        private boolean isNewerVersion = false; 
        private String version = "";
        private String changeLog = "";
        private String url = "";
        private Date date = null;
        
        public boolean isNewerVersion() {
            return isNewerVersion;
        }
        public void setNewerVersion(boolean isNewerVersion) {
            this.isNewerVersion = isNewerVersion;
        }
        public String getVersion() {
            return version;
        }
        public void setVersion(String version) {
            this.version = version;
        }
        public String getChangeLog() {
            return changeLog;
        }
        public void setChangeLog(String changeLog) {
            this.changeLog = changeLog;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public Date getDate() {
            return date;
        }
        public String getDateString() {
            return date != null ? Constants.DATE_FORMAT.format(date) : "?";
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
    }
    
    public LatestRelease check() {
        LatestRelease result = new LatestRelease();
        try {
            logger.info("Checking whether there is a newer released version of the project...");
            URL url = new URL(Constants.GITHUB_LATEST_RELEASE);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setInstanceFollowRedirects(false);
            huc.setConnectTimeout(TIMEOUT);
            huc.setReadTimeout(TIMEOUT);
            huc.setRequestMethod("GET");
            huc.setUseCaches(false);
            huc.setRequestProperty("Content-Type", "application/json");
            huc.connect();
            InputStream input = huc.getInputStream();
            JsonReader jsonReader = Json.createReader(input);
            JsonObject jsonObj = jsonReader.readObject();
            result.setVersion(jsonObj.getString("tag_name"));
            result.setChangeLog(jsonObj.getString("body"));
            result.setDate(DATE_PARSER.parse(jsonObj.getString("created_at")));
            result.setUrl(jsonObj.getString("html_url"));
            boolean isNewer = new ComparableVersion(result.getVersion()).compareTo(new ComparableVersion(Constants.VERSION)) > 0;
            result.setNewerVersion(isNewer);
            if (isNewer) {
                logger.info("A newer version is available: {}, released @ {}", result.getVersion(), result.getDate());
            } else {
                logger.info("No new version is available (latest: {})", result.getVersion());
            }
        } catch (Exception ex) {
            logger.warn("Error checking newer version", ex);
        }

        return result;
    }
}
