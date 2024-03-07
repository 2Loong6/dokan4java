package dev.dokan.core.structures;

import dev.dokan.core.DokanAPI;
import dev.dokan.core.NTStatus;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinBase.WIN32_FIND_DATA;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import dev.dokan.core.constants.FileSystemAttributes;
import dev.dokan.core.constants.MountOptions;
import dev.dokan.core.nativeannotations.Enum;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Unsigned;

import static com.sun.jna.platform.win32.WinDef.MAX_PATH;

/**
 * DOKAN_OPERATIONS
 * Dokan API callbacks interface
 * <p>
 * DOKAN_OPERATIONS is a struct of callbacks that describe all Dokan API operations
 * that will be called when Windows access to the filesystem.
 * <p>
 * If an error occurs, return <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/596a1078-e883-4972-9bbc-49e60bebca55">NTSTATUS</a>.
 * Win32 Error can be converted to {@link NTStatus} with {@link DokanAPI#DokanNtStatusFromWin32(int)}
 * <p>
 * All callbacks can be set to {@code null} or return {@link NTStatus#STATUS_NOT_IMPLEMENTED}
 * if supporting one of them is not desired. Be aware that returning such values to important callbacks
 * such as {@link ZwCreateFile} / {@link ReadFile} / ... would make the filesystem not work or become unstable.
 */
@Structure.FieldOrder({"ZwCreateFile", "Cleanup", "CloseFile", "ReadFile", "WriteFile", "FlushFileBuffers", "GetFileInformation", "FindFiles", "FindFilesWithPattern", "SetFileAttributes", "SetFileTime", "DeleteFile", "DeleteDirectory", "MoveFile", "SetEndOfFile", "SetAllocationSize", "LockFile", "UnlockFile", "GetDiskFreeSpace", "GetVolumeInformation", "Mounted", "Unmounted", "GetFileSecurity", "SetFileSecurity", "FindStreams"})
public class DokanOperations extends Structure {

    //TODO: should this class be final?
    public DokanOperations() {
    }

    public ZwCreateFile ZwCreateFile;
    public Cleanup Cleanup;
    public CloseFile CloseFile;
    public ReadFile ReadFile;
    public WriteFile WriteFile;
    public FlushFileBuffers FlushFileBuffers;
    public GetFileInformation GetFileInformation;
    public FindFiles FindFiles;
    public FindFilesWithPattern FindFilesWithPattern;
    public SetFileAttributes SetFileAttributes;
    public SetFileTime SetFileTime;
    public DeleteFile DeleteFile;
    public DeleteDirectory DeleteDirectory;
    public MoveFile MoveFile;
    public SetEndOfFile SetEndOfFile;
    public SetAllocationSize SetAllocationSize;
    public LockFile LockFile;
    public UnlockFile UnlockFile;
    public GetDiskFreeSpace GetDiskFreeSpace;
    public GetVolumeInformation GetVolumeInformation;
    public Mounted Mounted;
    public Unmounted Unmounted;
    public GetFileSecurity GetFileSecurity;
    public SetFileSecurity SetFileSecurity;
    public FindStreams FindStreams;

    @FunctionalInterface
    public interface ZwCreateFile extends DokanCallback {

