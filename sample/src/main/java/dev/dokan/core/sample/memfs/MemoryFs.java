package dev.dokan.core.sample.memfs;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase;
import dev.dokan.core.DokanFileSystem;
import dev.dokan.core.NTStatus;
import dev.dokan.core.constants.CreateOptions;
import dev.dokan.core.enums.CreateDisposition;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Out;
import dev.dokan.core.structures.ByHandleFileInformation;
import dev.dokan.core.structures.DokanFileInfo;
import dev.dokan.core.structures.DokanIOSecurityContext;
import dev.dokan.core.structures.DokanOperations;

import java.util.concurrent.atomic.AtomicLong;

public class MemoryFs implements DokanFileSystem {

    private final ResourceManager resourceManager;
    private final AtomicLong handleGenerator = new AtomicLong(0);

    public MemoryFs() {
        this.resourceManager = new ResourceManager();
    }

    public MemoryFs(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public int zwCreateFile(WString path, DokanIOSecurityContext securityContext, @EnumSet int desiredAccess, @EnumSet int fileAttributes, @EnumSet int shareAccess, int createDisposition, @EnumSet int createOptions, DokanFileInfo dokanFileInfo) {
        final MemoryPath memoryPath = MemoryPath.of(path.toString());
        if (!isValid(memoryPath)) {
            return NTStatus.OBJECT_NAME_INVALID;
        }
		/*
			We just count every zwCreateFile call.
			Long stores up to 2^64 values.
			If we assume 2^20 createFile calls per second (~ 1 million/s), we can continue counting without duplicates for 557844 yrs
		 */
        dokanFileInfo.context = handleGenerator.incrementAndGet();

        var createDispositionEnum = CreateDisposition.of(createDisposition);
        Resource resource = resourceManager.get(memoryPath);

        //TODO: validate input
        //TODO: ensure that file names can be at most MAX_PATH-1 long

        if (resource != null) {
            return switch (resource.getType()) {
                case FILE ->
                        handleExistingFile(memoryPath, (File) resource, createDispositionEnum, createOptions, fileAttributes);
                case DIR ->
                        handleExistingDir(memoryPath, (Directory) resource, createDispositionEnum, createOptions, fileAttributes, dokanFileInfo);
            };
        } else {
            boolean createDir = (createOptions & CreateOptions.FILE_DIRECTORY_FILE) != 0;
            boolean createFile = (createOptions & CreateOptions.FILE_NON_DIRECTORY_FILE) != 0;
            if (createFile && createDir) {
                return NTStatus.INVALID_PARAMETER;
            } else if (createDir) {
                return handleNewDirectory(memoryPath, createDispositionEnum, createOptions, fileAttributes);
            } else if (createFile) {
                return handleNewFile(memoryPath, createDispositionEnum, createOptions, fileAttributes);
            } else {
                return NTStatus.UNSUCCESSFUL;
            }
        }
    }

    @Override
    public void cleanup(WString path, DokanFileInfo dokanFileInfo) {
        final MemoryPath memoryPath = MemoryPath.of(path.toString());
        if (dokanFileInfo.context == 0) {
            return;
        }

        if (dokanFileInfo.getDeleteOnClose()) {
            resourceManager.remove(memoryPath);
        }
    }

    @Override
    public void closeFile(WString path, DokanFileInfo dokanFileInfo) {
        dokanFileInfo.context = 0L;
    }

    @Override
    public int findFiles(WString path, DokanOperations.PFillFindData fillFindDataCallback, DokanFileInfo dokanFileInfo) {
        final MemoryPath memoryPath = MemoryPath.of(path.toString());
        if (resourceManager.get(memoryPath) instanceof Directory directory) {
            directory.list().forEach(resource -> fillFindDataCallback.invoke(resource.toFIND_DATAStruct(), dokanFileInfo));
            return NTStatus.STATUS_SUCCESS;
        } else {
            return NTStatus.UNSUCCESSFUL;
        }
    }

    @Override
    public int getFileInformation(WString path, @Out ByHandleFileInformation handleFileInfo, DokanFileInfo dokanFileInfo) {
        var memoryPath = MemoryPath.of(path.toString());
        Resource resource = resourceManager.get(memoryPath);
        if (resource != null) {
            resource.writeTo(handleFileInfo);
            return NTStatus.STATUS_SUCCESS;
        } else {
            return NTStatus.NO_SUCH_FILE;
        }
    }

    private int handleExistingFile(MemoryPath memoryPath, File file, CreateDisposition createDisposition, int createOptions, int fileAttributes) {
        return switch (createDisposition) {
            case CREATE -> NTStatus.OBJECT_NAME_COLLISION;
            case OPEN, OPEN_IF -> //open file
                    NTStatus.STATUS_SUCCESS;
            case OVERWRITE, OVERWRITE_IF -> {
                file.wipe();
                file.setAttributes(fileAttributes);
                yield NTStatus.STATUS_SUCCESS;
            }
            case SUPERSEDE -> {
                if ((createOptions & CreateOptions.FILE_DIRECTORY_FILE) == 0) {
                    resourceManager.put(memoryPath, new File(memoryPath.getFileName().toString(), fileAttributes));
                } else {
                    resourceManager.put(memoryPath, new Directory(memoryPath.getFileName().toString(), fileAttributes));
                }
                yield NTStatus.STATUS_SUCCESS;
            }
        };
    }

    private int handleExistingDir(MemoryPath memoryPath, Directory directory, CreateDisposition createDisposition, int createOptions, int fileAttributes, DokanFileInfo dokanFileInfo) {
        if ((createOptions & CreateOptions.FILE_NON_DIRECTORY_FILE) != 0) {
            return NTStatus.FILE_IS_A_DIRECTORY;
        }

        dokanFileInfo.setIsDirectory(true); //according to the docs, this must be set
        return switch (createDisposition) {
            case CREATE -> NTStatus.OBJECT_NAME_COLLISION;
            case OPEN, OPEN_IF -> NTStatus.STATUS_SUCCESS;
            case OVERWRITE, OVERWRITE_IF, SUPERSEDE -> NTStatus.STATUS_ACCESS_DENIED;
        };
    }

    private int handleNewFile(MemoryPath path, CreateDisposition createDisposition, int createOptions, int fileAttributes) {
        return switch (createDisposition) {
            case CREATE, OPEN_IF, OVERWRITE_IF, SUPERSEDE -> {
                resourceManager.put(path, new File(path.getFileName().toString(), fileAttributes));
                yield NTStatus.STATUS_SUCCESS;
            }
            case OPEN, OVERWRITE -> NTStatus.NO_SUCH_FILE;
        };
    }

    private int handleNewDirectory(MemoryPath path, CreateDisposition createDisposition, int createOptions, int fileAttributes) {
        return switch (createDisposition) {
            case CREATE, OPEN_IF -> {
                resourceManager.put(path, new Directory(path.getFileName().toString(), fileAttributes));
                yield NTStatus.STATUS_SUCCESS;
            }
            case OPEN -> NTStatus.NO_SUCH_FILE;
            case OVERWRITE, OVERWRITE_IF, SUPERSEDE -> NTStatus.STATUS_ACCESS_DENIED;
        };
    }

    private boolean isValid(MemoryPath memoryPath) {
        try {
            return MemoryPath.ROOT.equals(memoryPath)
                    || memoryPath.getFileName().toString().length() < WinBase.MAX_PATH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
