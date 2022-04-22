package dev.ssdd.ws.staticfiles;

import dev.ssdd.ws.utils.StringUtils;

import java.nio.file.Paths;

/**
 * Protecting against Directory traversal
 */
public class DirectoryTraversal {

    public static void protectAgainstInClassPath(String path, String localFolder) {
        if (!isPathWithinFolder(path, localFolder)) {
            throw new DirectoryTraversalDetection("classpath");
        }
    }

    public static void protectAgainstForExternal(String path, String externalFolder) {
    	String unixLikeFolder = unixifyPath(externalFolder);
        String nixLikePath = unixifyPath(path);
        if (!isPathWithinFolder(nixLikePath, unixLikeFolder)) {
            throw new DirectoryTraversalDetection("external");
        }
    }
    
    private static String unixifyPath(String path) {
    	return Paths.get(path).toAbsolutePath().toString().replace("\\", "/");
    }
    
    private static boolean isPathWithinFolder(String path, String folder) {
    	String rlatsPath = StringUtils.removeLeadingAndTrailingSlashesFrom(path);
    	String rlatsFolder = StringUtils.removeLeadingAndTrailingSlashesFrom(folder);
    	return rlatsPath.startsWith(rlatsFolder);
    }

    public static final class DirectoryTraversalDetection extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DirectoryTraversalDetection(String msg) {
            super(msg);
        }

    }

}
