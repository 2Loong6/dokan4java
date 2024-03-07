package dev.dokan.core.constants;

import dev.dokan.core.structures.DokanFileInfo;
import dev.dokan.core.structures.DokanOperations;
import dev.dokan.core.structures.DokanOptions;

/**
 * All DOKAN_OPTION flags used in DOKAN_OPTIONS.Options
 *
 * @see DokanFileInfo
 */
public interface MountOptions {

    //-- Dokan Run Options --
    /**
     * Enable output debug message
     */
    int DEBUG = 1;
    /**
     * Enable output debug message to stderr
     */
    int STDERR = (1 << 1);
    /**
     * Enable the use of alternate stream paths in the form
     * {@code <file-name>:<stream-name>}. If this is not specified then the driver will
     * fail any attempt to access a path with a colon.
     */
    int ALT_STREAM = (1 << 2);
    /**
     * Enable mount drive as write-protected
     */
    int WRITE_PROTECT = (1 << 3);
    /**
     * Use network drive
     * <p>
     * Dokan network provider needs to be installed and a {@link DokanOptions#UNCName} provided
     */
    int NETWORK = (1 << 4);
    /**
     * Use removable drive
     * <p>
     * Be aware that on some environments, the userland application will be denied
     * to communicate with the drive which will result in an unwanted unmount.
     *
     * @see <a href="https://github.com/dokan-dev/dokany/issues/843">Issue #843</a>
     */
    int REMOVABLE = (1 << 5);
    /**
     * Use Windows Mount Manager.
     * <p>
     * This option is highly recommended to use for better system integration
     * <p>
     * If a drive letter is used but is busy, Mount manager will assign one for us and
     * {@link DokanOperations#Mounted} parameters will contain the new mount point.
     */
    int MOUNT_MANAGER = (1 << 6);
    /**
     * Mount the drive on current session only
     * <p>
     * Note: As Windows process only have on sessionID which is here used to define what is the current session,
     * impersonation will not work if someone attend to mount for a user from another one (like system service).
     *
     * @see <a href="https://github.com/dokan-dev/dokany/issues/1196">issue #1196</a>
     */
    int CURRENT_SESSION = (1 << 7);
    /**
     * Enable Lockfile/Unlockfile operations. Otherwise, Dokan will take care of it
     */
    int FILELOCK_USER_MODE = (1 << 8);
    /**
     * Enable Case sensitive path.
     * <p>
     * By default, all path are case-insensitive.
     * For case-sensitive: {@code \dir\File} &amp; {@code \diR\file} are different files
     * but for case-insensitive they are the same.
     */
    int CASE_SENSITIVE = (1 << 9);
    /**
     * Allows unmounting of network drive via explorer
     */
    int ENABLE_UNMOUNT_NETWORK_DRIVE = (1 << 10);
    /**
     * Forward the kernel driver global and volume logs to the userland.
     * <p>
     * Can be very slow if single thread is enabled.
     */
    int DISPATCH_DRIVER_LOGS = (1 << 11);
    /**
     * Pull batches of events from the driver instead of a single one and execute them parallel.
     * <p>
     * This option should only be used on computers with low cpu count
     * and userland filesystem taking time to process requests (like remote storage).
     */
    int ALLOW_IPC_BATCHING = (1 << 12);
}
