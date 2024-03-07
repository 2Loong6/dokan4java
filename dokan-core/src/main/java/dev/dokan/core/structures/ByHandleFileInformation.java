package dev.dokan.core.structures;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Unsigned;

/**
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/fileapi/ns-fileapi-by_handle_file_information">BY_HANDLE_FILE_INFORMATION structure (fileapi.h)</a>
 */
@Structure.FieldOrder({"dwFileAttributes", "ftCreationTime", "ftLastAccessTime", "ftLastWriteTime", "dwVolumeSerialNumber", "nFileSizeHigh", "nFileSizeLow", "nNumberOfLinks", "nFileIndexHigh", "nFileIndexLow"})
public class ByHandleFileInformation extends Structure {

    /**
     * The file attributes.
     * For possible values and their descriptions, see <a href="https://learn.microsoft.com/en-us/windows/desktop/FileIO/file-attribute-constants">File Attribute Constants.</a>
     */
    @EnumSet
    public int dwFileAttributes;
    /**
     * A <a href="https://learn.microsoft.com/en-us/windows/desktop/api/minwinbase/ns-minwinbase-filetime">FILETIME</a> structure that specifies when a file or directory is created.
     * If the underlying file system does not support creation time,
     * this member is zero (0).
     */
    public WinBase.FILETIME ftCreationTime;
    /**
     * A <a href="https://learn.microsoft.com/en-us/windows/desktop/api/minwinbase/ns-minwinbase-filetime">FILETIME</a> structure.
     * For a file, the structure specifies the last time that a file is read from or written to.
     * For a directory, the structure specifies when the directory is created.
     * <p>
     * For both files and directories, the specified date is correct, but the time of day is always set to midnight. If the underlying file system does not support the last access time, this member is zero (0).
     */
    public WinBase.FILETIME ftLastAccessTime;
    /**
     * A <a href="https://learn.microsoft.com/en-us/windows/desktop/api/minwinbase/ns-minwinbase-filetime">FILETIME</a> structure.
     * For a file, the structure specifies the last time that a file is written to.
     * For a directory, the structure specifies when the directory is created.
     * If the underlying file system does not support the last write time, this member is zero (0).
     */
    public WinBase.FILETIME ftLastWriteTime;
    /**
     * The serial number of the volume that contains a file.
     */
    @Unsigned
    public int dwVolumeSerialNumber;
    /**
     * The high-order part of the file size.
     */
    @Unsigned
    public int nFileSizeHigh;
    /**
     * The low-order part of the file size.
     */
    @Unsigned
    public int nFileSizeLow;
    /**
     * The number of links to this file.
     * For the FAT file system this member is always 1.
     * For the NTFS file system, it can be more than 1.
     */
    @Unsigned
    public int nNumberOfLinks;
    /**
     * The high-order part of a unique identifier that is associated with a file. For more information, see {@link #nFileIndexLow}.
     */
    @Unsigned
    public int nFileIndexHigh;
    /**
     * The low-order part of a unique identifier that is associated with a file.
     * <p>
     * The identifier (low and high parts) and the volume serial number uniquely identify a file on a single computer.
     * To determine whether two open handles represent the same file,
     * combine the identifier and the volume serial number for each file and compare them.
     * <p>
     * The ReFS file system, introduced with Windows Server 2012, includes 128-bit file identifiers.
     * To retrieve the 128-bit file identifier use the <a href="https://learn.microsoft.com/en-us/windows/desktop/api/winbase/nf-winbase-getfileinformationbyhandleex">GetFileInformationByHandleEx</a> function with FileIdInfo to retrieve the <a href="https://learn.microsoft.com/en-us/windows/desktop/api/winbase/ns-winbase-file_id_info">FILE_ID_INFO</a> structure.
     * The 64-bit identifier in this structure is not guaranteed to be unique on ReFS.
     */
    @Unsigned
    public int nFileIndexLow;

    public long getDwFileAttributes() {
        return Integer.toUnsignedLong(dwFileAttributes);
    }

    public long getDwVolumeSerialNumber() {
        return Integer.toUnsignedLong(dwVolumeSerialNumber);
    }

    public long getnFileSizeHigh() {
        return Integer.toUnsignedLong(nFileSizeHigh);
    }

    public long getnFileSizeLow() {
        return Integer.toUnsignedLong(nFileSizeLow);
    }

    public long getnNumberOfLinks() {
        return Integer.toUnsignedLong(nNumberOfLinks);
    }

    public long getnFileIndexHigh() {
        return Integer.toUnsignedLong(nFileIndexHigh);
    }

    public long getnFileIndexLow() {
        return Integer.toUnsignedLong(nFileIndexLow);
    }

    public void setFileIndex(long index) {
        nFileIndexLow = (int) index;
        nFileIndexHigh = (int) (index >>> 32);
    }

    public void setFileSize(long size) {
        nFileSizeLow = (int) size;
        nFileSizeHigh = (int) (size >>> 32);
    }
}
