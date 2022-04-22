package dev.ssdd.ws.staticfiles;

import dev.ssdd.ws.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Created by Per Wendel on 2016-11-05.
 */
public class StaticFilesFolder {
    private static final Logger LOG = LoggerFactory.getLogger(StaticFilesFolder.class);
    
    private static volatile String local;
    private static volatile String external;

    @Deprecated
    public static final void localConfiguredTo(String folder) {

        local = StringUtils.removeLeadingAndTrailingSlashesFrom(folder);
    }

    @Deprecated
    public static final void externalConfiguredTo(String folder) {

        String unixLikeFolder = Paths.get(folder).toAbsolutePath().toString().replace("\\", "/");
        LOG.warn("Registering external static files folder [{}] as [{}].", folder, unixLikeFolder);
        external = StringUtils.removeLeadingAndTrailingSlashesFrom(unixLikeFolder);
    }

    @Deprecated
    public static final String local() {
        return local;
    }

    @Deprecated
    public static final String external() {
        return external;
    }

}
