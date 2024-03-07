package dev.dokan.core;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinNT;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Unsigned;
import dev.dokan.core.structures.DokanOperations;
import dev.dokan.core.structures.DokanOptions;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.dokan.core.constants.DokanMountReturnValues.*;

/**
 * Class to mount a {@link DokanFileSystem}.
 * <p>
 * When using this class, there is no need to call {@link DokanAPI#DokanInit} or {@link DokanAPI#DokanShutdown}. In contrast, it can even lead to illegal memory accesses.
 * <p>
 * {@link DokanAPI#DokanInit} is called when this class is loaded, and for DokanShutdown a shutdownHook via {@link Runtime#addShutdownHook} is added to the JVM.
 */
public class DokanMount implements AutoCloseable {

    static {
        DokanAPI.DokanInit();
        Runtime.getRuntime().addShutdownHook(new Thread(DokanAPI::DokanShutdown));
    }

    private final DokanOperations dokanOperations;
    private final DokanOptions dokanOptions;
    private final CallbackThreadInitializer callbackThreadInitializer;
    private final Pointer memoryContainingHandle;

    private volatile boolean isUnmounted;

    private DokanMount(DokanOperations dokanOperations, DokanOptions dokanOptions, Memory dokanInstanceHandle, CallbackThreadInitializer callbackThreadInitializer) {
        this.dokanOperations = dokanOperations;
        this.dokanOptions = dokanOptions;
        this.callbackThreadInitializer = callbackThreadInitializer;
        this.memoryContainingHandle = dokanInstanceHandle;
        this.isUnmounted = false;
    }

    public static Mounter create(DokanFileSystem fs) {
        return new Mounter(fs);
    }

    public synchronized void unmount() {
        if (isUnmounted) {
            return;
        }

        if (isRunning()) {
            DokanAPI.DokanCloseHandle(memoryContainingHandle.getPointer(0));
        }
        this.memoryContainingHandle.clear(Native.POINTER_SIZE);
        this.isUnmounted = true;
    }

    @Override
    public void close() {
        unmount();
    }

    public synchronized boolean isRunning() {
        return DokanAPI.DokanIsFileSystemRunning(memoryContainingHandle.getPointer(0));
    }

    public static class Mounter {
        private final DokanFileSystem fs;
        private final DokanOptions.Builder optionsBuilder;

        Mounter(DokanFileSystem fs) {
            this.fs = fs;
            this.optionsBuilder = DokanOptions.create();
        }

        public Mounter withSingleThreaded(boolean useOneThread) {
            optionsBuilder.withSingleThreadEnabled(useOneThread);
            return this;
        }

        public Mounter withOptions(@EnumSet int mountOptions) {
            optionsBuilder.withOptions(mountOptions);
            return this;
        }

        public Mounter withGlobalContext(@Unsigned long globalContext) {
            optionsBuilder.withGlobalContext(globalContext);
            return this;
        }

        public Mounter withMountPath(Path mountPath) {
            optionsBuilder.withMountPoint(mountPath);
            return this;
        }

        public Mounter withUncName(String uncName) {
            optionsBuilder.withUncName(uncName);
            return this;
        }

        public Mounter withTimeout(@Unsigned int timeout) {
            optionsBuilder.withTimeout(timeout);
            return this;
        }

        public Mounter withAllocationUnitSize(@Unsigned int allocationUnitSize) {
            optionsBuilder.withAllocationUnitSize(allocationUnitSize);
            return this;
        }

        public Mounter withSectorSize(@Unsigned int sectorSize) {
            optionsBuilder.withSectorSize(sectorSize);
            return this;
        }

        public Mounter withSecurityDescriptor(WinNT.SECURITY_DESCRIPTOR descriptor) {
            optionsBuilder.withSecurityDescriptor(descriptor);
            return this;
        }

        public DokanMount mount() throws DokanException {
            var callbackThreadInitializer = new DokanCallbackThreadInitializer("dokan-");
            var dokanOperations = extractImplementedMethods(fs, callbackThreadInitializer);
            var dokanOptions = optionsBuilder.build();
            var memoryContainingHandle = new Memory(Native.POINTER_SIZE);
            memoryContainingHandle.clear(Native.POINTER_SIZE);

            int result = DokanAPI.DokanCreateFileSystem(dokanOptions, dokanOperations, memoryContainingHandle);
            switch (result) {
                case DOKAN_SUCCESS -> {
                }
                case DOKAN_ERROR -> throw new DokanException("Error");
                case DOKAN_DRIVE_LETTER_ERROR -> throw new DokanException("Bad Drive letter");
                case DOKAN_DRIVER_INSTALL_ERROR -> throw new DokanException("Can't install driver");
                case DOKAN_START_ERROR -> throw new DokanException("Driver something wrong");
                case DOKAN_MOUNT_ERROR -> throw new DokanException("Can't assign a drive letter");
                case DOKAN_MOUNT_POINT_ERROR -> throw new DokanException("Mount point error");
                case DOKAN_VERSION_ERROR -> throw new DokanException("Version error");
                default -> throw new DokanException("DokanCreateFileSystem returned non-zero result: " + result);
            }

            return new DokanMount(dokanOperations, dokanOptions, memoryContainingHandle, callbackThreadInitializer);
        }
    }

    private static class DokanCallbackThreadInitializer extends CallbackThreadInitializer {

