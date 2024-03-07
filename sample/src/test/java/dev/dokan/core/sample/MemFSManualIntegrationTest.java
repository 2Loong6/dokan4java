package dev.dokan.core.sample;

import dev.dokan.core.DokanException;
import dev.dokan.core.DokanMount;
import dev.dokan.core.constants.MountOptions;
import dev.dokan.core.sample.memfs.MemoryFs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class MemFSManualIntegrationTest {

    public static void main(String[] args) throws IOException, DokanException {
        var fs = new MemoryFs();
        try (
                var reader = new BufferedReader(new InputStreamReader(System.in));
                var mount = DokanMount.create(fs)
                        .withMountPath(Path.of("X:\\"))
                        .withOptions(MountOptions.MOUNT_MANAGER | MountOptions.STDERR | MountOptions.DEBUG)
                        .withTimeout(3000)
                        .withSingleThreaded(true)
                        .mount()
        ) {

            waitForUserInput(reader);
            mount.unmount();
        }
        System.out.println("Goodbye!");
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
