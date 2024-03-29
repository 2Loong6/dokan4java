package dev.dokan.core.structures;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinNT;
import dev.dokan.core.nativeannotations.Boolean;
import dev.dokan.core.nativeannotations.EnumSet;

/**
 * This is a Dokan specific implementation of the ACCESS_STATE structure of the Windows kernel.
 *
 * @see <a href="https://docs.microsoft.com/de-de/windows-hardware/drivers/ddi/wdm/ns-wdm-_access_state">ACCESS_STATE-Struktur (wdm.h)</a>
 * @see <a href="https://github.com/dokan-dev/dokany/blob/master/sys/public.h">DOKAN_ACCESS_STATE</a> of the Dokany project.
 */
@Structure.FieldOrder({"SecurityEvaluated", "GenerateAudit", "GenerateOnClose", "AuditPrivileges", "Flags", "RemainingDesiredAccess", "PreviouslyGrantedAccess", "OriginalDesiredAccess", "SecurityDescriptor", "ObjectName", "ObjectType"})
public class DokanAccessState extends Structure {

    /**
     * A boolean value that specifies whether security was evaluated as part of the access check.
     * This member is currently unused by drivers.
     */
    @Boolean
    public volatile byte SecurityEvaluated;

    /**
     * A Boolean value that specifies whether the access should generate an audit.
     * This member is currently unused by drivers.
     */
    @Boolean
    public volatile byte GenerateAudit;

    /**
     * A Boolean value that specifies whether an audit should be generated when the handle being created is closed.
     * This member is currently unused by drivers.
     */
    @Boolean
    public volatile byte GenerateOnClose;

    /**
     * A Boolean value that specifies whether a privilege usage should be audited.
     * This member is currently unused by drivers.
     */
    @Boolean
    public volatile byte AuditPrivileges;

    /**
     * A 32-bit value that contains bit-field flags for the access.
     * A driver can check for the traverse access flag (TOKEN_HAS_TRAVERSE_PRIVILEGE).
     * For more information about how to check for traverse access, see <a href="https://docs.microsoft.com/windows-hardware/drivers/ifs/checking-for-traverse-privilege-on-irp-mj-create">Check for Traverse Privilege on IRP_MJ_CREATE</a>.
     * A driver can also check for the TOKEN_IS_RESTRICTED flag.
     * These flags are defined in ntifs.h.
     */
    @EnumSet
    public volatile int Flags;

    /**
     * An <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/kernel/access-mask">ACCESS_MASK</a> type that describes the access rights that have not yet been granted to the caller.
     * A driver uses this member to determine if the Windows security system can grant access.
     * If access can be granted, the driver updates the <b>PreviouslyGrantedAccess</b> and <b>RemainingDesiredAccess</b> members accordingly.
     */
    @EnumSet
    public volatile int RemainingDesiredAccess;

    /**
     * An <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/kernel/access-mask">ACCESS_MASK</a> type that specifies the information about access that has already been granted to the caller of one of the <a href="https://learn.microsoft.com/en-us/previous-versions/windows/hardware/drivers/ff563711(v=vs.85)">Security Reference Monitor Routines</a>
     * The Windows security system grants certain rights based on the privileges of the caller, such as traverse right (the ability to traverse through a directory as part of opening a subdirectory or file).
     */
    @EnumSet
    public volatile int PreviouslyGrantedAccess;

    /**
     * An <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/kernel/access-mask">ACCESS_MASK</a> type that contains the original access rights that were requested by the caller.
     */
    @EnumSet
    public volatile int OriginalDesiredAccess;

    /**
     * A pointer to a <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/ns-ntifs-_security_descriptor">SECURITY_DESCRIPTOR</a> structure that contains security information for the object that this access relates to.
     */
    public volatile WinNT.SECURITY_DESCRIPTOR_RELATIVE.ByReference SecurityDescriptor;

    /**
     * A <a href="https://learn.microsoft.com/en-us/windows/win32/api/ntdef/ns-ntdef-_unicode_string">UNICODE_STRING</a> structure that contains the object name string for the access. This member is used for auditing.
     */
    public volatile UnicodeString ObjectName;

    /**
     * A <a href="https://learn.microsoft.com/en-us/windows/win32/api/ntdef/ns-ntdef-_unicode_string">UNICODE_STRING</a> structure that contains the object type name string for the access. This member is used for auditing.
     */
    public volatile UnicodeString ObjectType;

    public boolean getSecurityEvaluated() {
        return SecurityEvaluated != 0;
    }

    public boolean isGenerateAudit() {
        return GenerateAudit != 0;
    }

    public boolean isGenerateOnClose() {
        return GenerateOnClose != 0;
    }

    public boolean isAuditPrivileges() {
        return AuditPrivileges != 0;
    }

    public long getFlags() {
        return Integer.toUnsignedLong(Flags);
    }

    public long getRemainingDesiredAccess() {
        return Integer.toUnsignedLong(RemainingDesiredAccess);
    }

    public long getPreviouslyGrantedAccess() {
        return Integer.toUnsignedLong(PreviouslyGrantedAccess);
    }

    public long getOriginalDesiredAccess() {
        return Integer.toUnsignedLong(OriginalDesiredAccess);
    }
}
