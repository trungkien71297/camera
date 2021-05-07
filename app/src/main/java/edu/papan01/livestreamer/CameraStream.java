package edu.papan01.livestreamer;

public class CameraStream {
    private Live555Native live555Native;
    public CameraStream() {
        live555Native = new Live555Native();
    }

    public void startStream(){
        live555Native.initialize(30,8554);
        live555Native.doLoop();
    }
}
