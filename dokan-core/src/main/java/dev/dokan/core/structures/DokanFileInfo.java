package dev.dokan.core.structures;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import dev.dokan.core.DokanAPI;
import dev.dokan.core.nativeannotations.Boolean;
import dev.dokan.core.nativeannotations.Reserved;
import dev.dokan.core.nativeannotations.Unsigned;

/**
 * The DokanFileInfo Struct.
 * <p>
 * For field descriptions, see the <a href="https://github.com/dokan-dev/dokany/blob/master/dokan/dokan.h">DOKAN_FILE_INFO</a>.
 */
@Structure.FieldOrder({"context", "dokanContext", "dokanOptions", "processingContext", "processId", "isDirectory", "deleteOnClose", "pagingIo", "synchronousIo", "noCache", "writeToEndOfFile"})
public class DokanFileInfo extends Structure implements Structure.ByReference {

    /**
     * Context that can be used to carry information between operations.
     * The context can carry whatever type like HANDLE, struct, int,
     * internal reference that will help the implementation understand the request context of the event.
     */
    @Unsigned
    public long context;

    /**
     * Reserved. Used internally by Dokan library. Never modify.
     */
    @Reserved
    public final long dokanContext;

    {
        this.dokanContext = -1;
    }

    /**
     * A pointer to {@link DokanOptions} which was passed to {@link DokanAPI#DokanMain} or {@link DokanAPI#DokanCreateFileSystem}.
     */
    public DokanOptions dokanOptions;

    /**
     * Reserved. Used internally by Dokan library. Never modify.
     * If the processing for the event requires extra data to be associated with it
     * then a pointer to that data can be placed here
     */
    @Reserved
    public final Pointer processingContext;

    {
        this.processingContext = Pointer.NULL;
    }

    /**
     * Process ID for the thread that originally requested a given I/O operation.
     */
    @Unsigned
    public volatile int processId;

    /**
     * Requesting a directory file.
     * Must be set in {@link DokanOperations.ZwCreateFile} if the file appears to be a folder.
     */
    @Boolean
    public byte isDirectory;

    /**
     * Flag if the file has to be deleted during {@link DokanOperations}. Cleanup event.
     */
    @Boolean
    public byte deleteOnClose;

    /**
     * Read or write is paging IO.
     */
    @Boolean
    public byte pagingIo;

    /**
     * Read or write is synchronous IO.
     */
    @Boolean
    public byte synchronousIo;

    /**
     * Read or write directly from data source without cache
     */
    @Boolean
    public byte noCache;

    /**
     * If {@code TRUE}, write to the current end of file instead of using the Offset parameter.
     */
    @Boolean
    public byte writeToEndOfFile;

    public long getProcessId() {
        return Integer.toUnsignedLong(processId);
    }

    public boolean getIsDirectory() {
        return isDirectory != 0;
    }

    public void setIsDirectory(boolean value) {
        this.isDirectory = (byte) (value ? 1 : 0);
    }

    public boolean getDeleteOnClose() {
        return deleteOnClose != 0;
    }

    public boolean getPagingIo() {
        return pagingIo != 0;
    }

    public boolean getSynchronousIo() {
        return synchronousIo != 0;
    }

    public boolean getNoCache() {
        return noCache != 0;
    }

    public boolean getWriteToEndOfFile() {
        return writeToEndOfFile != 0;
    }
}
