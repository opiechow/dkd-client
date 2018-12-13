package pl.edu.pg.oskpiech.dormkeydispenser;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketSender extends AsyncTask<Void, Void, Void> {
    final private ProgramState state;

    SocketSender(final ProgramState state) {
        this.state = state;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        send();
        return null;
    }

    private void send() {
        int numBytesRead;
        try {
            byte[] response = new byte[64];
            for (int i = 0; i < 64; i++) {
                response[i] = 0;
            }
            InetAddress host = InetAddress.getByName("optiviadev.pl");
            Socket socket = new Socket(host.getHostName(), 50007);
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream());
            JSONObject json = state.getAuthJSON();
            String jsonString = json.toString();
            oos.write(jsonString.getBytes(), 0, jsonString.length());
            oos.write("DATA_END".getBytes(), 0, "DATA_END".length());
            // read the server response message
            numBytesRead = ois.read(response);
            String message = new String(response, StandardCharsets.UTF_8);
            if (numBytesRead > 0) {
                state.setAuthenticated(true);
                if (message.contains("ok")) {
                    state.setLockerOpened(true);
                } else {
                    state.setLockerOpened(false);
                }
            }
            // close resources
            ois.close();
            oos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
