package dev.dokan.core.sample.iso;

import com.palantir.isofilereader.isofilereader.IsoFileReader;
import dev.dokan.core.DokanMount;
import dev.dokan.core.constants.MountOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class IsoFsManualIntegrationTest {

    // todo: iso file system should be mounted with readonly udf filesystem
    public static void main(String[] args) {
        IsoFileReader reader = null;
        DokanMount mount = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            reader = new IsoFileReader(new File("./src/test/resources/image.iso"));
            IsoFs fs = new IsoFs(reader);
            mount = DokanMount.create(fs)
                    .withMountPath(Path.of("X:\\"))
                    .withOptions(MountOptions.MOUNT_MANAGER | MountOptions.STDERR | MountOptions.DEBUG)
                    .withTimeout(3000)
                    .withSingleThreaded(true)
                    .mount();
            waitForUserInput(bufferedReader);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (mount != null) {
                mount.close();
            }
        }
    }

    private static void waitForUserInput(BufferedReader reader) throws IOException {
        System.out.println("Please enter ONE Character to continue...");
        char exit = ' ';
        while (!Character.isAlphabetic(exit)) {
            try {
                exit = (char) reader.read();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
