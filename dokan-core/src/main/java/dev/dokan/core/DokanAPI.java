package dev.dokan.core;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import dev.dokan.core.constants.DokanMountReturnValues;
import dev.dokan.core.nativeannotations.Enum;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Unsigned;
import dev.dokan.core.structures.DokanCallback;
import dev.dokan.core.structures.DokanFileInfo;
import dev.dokan.core.structures.DokanOperations;
import dev.dokan.core.structures.DokanOptions;

/**
 * @see <a href="https://github.com/dokan-dev/dokany/blob/master/dokan/dokan.h">dokan.h</a>
 */
public class DokanAPI {

    private static final String DOKAN_DLL = System.getProperty("dev.dokan.core.libraryPath", "dokan2");

    static {
        Native.register(DOKAN_DLL);
    }

    private DokanAPI() {
    }

    /**
     * Initialize all required Dokan internal resources.
     * <p>
     * This needs to be called only once before trying to use {@link #DokanMain} or {@link #DokanCreateFileSystem} for the first time.
     * Otherwise, both will fail and raise an exception.
     */
    public static native void DokanInit();

    /**
     * Release all allocated resources by {@link #DokanInit} when they are no longer needed.
     * <p>
     * This should be called when the application no longer expects to create a new FileSystem with
     * {@link #DokanMain} or {@link #DokanCreateFileSystem} and after all devices are unmount.
     */
    public static native void DokanShutdown();

    /**
     * Mount a new Dokan Volume.
     * <p>
     * This function block until the device is unmounted.
     * If the mount fails, it will directly return a {@link DokanMountReturnValues} error.
     *
     * @param dokanOptions    a {@link DokanOptions} that describe the mount.
     * @param dokanOperations Instance of {@link DokanOperations} that will be called for each request made by the kernel.
     * @return {@link DokanMountReturnValues} status.
     * @see #DokanCreateFileSystem to create mount Dokan Volume asynchronously.
     */
    public static native int DokanMain(DokanOptions dokanOptions, DokanOperations dokanOperations);

    /**
     * Mount a new Dokan Volume.
     * <p>
     * It is mandatory to have called {@link #DokanInit} previously to use this API.
     * <p>
     * This function returns directly on device mount or on failure.
     * See {@link DokanMountReturnValues} for possible errors.
     * <p>
     * {@link #DokanWaitForFileSystemClosed} can be used to wait until the device is unmounted.
     *
     * @param dokanOptions    a {@link DokanOptions} that describe the mount.
     * @param dokanOperations Instance of {@link DokanOperations} that will be called for each request made by the kernel.
     * @param dokanInstance   Dokan mount instance context that can be used for related instance calls like {@link #DokanIsFileSystemRunning}.
     * @return {@link DokanMountReturnValues} status.
     */
    public static native int DokanCreateFileSystem(DokanOptions dokanOptions, DokanOperations dokanOperations, Pointer dokanInstance);

    /**
     * Check if the FileSystem is still running or not.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @return Whether the FileSystem is still running or not.
     */
    public static native boolean DokanIsFileSystemRunning(Pointer dokanInstance);

    /**
     * Wait until the FileSystem is unmounted.
     *
     * @param dokanInstance  The dokan mount context created by {@link #DokanCreateFileSystem} .
     * @param dwMilliseconds The time-out interval, in milliseconds. See <a href="https://docs.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-waitforsingleobject">WaitForSingleObject</a>.
     * @return See <a href="https://docs.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-waitforsingleobject">WaitForSingleObject</a> for a description of return values.
     */
    //TODO: return value?
    public static native int DokanWaitForFileSystemClosed(Pointer dokanInstance, @Unsigned int dwMilliseconds);

    /**
     * Register callback for FileSystem unmount.
     *
     * @param dokanInstance  The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param waitHandle     Handle to wait handle registration. This handle needs to be closed by calling <a href="https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-unregisterwait">UnregisterWait</a>, <a href="https://learn.microsoft.com/en-us/windows/win32/sync/unregisterwait">UnregisterWaitEx</a> or {@link #DokanUnregisterWaitForFileSystemClosed} to unregister the callback.
     * @param callback       Callback function to be called from a worker thread when file system is unmounted.
     * @param context        A context parameter to send to callback function .
     * @param dwMilliseconds The time-out interval, in milliseconds. See <a href="https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-registerwaitforsingleobject">RegisterWaitForSingleObject</a>.
     * @return TRUE if successful, FALSE otherwise.
     */
    public static native boolean DokanRegisterWaitForFileSystemClosed(
            Pointer dokanInstance,
            WinNT.HANDLE waitHandle,
            DokanCallback callback,
            Pointer context,
            @Unsigned int dwMilliseconds);