        private String prefix;
        private final AtomicInteger counter;

        DokanCallbackThreadInitializer(String prefix) {
            super(true, false, prefix, new ThreadGroup(prefix));
            this.prefix = prefix;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public String getName(Callback cb) {
            return prefix + counter.getAndIncrement();
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    private static DokanOperations extractImplementedMethods(DokanFileSystem fs, DokanCallbackThreadInitializer callbackThreadInitializer) {
        Set<String> notImplementedMethods = Arrays.stream(fs.getClass().getMethods()).filter(method -> method.getAnnotation(NotImplemented.class) != null).map(Method::getName).collect(Collectors.toSet());
        DokanOperations dokanOperations = new DokanOperations();

        if (!notImplementedMethods.contains("zwCreateFile")) {
            dokanOperations.setZwCreateFile(fs::zwCreateFile);
            Native.setCallbackThreadInitializer(dokanOperations.ZwCreateFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("cleanup")) {
            dokanOperations.setCleanup(fs::cleanup);
            Native.setCallbackThreadInitializer(dokanOperations.Cleanup, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("closeFile")) {
            dokanOperations.setCloseFile(fs::closeFile);
            Native.setCallbackThreadInitializer(dokanOperations.CloseFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("readFile")) {
            dokanOperations.setReadFile(fs::readFile);
            Native.setCallbackThreadInitializer(dokanOperations.ReadFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("writeFile")) {
            dokanOperations.setWriteFile(fs::writeFile);
            Native.setCallbackThreadInitializer(dokanOperations.WriteFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("flushFileBuffer")) {
            dokanOperations.setFlushFileBuffers(fs::flushFileBuffers);
            Native.setCallbackThreadInitializer(dokanOperations.FlushFileBuffers, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("getFileInformation")) {
            dokanOperations.setGetFileInformation(fs::getFileInformation);
            Native.setCallbackThreadInitializer(dokanOperations.GetFileInformation, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("findFiles")) {
            dokanOperations.setFindFiles(fs::findFiles);
            Native.setCallbackThreadInitializer(dokanOperations.FindFiles, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("findFilesWithPattern")) {
            dokanOperations.setFindFilesWithPattern(fs::findFilesWithPattern);
            Native.setCallbackThreadInitializer(dokanOperations.FindFilesWithPattern, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("setFileAttributes")) {
            dokanOperations.setSetFileAttributes(fs::setFileAttributes);
            Native.setCallbackThreadInitializer(dokanOperations.SetFileAttributes, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("setFileTime")) {
            dokanOperations.setSetFileTime(fs::setFileTime);
            Native.setCallbackThreadInitializer(dokanOperations.SetFileTime, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("deleteFile")) {
            dokanOperations.setDeleteFile(fs::deleteFile);
            Native.setCallbackThreadInitializer(dokanOperations.DeleteFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("deleteDirectory")) {
            dokanOperations.setDeleteDirectory(fs::deleteDirectory);
            Native.setCallbackThreadInitializer(dokanOperations.DeleteDirectory, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("moveFile")) {
            dokanOperations.setMoveFile(fs::moveFile);
            Native.setCallbackThreadInitializer(dokanOperations.MoveFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("setEndOfFile")) {
            dokanOperations.setSetEndOfFile(fs::setEndOfFile);
            Native.setCallbackThreadInitializer(dokanOperations.SetEndOfFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("setAllocationSize")) {
            dokanOperations.setSetAllocationSize(fs::setAllocationSize);
            Native.setCallbackThreadInitializer(dokanOperations.SetAllocationSize, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("lockFile")) {
            dokanOperations.setLockFile(fs::lockFile);
            Native.setCallbackThreadInitializer(dokanOperations.LockFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("unlockFile")) {
            dokanOperations.setUnlockFile(fs::unlockFile);
            Native.setCallbackThreadInitializer(dokanOperations.UnlockFile, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("getDiskFreeSpace")) {
            dokanOperations.setGetDiskFreeSpace(fs::getDiskFreeSpace);
            Native.setCallbackThreadInitializer(dokanOperations.GetDiskFreeSpace, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("getVolumeInformation")) {
            dokanOperations.setGetVolumeInformation(fs::getVolumeInformation);
            Native.setCallbackThreadInitializer(dokanOperations.GetVolumeInformation, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("mounted")) {
            dokanOperations.setMounted(fs::mounted);
            Native.setCallbackThreadInitializer(dokanOperations.Mounted, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("unmounted")) {
            dokanOperations.setUnmounted(fs::unmounted);
            Native.setCallbackThreadInitializer(dokanOperations.Unmounted, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("getFileSecurity")) {
            dokanOperations.setGetFileSecurity(fs::getFileSecurity);
            Native.setCallbackThreadInitializer(dokanOperations.GetFileSecurity, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("setFileSecurity")) {
            dokanOperations.setSetFileSecurity(fs::setFileSecurity);
            Native.setCallbackThreadInitializer(dokanOperations.SetFileSecurity, callbackThreadInitializer);
        }
        if (!notImplementedMethods.contains("findStreams")) {
            dokanOperations.setFindStreams(fs::findStreams);
            Native.setCallbackThreadInitializer(dokanOperations.FindStreams, callbackThreadInitializer);
        }
        return dokanOperations;
    }
}
