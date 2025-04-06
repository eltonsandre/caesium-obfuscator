package dev.eltonsandre.caesium;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SynchronizedByteArrayOutputStreamWrapper extends OutputStream {
    // The console will be synchronized through a monitor.
    // WARNING! This could delay the code trying to write to the console!
    private final Object monitor = new Object();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Override
    public void write(int data) throws IOException {
        synchronized (monitor) {
            byteArrayOutputStream.write(data);
        }
    }

    public byte[] readEmpty() {
        byte[] bufferContent;
        synchronized(monitor) {
            bufferContent = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();
        }
        return bufferContent;
    }
}
