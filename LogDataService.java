package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LogDataService extends Service {
    public LogDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