    /**
     * Unregister callback for FileSystem unmount.
     *
     * @param waitHandle       Handle returned as WaitHandle parameter in previous call to {@link #DokanRegisterWaitForFileSystemClosed}.
     * @param waitForCallbacks Indicates whether to wait for callbacks to complete before returning. Normally set to TRUE unless called from same thread as callback function.
     * @return TRUE if successful, FALSE otherwise.
     */
    public static native boolean DokanUnregisterWaitForFileSystemClosed(WinNT.HANDLE waitHandle, boolean waitForCallbacks);

    /**
     * Unmount the Dokan instance.
     * <p>
     * Unmount and wait until all resources of the {@code DokanInstance} are released.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     */
    public static native void DokanCloseHandle(Pointer dokanInstance);

    /**
     * Unmount a Dokan device from a driver letter.
     *
     * @param driveLetter Dokan driver letter to unmount.
     * @return TRUE if device was unmounted or FALSE in case of failure or device not found.
     */
    public static native boolean DokanUnmount(char driveLetter);

    /**
     * Unmount a Dokan device from a mount point
     *
     * @param mountPoint Mount point to unmount ("Z", "Z:", "Z:\", "Z:\MyMountPoint").
     * @return TRUE if device was unmounted or FALSE in case of failure or device not found.
     */
    public static native boolean DokanRemoveMountPoint(WString mountPoint);

    /**
     * Checks whether Name matches Expression
     * <p>
     * Behave like <a href="https://msdn.microsoft.com/en-us/library/ff546850(v=VS.85).aspx">FsRtlIsNameInExpression</a> routine<br/>
     * <pre>
     * * (asterisk) Matches zero or more characters.<br/>
     * <tt>?</tt> (question mark) Matches a single character.<br/>
     * DOS_DOT (" quotation mark) Matches either a period or zero characters beyond the name string.<br/>
     * DOS_QM (> greater than) Matches any single character or, upon encountering a period or end of name string, advances the expression to the end of the set of contiguous DOS_QMs.<br/>
     * DOS_STAR (< less than) Matches zero or more characters until encountering and matching the final \c . in the name.
     * </pre>
     *
     * @param Expression Expression can contain any of the above characters.
     * @param Name       Name to check
     * @param IgnoreCase Case-sensitive or not
     * @return result if name matches the expression
     */
    public static native boolean DokanIsNameInExpression(WString Expression, WString Name, boolean IgnoreCase);

    /**
     * Get the version of Dokan.
     * <p>
     * The returned ULONG is the version number without the dots.
     *
     * @return The version of Dokan
     */
    public static native @Unsigned int DokanVersion();

    /**
     * Get the version of the Dokan driver.
     * <p>
     * The returned ULONG is the version number without the dots.
     *
     * @return The version of Dokan driver or 0 on failure.
     */
    public static native @Unsigned int DokanDriverVersion();

    /**
     * Extends the timeout of the current IO operation in driver.
     *
     * @param timeout       Extended time in milliseconds requested.
     * @param dokanFileInfo {@link DokanFileInfo} of the operation to extend.
     * @return If the operation was successful.
     */
    public static native boolean DokanResetTimeout(@Unsigned int timeout, DokanFileInfo dokanFileInfo);

    /**
     * Get the handle to Access Token.
     * <p>
     * This method needs be called in {@link DokanOperations.ZwCreateFile} callback.
     * The caller must call <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms724211(v=vs.85).aspx">CloseHandle</a>
     * for the returned handle.
     *
     * @param dokanFileInfo {@link DokanFileInfo} of the operation to extend.
     * @return A handle to the account token for the user on whose behalf the code is running.
     */
    public static native WinNT.HANDLE DokanOpenRequestorToken(DokanFileInfo dokanFileInfo);

    /**
     * Get active Dokan mount points.
     * <p>
     * Returned array need to be released by calling {@link #DokanReleaseMountPointList}
     *
     * @param uncOnly Get only instances that have UNC Name.
     * @param nbRead  Number of instances successfully retrieved.
     * @return Allocate array of DOKAN_MOUNT_POINT_INFO.
     */
    // todo
    public static native Pointer DokanGetMountPointList(boolean uncOnly, IntByReference nbRead);