        /**
         * CreateFile Dokan API callback
         * <p>
         * CreateFile is called each time a request is made on a file system object.
         * <p>
         * In case {@code OPEN_ALWAYS} & {@code CREATE_ALWAYS} are successfully opening an
         * existing file, {@link NTStatus#OBJECT_NAME_COLLISION} should be returned instead of {@link NTStatus#STATUS_SUCCESS}.
         * This will inform Dokan that the file has been opened and not created during the request.
         * <p>
         * If the file is a directory, CreateFile is also called.
         * In this case, CreateFile should return {@link NTStatus#STATUS_SUCCESS} when that directory
         * can be opened and {@link DokanFileInfo#isDirectory} has to be set to {@code TRUE}.
         * On the other hand, if {@link DokanFileInfo#isDirectory} is set to {@code TRUE}
         * but the path targets a file, {@link NTStatus#NOT_A_DIRECTORY} must be returned.
         * <p>
         * {@link DokanFileInfo#context} can be used to store Data (like {@code HANDLE})
         * that can be retrieved in all other requests related to the Context.
         * To avoid memory leak, Context needs to be released in {@link Cleanup}.
         *
         * @param fileName          File path requested by the Kernel on the FileSystem.
         * @param securityContext   SecurityContext, see <a href="https://msdn.microsoft.com/en-us/library/windows/hardware/ff550613(v=vs.85).aspx">IO_SECURITY_CONTEXT structure (wdm.h)</a>
         * @param desiredAccess     Specifies an <a href="https://msdn.microsoft.com/en-us/library/windows/hardware/ff540466(v=vs.85).aspx">ACCESS_MASK</a> value that determines the requested access to the object.
         * @param fileAttributes    Specifies one or more FILE_ATTRIBUTE_XXX flags, which represent the file attributes to set if a file is created or overwritten.
         * @param shareAccess       Type of share access, which is specified as zero or any combination of FILE_SHARE_* flags.
         * @param createDisposition Specifies the action to perform if the file does or does not exist.
         * @param createOptions     Specifies the options to apply when the driver creates or opens the file.
         * @param dokanFileInfo     Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/hardware/ff566424(v=vs.85).aspx">See ZwCreateFile for more information about the parameters of this callback (MSDN).</a>
         * @see DokanAPI#DokanMapKernelToUserCreateFileFlags(int, int, int, int, IntByReference, IntByReference, IntByReference)
         */
        int invoke(
                WString fileName,
                DokanIOSecurityContext securityContext,
                @EnumSet int desiredAccess,
                @EnumSet int fileAttributes,
                @EnumSet int shareAccess,
                @Enum int createDisposition,
                @EnumSet int createOptions,
                DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface Cleanup extends DokanCallback {

        /**
         * Cleanup Dokan API callback
         * <p>
         * Cleanup request before {@link CloseFile} is called.
         * <p>
         * When {@link DokanFileInfo#deleteOnClose} is {@code true}, the file in Cleanup must be deleted.
         * The function cannot fail therefore the filesystem need to ensure ahead
         * that a deleted can safely happen during Cleanup.
         * See DeleteFile documentation for explanation.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param dokanFileInfo Information about the file or directory.
         * @see DeleteFile
         * @see DeleteDirectory
         */
        void invoke(WString fileName, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface CloseFile extends DokanCallback {

        /**
         * CloseFile Dokan API callback
         * <p>
         * Clean remaining Context
         * <p>
         * CloseFile is called at the end of the life of the context.
         * Anything remaining in {@link DokanFileInfo#context} must be cleared before returning.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param dokanFileInfo Information about the file or directory.
         */
        void invoke(WString fileName, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface ReadFile extends DokanCallback {

        /**
         * ReadFile Dokan API callback
         * <p>
         * ReadFile callback on the file previously opened in {@link ZwCreateFile}.
         * It can be called by different threads at the same time, so the read/context has to be thread safe.
         * <p>
         * When apps make use of memory mapped files, {@link WriteFile} or {@link ReadFile}
         * functions may be invoked after {@link Cleanup} in order to complete the I/O operations.
         * The file system application should also properly work in this case.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param buffer        Read buffer that has to be filled with the read result.
         * @param bufferLength  Buffer length and read size to continue with.
         * @param readLength    Total data size that has been read.
         * @param offset        Offset from where the read has to be continued.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see WriteFile
         */
        int invoke(WString fileName,
                   Pointer buffer,
                   @Unsigned int bufferLength,
                   @Unsigned IntByReference readLength,
                   @Unsigned long offset,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface WriteFile extends DokanCallback {

        /**
         * WriteFile Dokan API callback
         * <p>
         * WriteFile callback on the file previously opened in {@link ZwCreateFile}
         * It can be called by different threads at the same time, sp the write/context has to be thread safe.
         * <p>
         * When apps make use of memory mapped files ( {@link DokanFileInfo#pagingIo} ),
         * {@link WriteFile} or {@link ReadFile}
         * functions may be invoked after {@link Cleanup} in order to complete the I/O operations.
         * The file system application should also properly work in this case.
         * This type of request should follow Windows rules like not extending the current file size.
         *
         * @param fileName             File path requested by the Kernel on the FileSystem.
         * @param buffer               Data that has to be written.
         * @param numberOfBytesToWrite Buffer length and write size to continue with.
         * @param numberOfBytesWritten Total number of bytes that have been written.
         * @param offset               Offset from where to write has to be continued.
         * @param dokanFileInfo        Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see ReadFile
         */
        int invoke(WString fileName,
                   Pointer buffer,
                   @Unsigned int numberOfBytesToWrite,
                   @Unsigned IntByReference numberOfBytesWritten,
                   @Unsigned long offset,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface FlushFileBuffers extends DokanCallback {

        /**
         * FlushFileBuffers Dokan API callback
         * <p>
         * Clears buffers for this context and causes any buffered data to be written to the file.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         */
        int invoke(WString fileName, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface GetFileInformation extends DokanCallback {

        /**
         * GetFileInformation Dokan API callback
         * <p>
         * Get specific information on a file.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param buffer        {@link ByHandleFileInformation} struct to fill.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         */
        int invoke(WString fileName, ByHandleFileInformation buffer, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface FindFiles extends DokanCallback {

        /**
         * FindFiles Dokan API callback
         * <p>
         * List all files in the requested path.
         * {@link FindFilesWithPattern} is checked first. If it is not implemented or
         * returns {@link NTStatus#STATUS_NOT_IMPLEMENTED}, then FindFiles is called, if assigned.
         * It is recommended to have this implemented for performance reason.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param fillFindData  Callback that has to be called with PWIN32_FIND_DATAW that contain file information.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see FindFilesWithPattern
         */
        int invoke(WString fileName, PFillFindData fillFindData, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface FindFilesWithPattern extends DokanCallback {

        /**
         * FindFilesWithPattern Dokan API callback
         * <p>
         * Same as {@link FindFiles} but with a search pattern.
         * <p>
         * The search pattern is a Windows MS-DOS-style expression.
         * It can contain wild cards and extended characters or none of them. See {@link DokanAPI#DokanIsNameInExpression}.
         * <p>
         * If the function is not implemented, {@link FindFiles}
         * will be called instead and the result will be filtered internally by the library.
         * It is recommended to have this implemented for performance reason.
         *
         * @param pathName      Path requested by the Kernel on the FileSystem.
         * @param searchPattern Search pattern.
         * @param fillFindData  Callback that has to be called with PWIN32_FIND_DATAW that contains file information.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see FindFiles
         * @see DokanAPI#DokanIsNameInExpression
         */
        int invoke(WString pathName, WString searchPattern, PFillFindData fillFindData, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface SetFileAttributes extends DokanCallback {

        /**
         * SetFileAttributes Dokan API callback
         * <p>
         * Set file attributes on a specific file
         *
         * @param fileName       File path requested by the Kernel on the FileSystem.
         * @param fileAttributes FileAttributes to set on file.
         * @param dokanFileInfo  Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         */
        int invoke(WString fileName, @EnumSet int fileAttributes, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface SetFileTime extends DokanCallback {

        /**
         * SetFileTime Dokan API callback
         * <p>
         * Set file attributes on a specific file
         *
         * @param fileName       File path requested by the Kernel on the FileSystem.
         * @param creationTime   Creation FILETIME.
         * @param lastAccessTime LastAccess FILETIME.
         * @param lastWriteTime  LastWrite FILETIME.
         * @param dokanFileInfo  Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         */
        int invoke(WString fileName,
                   FILETIME creationTime,
                   FILETIME lastAccessTime,
                   FILETIME lastWriteTime,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface DeleteFile extends DokanCallback {

        /**
         * DeleteFile Dokan API callback
         * <p>
         * Check if it is possible to delete a file.
         * <p>
         * DeleteFile will also be called with {@link DokanFileInfo#deleteOnClose} set to {@code FALSE}
         * to notify the driver when the file is no longer requested to be deleted.
         * <p>
         * The file in DeleteFile should not be deleted, but instead the file
         * must be checked whether it can be deleted,
         * and {@link NTStatus#STATUS_SUCCESS} should be returned (when it can be deleted) or
         * appropriate error codes, such as {@link NTStatus#STATUS_ACCESS_DENIED} or
         * {@link NTStatus#OBJECT_NAME_NOT_FOUND}, should be returned.
         * <p>
         * When {@link NTStatus#STATUS_SUCCESS} is returned, a Cleanup call is received afterward with
         * {@link DokanFileInfo#deleteOnClose} set to {@code TRUE}. Only then must the closing file
         * be deleted.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see DeleteDirectory
         * @see Cleanup
         */
        int invoke(WString fileName, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface DeleteDirectory extends DokanCallback {

        /**
         * DeleteDirectory Dokan API callback
         * <p>
         * Check if it is possible to delete a directory.
         * <p>
         * DeleteDirectory will also be called with {@link DokanFileInfo#deleteOnClose} set to {@code FALSE}
         * to notify the driver when the file is no longer requested to be deleted.
         * <p>
         * The Directory in DeleteDirectory should not be deleted, but instead
         * must be checked whether it can be deleted,
         * and {@link NTStatus#STATUS_SUCCESS} should be returned (when it can be deleted) or
         * appropriate error codes, such as {@link NTStatus#STATUS_ACCESS_DENIED},
         * {@link NTStatus#OBJECT_PATH_NOT_FOUND}, or {@link NTStatus#DIRECTORY_NOT_EMPTY}, should
         * be returned.
         * <p>
         * When {@link NTStatus#STATUS_SUCCESS} is returned, a Cleanup call is received afterward with
         * {@link DokanFileInfo#deleteOnClose} set to {@code TRUE}. Only then must the closing file
         * be deleted.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see DeleteFile
         * @see Cleanup
         */
        int invoke(WString fileName, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface MoveFile extends DokanCallback {

        /**
         * MoveFile Dokan API callback
         * <p>
         * Move a file or directory to a new destination
         *
         * @param fileName          Path for the file to be moved.
         * @param newFileName       Path for the new location of the file.
         * @param replaceIfExisting If destination already exists, can it be replaced?
         * @param dokanFileInfo     Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         */
        //TODO: check this boolean
        int invoke(WString fileName, WString newFileName, boolean replaceIfExisting, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface SetEndOfFile extends DokanCallback {

        /**
         * SetEndOfFile Dokan API callback
         * <p>
         * SetEndOfFile is used to truncate or extend a file (physical file size).
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param byteOffset    File length to set.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/75241cca-3167-472f-8058-a52d77c6bb17">FileEndOfFileInformation (MSDN)</a>
         */
        int invoke(WString fileName, @Unsigned long byteOffset, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface SetAllocationSize extends DokanCallback {

        /**
         * SetAllocationSize Dokan API callback
         * <p>
         * SetAllocationSize is used to truncate or extend a file.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param allocSize     File length to set.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/0201c69b-50db-412d-bab3-dd97aeede13b">FileAllocationInformation (MSDN)</a>
         */
        int callback(WString fileName, @Unsigned long allocSize, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface LockFile extends DokanCallback {

        /**
         * LockFile Dokan API callback
         * <p>
         * Lock file at a specific offset and data length.
         * This is only used if {@link MountOptions#FILELOCK_USER_MODE} is enabled.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param byteOffset    Offset from where the lock has to be continued.
         * @param length        Data length to lock.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see UnlockFile
         */
        int invoke(WString fileName, @Unsigned long byteOffset, @Unsigned long length, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface UnlockFile extends DokanCallback {

        /**
         * UnlockFile Dokan API callback
         * <p>
         * Unlock file at a specific offset and data length.
         * This is only used if {@link MountOptions#FILELOCK_USER_MODE} is enabled.
         *
         * @param fileName      File path requested by the Kernel on the FileSystem.
         * @param byteOffset    Offset from where the lock has to be continued.
         * @param length        Data length to lock.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see LockFile
         */
        int invoke(WString fileName, @Unsigned long byteOffset, @Unsigned long length, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface GetDiskFreeSpace extends DokanCallback {

        /**
         * GetDiskFreeSpace Dokan API callback
         * <p>
         * Retrieves information about the amount of space that is available on a disk volume.
         * It consists of the total amount of space, the total amount of free space, and
         * the total amount of free space available to the user that is associated with the calling thread.
         * <p>
         * Neither GetDiskFreeSpace nor {@link GetVolumeInformation}
         * save the {@link DokanFileInfo#context}.
         * Before these methods are called, {@link ZwCreateFile} may not be called.
         * (ditto {@link CloseFile} and {@link Cleanup})
         *
         * @param freeBytesAvailable     Amount of available space.
         * @param totalNumberOfBytes     Total size of storage space
         * @param totalNumberOfFreeBytes Amount of free space
         * @param dokanFileInfo          Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa364937(v=vs.85).aspx">GetDiskFreeSpaceEx function (MSDN)</a>
         * @see GetVolumeInformation
         */
        int invoke(@Unsigned LongByReference freeBytesAvailable,
                   @Unsigned LongByReference totalNumberOfBytes,
                   @Unsigned LongByReference totalNumberOfFreeBytes,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface GetVolumeInformation extends DokanCallback {

        /**
         * GetVolumeInformation Dokan API callback
         * <p>
         * Retrieves information about the file system and volume associated with the specified root directory.
         * <p>
         * Neither GetVolumeInformation nor GetDiskFreeSpace
         * save the {@link DokanFileInfo#context}.
         * Before these methods are called, {@link ZwCreateFile} may not be called.
         * (ditto {@link CloseFile} and {@link Cleanup})
         * <p>
         * VolumeName length can be anything that fit in the provided buffer.
         * But some Windows component expect it to be no longer than 32 characters
         * that why it is recommended to set a value under this limit.
         * <p>
         * FileSystemName could be anything up to 10 characters.
         * But Windows check few feature availability based on file system name.
         * For this, it is recommended to set NTFS or FAT here.
         * <p>
         * {@link FileSystemAttributes#FILE_READ_ONLY_VOLUME} is automatically added to the
         * FileSystemFlags if {@link MountOptions#WRITE_PROTECT} was
         * specified in DOKAN_OPTIONS when the volume was mounted.
         *
         * @param volumeNameBuffer       A pointer to a buffer that receives the name of a specified volume.
         * @param volumeNameSize         The length of a volume name buffer.
         * @param volumeSerialNumber     A pointer to a variable that receives the volume serial number.
         * @param maximumComponentLength A pointer to a variable that receives the maximum length.
         * @param fileSystemFlags        A pointer to a variable that receives flags associated with the specified file system.
         * @param fileSystemNameBuffer   A pointer to a buffer that receives the name of the file system.
         * @param fileSystemNameSize     The length of the file system name buffer.
         * @param dokanFileInfo          Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa364993(v=vs.85).aspx">GetVolumeInformation function (MSDN)</a>
         * @see GetDiskFreeSpace
         */
        int invoke(Pointer volumeNameBuffer,
                   @Unsigned int volumeNameSize,
                   @Unsigned IntByReference volumeSerialNumber,
                   @Unsigned IntByReference maximumComponentLength,
                   @EnumSet IntByReference fileSystemFlags,
                   Pointer fileSystemNameBuffer,
                   @Unsigned int fileSystemNameSize,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface Mounted extends DokanCallback {

        /**
         * Mounted Dokan API callback
         * <p>
         * Called when Dokan successfully mounts the volume.
         * <p>
         * If {@link MountOptions#MOUNT_MANAGER} is enabled and the drive letter requested is busy,
         * the MountPoint can contain a different drive letter that the mount manager assigned us.
         *
         * @param mountPoint    The mount point assign to the instance.
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result. The value is currently not used by the library.
         * @see Unmounted
         */
        int invoke(WString mountPoint, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface Unmounted extends DokanCallback {

        /**
         * Unmounted Dokan API callback
         * <p>
         * Called when Dokan is unmounting the volume.
         *
         * @param dokanFileInfo Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result. The value is currently not used by the library.
         * @see Mounted
         */
        int invoke(final DokanFileInfo dokanFileInfo);
    }


    @FunctionalInterface
    public interface GetFileSecurity extends DokanCallback {

        /**
         * GetFileSecurity Dokan API callback
         * <p>
         * Get specified information about the security of a file or directory.
         * <p>
         * Return {@link NTStatus#STATUS_NOT_IMPLEMENTED} to let dokan library build a sddl of the current process user with authenticate user rights for context menu.
         * Return {@link NTStatus#BUFFER_OVERFLOW} if buffer size is too small.
         *
         * @param fileName            File path requested by the Kernel on the FileSystem.
         * @param securityInformation A SECURITY_INFORMATION value that identifies the security information being requested.
         * @param securityDescriptor  A pointer to a buffer that receives a copy of the security descriptor of the requested file.
         * @param bufferLength        Specifies the size, in bytes, of the buffer.
         * @param lengthNeeded        A pointer to the variable that receives the number of bytes necessary to store the complete security descriptor.
         * @param dokanFileInfo       Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see SetFileSecurity
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa446639(v=vs.85).aspx">GetFileSecurity function (MSDN)</a>
         * @since Supported since version 0.6.0. The version must be specified in {@link DokanOptions#Version}.
         */
        int invoke(WString fileName,
                   @EnumSet IntByReference securityInformation,
                   Pointer securityDescriptor,
                   @Unsigned int bufferLength,
                   @Unsigned IntByReference lengthNeeded,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface SetFileSecurity extends DokanCallback {

        /**
         * SetFileSecurity Dokan API callback
         * <p>
         * Sets the security of a file or directory object.
         *
         * @param fileName            File path requested by the Kernel on the FileSystem.
         * @param securityInformation Structure that identifies the contents of the security descriptor pointed by \a SecurityDescriptor param.
         * @param securityDescriptor  A pointer to a SECURITY_DESCRIPTOR structure.
         * @param bufferLength        Specifies the size, in bytes, of the buffer.
         * @param dokanFileInfo       Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @see GetFileSecurity
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa379577(v=vs.85).aspx">SetFileSecurity function (MSDN)</a>
         * @since Supported since version 0.6.0. The version must be specified in {@link DokanOptions#Version}.
         */
        int invoke(WString fileName,
                   @EnumSet IntByReference securityInformation,
                   Pointer securityDescriptor,
                   @Unsigned int bufferLength,
                   DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface FindStreams extends DokanCallback {

        /**
         * FindStreams Dokan API callback
         * <p>
         * Retrieve all NTFS Streams information on the file.
         * This is only called if {@link MountOptions#ALT_STREAM} is enabled.
         *
         * @param fileName           File path requested by the Kernel on the FileSystem.
         * @param fillFindStreamData Callback that has to be called with PWIN32_FIND_STREAM_DATA that contain stream information.
         * @param findStreamContext  Context for the event to pass to the callback FillFindStreamData.
         * @param dokanFileInfo      Information about the file or directory.
         * @return {@link NTStatus#STATUS_SUCCESS} on success or NTSTATUS appropriate to the request result.
         * @since Supported since version 0.8.0. The version must be specified in {@link DokanOptions#Version}.
         */
        int invoke(WString fileName, PFillFindStreamData fillFindStreamData, Pointer findStreamContext, DokanFileInfo dokanFileInfo);
    }

    public void setZwCreateFile(DokanOperations.ZwCreateFile zwCreateFile) {
        ZwCreateFile = zwCreateFile;
    }

    public void setCleanup(DokanOperations.Cleanup cleanup) {
        Cleanup = cleanup;
    }

    public void setCloseFile(DokanOperations.CloseFile closeFile) {
        CloseFile = closeFile;
    }

    public void setReadFile(DokanOperations.ReadFile readFile) {
        ReadFile = readFile;
    }

    public void setWriteFile(DokanOperations.WriteFile writeFile) {
        WriteFile = writeFile;
    }

    public void setFlushFileBuffers(DokanOperations.FlushFileBuffers flushFileBuffers) {
        FlushFileBuffers = flushFileBuffers;
    }

    public void setGetFileInformation(DokanOperations.GetFileInformation getFileInformation) {
        GetFileInformation = getFileInformation;
    }

    public void setFindFiles(DokanOperations.FindFiles findFiles) {
        FindFiles = findFiles;
    }

    public void setFindFilesWithPattern(DokanOperations.FindFilesWithPattern findFilesWithPattern) {
        FindFilesWithPattern = findFilesWithPattern;
    }

    public void setSetFileAttributes(DokanOperations.SetFileAttributes setFileAttributes) {
        SetFileAttributes = setFileAttributes;
    }

    public void setSetFileTime(DokanOperations.SetFileTime setFileTime) {
        SetFileTime = setFileTime;
    }

    public void setDeleteFile(DokanOperations.DeleteFile deleteFile) {
        DeleteFile = deleteFile;
    }

    public void setDeleteDirectory(DokanOperations.DeleteDirectory deleteDirectory) {
        DeleteDirectory = deleteDirectory;
    }

    public void setMoveFile(DokanOperations.MoveFile moveFile) {
        MoveFile = moveFile;
    }

    public void setSetEndOfFile(DokanOperations.SetEndOfFile setEndOfFile) {
        SetEndOfFile = setEndOfFile;
    }

    public void setSetAllocationSize(DokanOperations.SetAllocationSize setAllocationSize) {
        SetAllocationSize = setAllocationSize;
    }

    public void setLockFile(DokanOperations.LockFile lockFile) {
        LockFile = lockFile;
    }

    public void setUnlockFile(DokanOperations.UnlockFile unlockFile) {
        UnlockFile = unlockFile;
    }

    public void setGetDiskFreeSpace(DokanOperations.GetDiskFreeSpace getDiskFreeSpace) {
        GetDiskFreeSpace = getDiskFreeSpace;
    }

    public void setGetVolumeInformation(DokanOperations.GetVolumeInformation getVolumeInformation) {
        GetVolumeInformation = getVolumeInformation;
    }

    public void setMounted(DokanOperations.Mounted mounted) {
        Mounted = mounted;
    }

    public void setUnmounted(DokanOperations.Unmounted unmounted) {
        Unmounted = unmounted;
    }

    public void setGetFileSecurity(DokanOperations.GetFileSecurity getFileSecurity) {
        GetFileSecurity = getFileSecurity;
    }

    public void setSetFileSecurity(DokanOperations.SetFileSecurity setFileSecurity) {
        SetFileSecurity = setFileSecurity;
    }

    public void setFindStreams(DokanOperations.FindStreams findStreams) {
        FindStreams = findStreams;
    }

    //-- helper functions --
    @FunctionalInterface
    public interface PFillFindData extends DokanCallback {

        /**
         * FillFindData Used to add an entry in FindFiles operation
         *
         * @return 1 if buffer is full, otherwise 0 (currently it never returns 1)
         */
        int invoke(WIN32_FIND_DATA fillFindData, DokanFileInfo dokanFileInfo);
    }

    @FunctionalInterface
    public interface PFillFindStreamData extends DokanCallback {

        /**
         * FillFindStreamData Used to add an entry in FindStreams
         *
         * @return {@code FALSE} if the buffer is full, otherwise {@code TRUE}
         */
        int invoke(WIN32_FIND_STREAM_DATA fillFindData, DokanFileInfo dokanFileInfo);

        /**
         * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/fileapi/ns-fileapi-win32_find_stream_data">WIN32_FIND_STREAM_DATA structure (fileapi.h)</a>
         */
        @Structure.FieldOrder({"StreamSize", "cStreamName"})
        class WIN32_FIND_STREAM_DATA extends Structure {

            /**
             * A <a href="https://learn.microsoft.com/en-us/windows/win32/api/winnt/ns-winnt-large_integer-r1">LARGE_INTEGER</a> value that specifies the size of the stream, in bytes.
             */
            WinNT.LARGE_INTEGER StreamSize;
            /**
             * The name of the stream. The string name format is ":streamname:$streamtype".
             */
            byte[] cStreamName = new byte[MAX_PATH + 36];
        }
    }
}
