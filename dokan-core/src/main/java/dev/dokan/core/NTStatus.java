package dev.dokan.core;

/**
 * @see <a href="https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/596a1078-e883-4972-9bbc-49e60bebca55">NTSTATUS</a>
 */
public interface NTStatus extends com.sun.jna.platform.win32.NTStatus {

    int STATUS_NOT_IMPLEMENTED = 0xC0000002;
    int NO_SUCH_FILE = 0xC000000F;
    int IO_DEVICE_ERROR = 0xC0000185;
    int NOT_A_DIRECTORY = 0xC0000103;
    int OBJECT_NAME_COLLISION = 0xC0000035;
    int NAME_TOO_LONG = 0xC0000106;
    int CANNOT_DELETE = 0xC0000121;
    int INVALID_HANDLE = 0xC0000008;
    int FILE_IS_A_DIRECTORY = 0xC00000BA;
    int UNSUCCESSFUL = 0xC0000001;
    int DISK_QUOTA_EXCEEDED = 0xC0000802;
    int OBJECT_NAME_INVALID = 0xC0000033;
    int OBJECT_NAME_NOT_FOUND = 0xC0000034;
    int OBJECT_PATH_NOT_FOUND = 0xc000003a;
    int DIRECTORY_NOT_EMPTY = 0xc0000101;
    int BUFFER_OVERFLOW = 0x80000005;
    int INVALID_PARAMETER = 0xC000000D;
}