    /**
     * Release Mount point list resources from {@link #DokanGetMountPointList}.
     * <p>
     * After {@link #DokanGetMountPointList} call you will receive a dynamically allocated array of DOKAN_MOUNT_POINT_INFO.
     * This array needs to be released when no longer needed by calling this function.
     *
     * @param list Allocated array of DOKAN_MOUNT_POINT_INFO from {@link #DokanGetMountPointList}.
     */
    // todo
    public static native void DokanReleaseMountPointList(Pointer list);

    /**
     * Convert {@link DokanOperations.ZwCreateFile} parameters to <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx">CreateFile</a> parameters.
     * <p>
     * Dokan Kernel forward the DesiredAccess directly from the IRP_MJ_CREATE.
     * This DesiredAccess has been converted from generic rights (user CreateFile request) to standard rights and will be converted back here.
     * <a href="https://msdn.microsoft.com/windows/hardware/drivers/ifs/access-mask">access-mask</a>
     *
     * @param desiredAccess             DesiredAccess from {@link DokanOperations.ZwCreateFile}.
     * @param fileAttributes            FileAttributes from {@link DokanOperations.ZwCreateFile}.
     * @param createOptions             CreateOptions from {@link DokanOperations.ZwCreateFile}.
     * @param createDisposition         CreateDisposition from {@link DokanOperations.ZwCreateFile}.
     * @param outDesiredAccess          New <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx">CreateFile</a> dwDesiredAccess.
     * @param outFileAttributesAndFlags New <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx">CreateFile</a> dwFlagsAndAttributes.
     * @param outCreationDisposition    New <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx">CreateFile</a> dwCreationDisposition.
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa363858(v=vs.85).aspx">CreateFile function (MSDN)</a>
     */
    public static native void DokanMapKernelToUserCreateFileFlags(
            @EnumSet int desiredAccess,
            @EnumSet int fileAttributes,
            @EnumSet int createOptions,
            @EnumSet int createDisposition,
            @EnumSet IntByReference outDesiredAccess,
            @EnumSet IntByReference outFileAttributesAndFlags,
            @EnumSet IntByReference outCreationDisposition);

    /**
     * Notify dokan that a file or a directory has been created.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param filePath      Absolute path to the file or directory, including the mount-point of the file system.
     * @param isDirectory   Indicates if the path is a directory.
     * @return TRUE if notification succeeded.
     */
    public static native boolean DokanNotifyCreate(Pointer dokanInstance, WString filePath, boolean isDirectory);

    /**
     * Notify dokan that a file or a directory has been deleted.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param filePath      Absolute path to the file or directory, including the mount-point of the file system.
     * @param isDirectory   Indicates if the path was a directory.
     * @return TRUE if notification succeeded.
     */
    public static native boolean DokanNotifyDelete(Pointer dokanInstance, WString filePath, boolean isDirectory);

    /**
     * Notify dokan that file or directory attributes have changed.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param filePath      Absolute path to the file or directory, including the mount-point of the file system.
     * @return TRUE if notification succeeded.
     */
    public static native boolean DokanNotifyUpdate(Pointer dokanInstance, WString filePath);

    /**
     * Notify dokan that file or directory extended attributes have changed.
     *
     * @param dokanInstance The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param filePath      Absolute path to the file or directory, including the mount-point of the file system.
     * @return TRUE if notification succeeded.
     */
    public static native boolean DokanNotifyXAttrUpdate(Pointer dokanInstance, WString filePath);

    /**
     * Notify dokan that a file or a directory has been renamed. This method
     * supports in-place rename for file/directory within the same parent.
     *
     * @param dokanInstance     The dokan mount context created by {@link #DokanCreateFileSystem}.
     * @param oldPath           Old, absolute path to the file or directory, including the mount-point of the file system.
     * @param newPath           New, absolute path to the file or directory, including the mount-point of the file system.
     * @param isDirectory       Indicates if the path is a directory.
     * @param isInSameDirectory Indicates if the file or directory have the same parent directory.
     * @return TRUE if notification succeeded.
     */
    public static native boolean DokanNotifyRename(
            Pointer dokanInstance,
            WString oldPath,
            WString newPath,
            boolean isDirectory,
            boolean isInSameDirectory);

    /**
     * Convert WIN32 error to <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/596a1078-e883-4972-9bbc-49e60bebca55">NTSTATUS</a>
     *
     * @param error Win32 Error to convert
     * @return {@link NTStatus} associate to the ERROR.
     */
    public static native int DokanNtStatusFromWin32(@Enum int error);
}
