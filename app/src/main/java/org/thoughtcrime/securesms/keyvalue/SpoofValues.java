package org.thoughtcrime.securesms.keyvalue;

import androidx.annotation.NonNull;

import org.signal.core.util.logging.Log;
import java.util.Collections;
import java.util.List;

public final class SpoofValues extends SignalStoreValues {

  private static final String TAG = Log.tag(SpoofValues.class);

  public  static final String SPOOF_ENABLED = "spoof.spoof_enabled";

  SpoofValues(KeyValueStore store) {
    super(store);
  }

  @Override
  void onFirstEverAppLaunch() {
  }

  @Override
  @NonNull List<String> getKeysToIncludeInBackup() {
    return Collections.emptyList();
  }

  public void setSpoofEnabled(boolean enabled) {
    putBoolean(SPOOF_ENABLED, enabled);
  }

  public boolean isSpoofEnabled() {
    return getBoolean(SPOOF_ENABLED, true);
  }
}