package org.thoughtcrime.securesms.jobs

import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.pin.SvrRepository
import org.whispersystems.signalservice.api.push.exceptions.NonSuccessfulResponseCodeException
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Refresh KBS authentication credentials for talking to KBS during re-registration.
 */
class RefreshSvrCredentialsJob private constructor(parameters: Parameters) : BaseJob(parameters) {

  companion object {
    const val KEY = "RefreshKbsCredentialsJob"

    private val TAG = Log.tag(RefreshSvrCredentialsJob::class.java)
    private val FREQUENCY: Duration = 15.days

    @JvmStatic
    fun enqueueIfNecessary() {
      if (SignalStore.svr().hasPin()) {
        val lastTimestamp = SignalStore.svr().lastRefreshAuthTimestamp
        if (lastTimestamp + FREQUENCY.inWholeMilliseconds < System.currentTimeMillis() || lastTimestamp > System.currentTimeMillis()) {
          ApplicationDependencies.getJobManager().add(RefreshSvrCredentialsJob())
        } else {
          Log.d(TAG, "Do not need to refresh credentials. Last refresh: $lastTimestamp")
        }
      }
    }
  }

  private constructor() : this(
    parameters = Parameters.Builder()
      .setQueue("RefreshKbsCredentials")
      .addConstraint(NetworkConstraint.KEY)
      .setMaxInstancesForQueue(2)
      .setLifespan(1.days.inWholeMilliseconds)
      .build()
  )

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  override fun onRun() {
    SvrRepository.refreshAndStoreAuthorization()
  }

  override fun onShouldRetry(e: Exception): Boolean {
    return e is IOException && e !is NonSuccessfulResponseCodeException
  }

  override fun onFailure() = Unit

  class Factory : Job.Factory<RefreshSvrCredentialsJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): RefreshSvrCredentialsJob {
      return RefreshSvrCredentialsJob(parameters)
    }
  }
}
