package com.skaalsolutions.midnightchannel.navigation

import com.google.common.truth.Truth.assertThat
import com.skaalsolutions.midnightchannel.connectivity.ConnectivityStatus
import com.skaalsolutions.midnightchannel.recovery.ChannelRecoveryController
import org.junit.Test

class ShellControllerTest {

    @Test
    fun dispatch_drives_store_to_ready() {
        val controller = ShellController()
        controller.dispatch(ShellEvent.SplashFloorElapsed)
        controller.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(controller.current()).isEqualTo(ShellState.Ready)
    }

    @Test
    fun main_frame_finished_while_ready_clears_recovery_flight() {
        val controller = ShellController()
        controller.dispatch(ShellEvent.NetworkLost)
        assertThat(controller.current()).isEqualTo(ShellState.Offline)

        val recovery = ChannelRecoveryController(
            dispatch = controller::dispatch,
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = controller::current,
        )
        controller.bindRecovery(recovery)
        assertThat(recovery.requestRetry()).isTrue()
        assertThat(recovery.isInFlight).isTrue()
        assertThat(controller.current()).isEqualTo(ShellState.Retrying)

        // Retrying + MainFrameLoadFinished → Ready; recovery hook ends flight.
        controller.dispatch(ShellEvent.MainFrameLoadFinished)
        assertThat(controller.current()).isEqualTo(ShellState.Ready)
        assertThat(recovery.isInFlight).isFalse()
    }

    @Test
    fun retry_failed_clears_recovery_flight() {
        val controller = ShellController()
        controller.dispatch(ShellEvent.NetworkLost)
        val recovery = ChannelRecoveryController(
            dispatch = controller::dispatch,
            connectivitySnapshot = { ConnectivityStatus.Available },
            currentState = controller::current,
        )
        controller.bindRecovery(recovery)
        assertThat(recovery.requestRetry()).isTrue()

        controller.dispatch(ShellEvent.RetryFailed)
        assertThat(controller.current()).isEqualTo(ShellState.Offline)
        assertThat(recovery.isInFlight).isFalse()
    }
}
