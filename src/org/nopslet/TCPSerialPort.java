package org.nopslet;

import javax.comm.*;
import java.io.*;
import java.net.Socket;
import java.util.TooManyListenersException;

public class TCPSerialPort extends SerialPort implements Runnable {

    private final Socket socket;
    private final PipedInputStream inputStream;
    private final PipedOutputStream pos;
    private String name;
    private SerialPortEventListener eventListener;
    private Thread notificationThread;
    private boolean notifyOnDataAvailable;



    public TCPSerialPort(String addresscolonport) throws IOException {
        int colon = addresscolonport.indexOf(':');

        socket = new Socket(addresscolonport.substring(0, colon).trim(), Integer.valueOf(addresscolonport.substring(colon + 1).trim()));

        pos = new PipedOutputStream();
        inputStream = new PipedInputStream(pos);
        name = addresscolonport;

        this.notificationThread = new Thread(null, this,
                "TCPSerialPort Notification thread");
        this.notificationThread.start();

    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    public synchronized void addEventListener(SerialPortEventListener paramSerialPortEventListener)
            throws TooManyListenersException {
        if (this.socket.isClosed()) {
            throw new IllegalStateException("Port Closed");
        }
        if (this.eventListener != null) {
            throw new TooManyListenersException();
        }
        eventListener = paramSerialPortEventListener;
    }

    @Override
    public void removeEventListener() {
        eventListener = null;
    }


    @Override
    public void run() {
        int count, i = 0;
        if (rcvThreshold > 0) {
            count = rcvThreshold;
        } else {
            count = 1;
        }

        InputStream sockInput;
        try {
            sockInput = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            TCPSerialPort.this.notificationThread = null;
            return;
        }

        while (true) {
            if (socket.isClosed()) {
                TCPSerialPort.this.notificationThread = null;
                return;
            }


            try {
                int read = sockInput.read();
                if (read < 0) {
                    TCPSerialPort.this.notificationThread = null;
                    return;
                }
                i++;
                if (i >= count) {
                    pos.write(read);
                    sendDataAvailEvent();
                    i = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
                TCPSerialPort.this.notificationThread = null;
                return;
            }
        }
    }

    void sendDataAvailEvent() {
        if (notifyOnDataAvailable) {
            SerialPortEvent localSerialPortEvent = new SerialPortEvent(this,
                    1, false, true);
            eventListener.serialEvent(localSerialPortEvent);
        }
    }

    @Override
    public void notifyOnDataAvailable(boolean b) {
        notifyOnDataAvailable = b;
    }


    @Override
    public String getName() {
        return name;
    }

    private int rcvThreshold = -1;

    public void enableReceiveThreshold(int paramInt) //eroslink doet 1
            throws UnsupportedCommOperationException {
        if (this.socket.isClosed()) {
            throw new IllegalStateException("Port Closed");
        }
        if (paramInt < 1) {
            throw new UnsupportedCommOperationException("This threshold value is not supported");
        }
        this.rcvThreshold = paramInt;
    }

    public void disableReceiveThreshold() {
        if (this.socket.isClosed()) {
            throw new IllegalStateException("Port Closed");
        }
        this.rcvThreshold = -1;
    }

    public boolean isReceiveThresholdEnabled() {
        if (this.socket.isClosed()) {
            throw new IllegalStateException("Port Closed");
        }
        return this.rcvThreshold != -1;
    }

    public int getReceiveThreshold() {
        if (this.socket.isClosed()) {
            throw new IllegalStateException("Port Closed");
        }
        return this.rcvThreshold;
    }

    /* stubs */

    @Override
    public void notifyOnOutputEmpty(boolean paramBoolean) {

    }

    @Override
    public void notifyOnCTS(boolean paramBoolean) {

    }

    @Override
    public void notifyOnDSR(boolean paramBoolean) {

    }

    @Override
    public void notifyOnRingIndicator(boolean paramBoolean) {

    }

    @Override
    public void notifyOnCarrierDetect(boolean paramBoolean) {

    }

    @Override
    public void notifyOnOverrunError(boolean paramBoolean) {

    }

    @Override
    public void notifyOnParityError(boolean paramBoolean) {
    }

    @Override
    public void notifyOnFramingError(boolean paramBoolean) {
    }

    @Override
    public void notifyOnBreakInterrupt(boolean paramBoolean) {
    }


    @Override
    public void enableReceiveTimeout(int paramInt) {
    }

    @Override
    public void disableReceiveTimeout() {
    }

    @Override
    public boolean isReceiveTimeoutEnabled() {
        return false;
    }

    @Override
    public int getReceiveTimeout() {
        return 0;
    }

    @Override
    public void enableReceiveFraming(int paramInt) {
    }

    @Override
    public void disableReceiveFraming() {
    }

    @Override
    public boolean isReceiveFramingEnabled() {
        return false;
    }

    @Override
    public int getReceiveFramingByte() {
        return -1;
    }

    @Override
    public void setInputBufferSize(int paramInt) {
    }

    @Override
    public int getInputBufferSize() {
        return -1;
    }

    @Override
    public void setOutputBufferSize(int paramInt) {
    }

    @Override
    public int getOutputBufferSize() {
        return -1;
    }

    @Override
    public int getBaudRate() {
        return -1;
    }

    @Override
    public int getDataBits() {
        return -1;
    }

    @Override
    public int getStopBits() {
        return -1;
    }

    @Override
    public int getParity() {
        return -1;
    }

    @Override
    public void sendBreak(int paramInt) {
    }

    @Override
    public void setFlowControlMode(int i) throws UnsupportedCommOperationException {
    }

    @Override
    public int getFlowControlMode() {
        return -1;
    }

    @Override
    public void setSerialPortParams(int baud, int databits, int stopbits, int parity) throws UnsupportedCommOperationException {
    }

    @Override
    public void setDTR(boolean paramBoolean) {
    }

    @Override
    public boolean isDTR() {
        return false;
    }

    @Override
    public void setRTS(boolean paramBoolean) {
    }

    @Override
    public boolean isRTS() {
        return false;
    }

    @Override
    public boolean isCTS() {
        return false;
    }

    @Override
    public boolean isDSR() {
        return false;
    }

    @Override
    public boolean isRI() {
        return false;
    }

    @Override
    public boolean isCD() {
        return false;
    }

}
