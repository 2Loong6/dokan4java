package dev.dokan.core.structures;

import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinNT;
import dev.dokan.core.constants.MountOptions;
import dev.dokan.core.nativeannotations.Boolean;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.nativeannotations.Unsigned;

import java.nio.file.Path;

/**
 * Dokan mount options used to describe Dokan device behavior.
 *
 * @see <a href="https://github.com/dokan-dev/dokany/blob/master/dokan/dokan.h">dokan.h</a>
 */
@Structure.FieldOrder({"Version", "SingleThread", "Options", "GlobalContext", "MountPoint", "UNCName", "Timeout", "AllocationUnitSize", "SectorSize", "VolumeSecurityDescriptorLength", "VolumeSecurityDescriptor"})
public class DokanOptions extends Structure implements Structure.ByReference {

    private static final int VOLUME_SECURITY_DESCRIPTOR_MAX_SIZE = 1024 * 16;

    /**
     * Version of the Dokan features requested without dots (version "123" is equal to Dokan version 1.2.3).
     */
    @Unsigned
    public volatile short Version;

    /**
     * Only use a single thread to process events. This is highly not recommended as can easily create a bottleneck.
     */
    @Boolean
    public volatile byte SingleThread;

    /**
     * Features enabled for the mount. See {@link MountOptions}
     */
    @EnumSet
    public volatile int Options;

    /**
     * FileSystem can store anything here.
     * TODO: maybe make this non-volatile
     */
    @Unsigned
    public volatile long GlobalContext;

    /**
     * Mount point. It can be a driver letter like "M:\" or a folder path "C:\mount\dokan" on a NTFS partition.
     */
    public volatile WString MountPoint;

    /**
     * UNC Name for the Network Redirector
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/hardware/ff556761(v=vs.85).aspx">Support for UNC Naming</a>
     */
    public volatile WString UNCName;

    /**
     * Max timeout in milliseconds of each request before Dokan gives up to wait events to complete.
     * A timeout request is a sign that the userland implementation is no longer able to properly manage requests in time.
     * The driver will therefore unmount the device when a timeout trigger in order to keep the system stable.
     * The default timeout value is 15 seconds.
     */
    @Unsigned
    public volatile int Timeout;

    /**
     * Allocation Unit Size of the volume. This will affect the file size.
     */
    @Unsigned
    public volatile int AllocationUnitSize;

    /**
     * Sector Size of the volume. This will affect the file size.
     */
    @Unsigned
    public volatile int SectorSize;

    /**
     * Length of the optional VolumeSecurityDescriptor provided. Set 0 will disable the option.
     */
    @Unsigned
    public volatile int VolumeSecurityDescriptorLength;

    /**
     * Optional Volume Security descriptor.
     *
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-initializesecuritydescriptor">InitializeSecurityDescriptor</a>
     */
    public volatile byte[] VolumeSecurityDescriptor = new byte[VOLUME_SECURITY_DESCRIPTOR_MAX_SIZE];

    public int getVersion() {
        return Short.toUnsignedInt(Version);
    }

    public boolean getSingleThread() {
        return SingleThread != 0;
    }

    public long getOptions() {
        return Integer.toUnsignedLong(Options);
    }

    public long getTimeout() {
        return Integer.toUnsignedLong(Timeout);
    }

    public long getAllocationUnitSize() {
        return Integer.toUnsignedLong(AllocationUnitSize);
    }

    public long getSectorSize() {
        return Integer.toUnsignedLong(SectorSize);
    }

    public long getVolumeSecurityDescriptorLength() {
        return Integer.toUnsignedLong(VolumeSecurityDescriptorLength);
    }

    public static Builder create() {
        return new Builder();
    }

    public static final class Builder {
        @Unsigned
        private short version = 210;
        @Boolean
        private byte singleThread = 0x00;
        @EnumSet
        private int options = MountOptions.MOUNT_MANAGER;
        @Unsigned
        private long globalContext = 0;
        private WString mountPoint = new WString("");
        private WString uncName = new WString("");
        @Unsigned
        private int timeout = 5000;
        @Unsigned
        private int allocationUnitSize = 4096; //default ntfs is 4KB
        @Unsigned
        private int sectorSize = 4096;
        @Unsigned
        private int volumeSecurityDescriptorLength = 0;
        private byte[] volumeSecurityDescriptor = new byte[VOLUME_SECURITY_DESCRIPTOR_MAX_SIZE];

        private final DokanOptions dokanOptions = new DokanOptions();

        private Builder() {
        }

        public Builder withSingleThreadEnabled(boolean isSingleThreaded) {
            if (isSingleThreaded) {
                this.singleThread = 0x01;
            }
            return this;
        }

        public Builder withOptions(@EnumSet int options) {
            this.options = options;
            return this;
        }

        public Builder withGlobalContext(@Unsigned long globalContext) {
            this.globalContext = globalContext;
            return this;
        }

        public Builder withMountPoint(Path mountPoint) {
            this.mountPoint = new WString(mountPoint.toAbsolutePath().toString());
            return this;
        }

        public Builder withUncName(String uncName) {
            this.uncName = new WString(uncName);
            return this;
        }

        public Builder withTimeout(@Unsigned int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withAllocationUnitSize(@Unsigned int allocationUnitSize) {
            this.allocationUnitSize = allocationUnitSize;
            return this;
        }

        public Builder withSectorSize(@Unsigned int sectorSize) {
            this.sectorSize = sectorSize;
            return this;
        }

        public Builder withSecurityDescriptor(WinNT.SECURITY_DESCRIPTOR descriptor) {
            if (descriptor.data.length > this.volumeSecurityDescriptor.length) {
                throw new IllegalArgumentException("Given security descriptor is too big");
            }
            System.arraycopy(descriptor.data, 0, volumeSecurityDescriptor, 0, descriptor.data.length);
            this.volumeSecurityDescriptorLength = descriptor.data.length;
            return this;
        }

        public DokanOptions build() {
            dokanOptions.writeField("Version", version);
            dokanOptions.writeField("SingleThread", singleThread);
            dokanOptions.writeField("Options", options);
            dokanOptions.writeField("GlobalContext", globalContext);
            dokanOptions.writeField("MountPoint", mountPoint);
            dokanOptions.writeField("UNCName", uncName);
            dokanOptions.writeField("Timeout", timeout);
            dokanOptions.writeField("AllocationUnitSize", allocationUnitSize);
            dokanOptions.writeField("SectorSize", sectorSize);
            dokanOptions.writeField("VolumeSecurityDescriptorLength", volumeSecurityDescriptorLength);
            dokanOptions.writeField("VolumeSecurityDescriptor", volumeSecurityDescriptor);
            return dokanOptions;
        }
    }
}
