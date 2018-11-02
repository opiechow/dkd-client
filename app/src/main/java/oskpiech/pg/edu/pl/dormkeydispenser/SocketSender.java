package oskpiech.pg.edu.pl.dormkeydispenser;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketSender extends AsyncTask<Void, Void, Void> {
    final ProgramState state;

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
            /* TODO: Construct this appropriately, preferably outside this function */
            String jsonString = "{\"id\":1,\"authenticationMethod\":\"password\",\"data\":\"pass\"}";
            /* FIXME: Why on the other side an 8 is prepended to the string?" */
            oos.writeUTF(jsonString);
            // read the server response message
            numBytesRead = ois.read(response);
            String message = new String(response, StandardCharsets.UTF_8);
            if (message.contains("ok") && numBytesRead > 0) {
                if (!state.isAuthenticated()) {
                    state.setAuthenticated(true);
                } else {
                    state.setLockerOpened(true);
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
