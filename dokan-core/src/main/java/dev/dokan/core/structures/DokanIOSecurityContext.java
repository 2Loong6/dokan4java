package dev.dokan.core.structures;

import com.sun.jna.Structure;
import dev.dokan.core.nativeannotations.EnumSet;

/**
 * The DokanIOSecurityContext contains the Dokan specific security context of the Windows kernel create request.
 * It is a parameter in the {@link DokanOperations.ZwCreateFile} callback.
 *
 * @see <a href="https://docs.microsoft.com/en-us/windows-hardware/drivers/ddi/wdm/ns-wdm-_io_security_context?redirectedfrom=MSDN">IO_SECURITY_CONTEXT structure (wdm.h)</a> of the original structure
 * @see <a href="https://github.com/dokan-dev/dokany/blob/master/sys/public.h">DOKAN_IO_SECURITY_CONTEXT</a> of the Dokany project.
 */
@Structure.FieldOrder({"accessState", "desiredAccess"})
public class DokanIOSecurityContext extends Structure implements Structure.ByReference {

    /**
     * Reserved for use by file systems and file system filter drivers.
     * This member is a pointer to an <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/wdm/ns-wdm-_access_state">ACCESS_STATE</a> structure that contains the object's subject context,
     * granted access types, and remaining desired access types.
     */
    public volatile DokanAccessState accessState;

    /**
     * An <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/wdm/ns-wdm-_access_state">ACCESS_MASK</a> value that expresses the access rights that are requested in the <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ifs/irp-mj-create">IRP_MJ_CREATE</a> request.
     */
    @EnumSet
    public volatile int desiredAccess;

    public long getDesiredAccess() {
        return Integer.toUnsignedLong(desiredAccess);
    }
}
