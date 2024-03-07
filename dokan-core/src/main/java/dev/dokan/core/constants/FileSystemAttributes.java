package dev.dokan.core.constants;

import dev.dokan.core.DokanFileSystem;

/**
 * Constants for indicating supported file system features.
 * To be used for the {@code fileSystemFlags} parameter in a {@link DokanFileSystem#getVolumeInformation} call.
 * <p>
 *
 * @see <a href="https://docs.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/ns-ntifs-_file_fs_attribute_information">FILE_FS_ATTRIBUTE_INFORMATION structure (ntifs.h)</a>
 */
public interface FileSystemAttributes {

    /**
     * The file system supports case-sensitive file names.
     */
    int FILE_CASE_SENSITIVE_SEARCH = 0x00000001;

    /**
     * The file system preserves the case of file names when it places a name on disk.
     */
    int FILE_CASE_PRESERVED_NAMES = 0x00000002;

    /**
     * The file system supports <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-smb/c7d64f17-1ab6-4151-b9e8-f15813235c83#gt_c305d0ab-8b94-461a-bd76-13b40cb8c4d8">Unicode</a> in file names.
     */
    int FILE_UNICODE_ON_DISK = 0x00000004;

    /**
     * The file system preserves and enforces access control lists (<a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/wdm/ns-wdm-_acl">ACLs</a>).
     */
    int FILE_PERSISTENT_ACLS = 0x00000008;

    /**
     * The file system supports file-based compression.
     * This flag is incompatible with the <b>FILE_VOLUME_IS_COMPRESSED</b> flag.
     * This flag does not affect how data is transferred over the network.
     */
    int FILE_FILE_COMPRESSION = 0x00000010;

    /**
     * The file system supports per-user quotas.
     */
    int FILE_VOLUME_QUOTAS = 0x00000020;

    /**
     * The file system supports <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-smb/c7d64f17-1ab6-4151-b9e8-f15813235c83#gt_4fed0b53-5fc8-4818-886f-93d87f3035e1">sparse files</a>.
     */
    int FILE_SUPPORTS_SPARSE_FILES = 0x00000040;

    /**
     * The file system supports reparse points.
     */
    int FILE_SUPPORTS_REPARSE_POINTS = 0x00000080;

    /**
     * The file system supports remote storage.
     */
    int FILE_SUPPORTS_REMOTE_STORAGE = 0x00000100;

    /**
     * On a successful cleanup operation,
     * the file system returns information that describes additional actions taken during cleanup,
     * such as deleting the file. File system filters can examine this information in their post-cleanup callback.
     */
    int FILE_RETURNS_CLEANUP_RESULT_INFO = 0x00000200;

    /**
     * The file system supports POSIX-style delete and rename operations.
     */
    int FILE_SUPPORTS_POSIX_UNLINK_RENAME = 0x00000400;

    /**
     * The specified volume is a compressed volume.
     * This flag is incompatible with the <b>FILE_FILE_COMPRESSION</b> flag.
     * This does not affect how data is transferred over the network.
     */
    int FILE_VOLUME_IS_COMPRESSED = 0x00008000;

    /**
     * The file system supports object identifiers.
     */
    int FILE_SUPPORTS_OBJECT_IDS = 0x00010000;

    /**
     * The file system supports encryption.
     */
    int FILE_SUPPORTS_ENCRYPTION = 0x00020000;

    /**
     * The file system supports named <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-smb/c7d64f17-1ab6-4151-b9e8-f15813235c83#gt_f3529cd8-50da-4f36-aa0b-66af455edbb6">data streams</a> for a file.
     */
    int FILE_NAMED_STREAMS = 0x00040000;

    /**
     * The specified volume is read-only.
     */
    int FILE_READ_ONLY_VOLUME = 0x00080000;

    /**
     * The specified volume can be written to one time only. The write must be performed in sequential order.
     */
    int FILE_SEQUENTIAL_WRITE_ONCE = 0x00100000;

    /**
     * The file system supports <a href="https://learn.microsoft.com/en-us/windows/win32/fileio/transactional-ntfs-portal">transaction processing</a>.
     */
    int FILE_SUPPORTS_TRANSACTIONS = 0x00200000;

    /**
     * The file system supports direct links to other devices and partitions.
     */
    int FILE_SUPPORTS_HARD_LINKS = 0x00400000;

    /**
     * The file system supports extended attributes (EAs).
     */
    int FILE_SUPPORTS_EXTENDED_ATTRIBUTES = 0x00800000;

    /**
     * The file system supports open by file ID.
     */
    int FILE_SUPPORTS_OPEN_BY_FILE_ID = 0x01000000;

    /**
     * The file system supports update sequence number (USN) journals.
     */
    int FILE_SUPPORTS_USN_JOURNAL = 0x02000000;

    /**
     * The file system supports <a href="https://learn.microsoft.com/en-us/windows-server/storage/refs/integrity-streams">integrity streams</a>.
     */
    int FILE_SUPPORTS_INTEGRITY_STREAMS = 0x04000000;

    /**
     * The file system supports <a href="https://learn.microsoft.com/en-us/windows/win32/fileio/block-cloning">block cloning</a>,
     * that is, sharing logical clusters between files on the same volume.
     * The file system reallocates on writes to shared clusters.
     */
    int FILE_SUPPORTS_BLOCK_REFCOUNTING = 0x08000000;

    /**
     * The file system tracks whether each cluster of a file contains valid data (either from explicit file writes or automatic zeros)
     * or invalid data (has not yet been written to or zeroed).
     * File systems that use sparse valid data length (VDL) do not store a valid data length
     * and do not require that valid data be contiguous within a file.
     */
    int FILE_SUPPORTS_SPARSE_VDL = 0x10000000;

    /**
     * The specified volume is a direct access (DAX) volume.
     */
    int FILE_DAX_VOLUME = 0x20000000;

    /**
     * The file system supports ghosting.
     */
    int FILE_SUPPORTS_GHOSTING = 0x40000000;
}
