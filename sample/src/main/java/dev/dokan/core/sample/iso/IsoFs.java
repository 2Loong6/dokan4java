package dev.dokan.core.sample.iso;

import com.palantir.isofilereader.isofilereader.GenericInternalIsoFile;
import com.palantir.isofilereader.isofilereader.IsoFileReader;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase;
import dev.dokan.core.DokanFileSystem;
import dev.dokan.core.NTStatus;
import dev.dokan.core.nativeannotations.EnumSet;
import dev.dokan.core.structures.DokanFileInfo;
import dev.dokan.core.structures.DokanIOSecurityContext;
import dev.dokan.core.structures.DokanOperations;

import java.util.Date;

import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_DIRECTORY;
import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_READONLY;

// todo : implement
public class IsoFs implements DokanFileSystem {

    private final static char[] EMPTY_ALT_NAME = new char[]{'\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0'};

    private final IsoFileReader reader;

    public IsoFs(IsoFileReader reader) {
        this.reader = reader;
    }

    @Override
    public int zwCreateFile(WString path, DokanIOSecurityContext securityContext, @EnumSet int desiredAccess, @EnumSet int fileAttributes, @EnumSet int shareAccess, int createDisposition, @EnumSet int createOptions, DokanFileInfo dokanFileInfo) {
        return DokanFileSystem.super.zwCreateFile(path, securityContext, desiredAccess, fileAttributes, shareAccess, createDisposition, createOptions, dokanFileInfo);
    }

    @Override
    public int findFiles(WString path, DokanOperations.PFillFindData fillFindDataCallback, DokanFileInfo dokanFileInfo) {
        try {
            GenericInternalIsoFile file;
            if (path.toString().equals("\\")) {
                file = reader.getAllFiles()[0];
            } else {
                file = reader.getSpecificFileByName(reader.getAllFiles(), path.toString()).orElse(null);
            }
            if (file.isDirectory()) {
                for (GenericInternalIsoFile child : file.getChildren()) {
                    int attributes = 0;
                    if (child.isDirectory()) {
                        attributes |= FILE_ATTRIBUTE_DIRECTORY;
                    } else {
                        attributes |= FILE_ATTRIBUTE_READONLY;
                    }
                    WinBase.WIN32_FIND_DATA findData = new WinBase.WIN32_FIND_DATA(
                            attributes,
                            new WinBase.FILETIME(child.getDateAsDate().orElse(new Date())),
                            new WinBase.FILETIME(child.getDateAsDate().orElse(new Date())),
                            new WinBase.FILETIME(child.getDateAsDate().orElse(new Date())),
                            (int) (child.getSize() >>> 32),
                            (int) child.getSize(),
                            0,
                            0,
                            toFIND_DATAFileName(child.getFileName()),
                            EMPTY_ALT_NAME
                    );
                    fillFindDataCallback.invoke(findData, dokanFileInfo);
                }
            } else {
                return NTStatus.UNSUCCESSFUL;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return NTStatus.UNSUCCESSFUL;
        }
        
        return NTStatus.UNSUCCESSFUL;
    }

    private char[] toFIND_DATAFileName(String s) {
        char[] buffer = new char[WinBase.MAX_PATH];
        s.getChars(0, s.length(), buffer, 0);
        buffer[s.length()] = '\0';
        return buffer;
    }
}
