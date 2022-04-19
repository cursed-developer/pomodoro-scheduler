package com.emenjivar.pomodoro.screens.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.emenjivar.core.model.Pomodoro
import com.emenjivar.core.usecase.GetAutoPlayUseCase
import com.emenjivar.core.usecase.GetPomodoroUseCase
import com.emenjivar.core.usecase.IsKeepScreenOnUseCase
import com.emenjivar.core.usecase.SetAutoPlayUseCase
import com.emenjivar.core.usecase.SetKeepScreenOnUseCase
import com.emenjivar.core.usecase.SetRestTimeUseCase
import com.emenjivar.core.usecase.SetWorkTimeUseCase
import com.emenjivar.pomodoro.MainCoroutineRule
import com.emenjivar.pomodoro.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var getPomodoroUseCase: GetPomodoroUseCase
    private lateinit var setWorkTimeUseCase: SetWorkTimeUseCase
    private lateinit var setRestTimeUseCase: SetRestTimeUseCase
    private lateinit var getAutoPlayUseCase: GetAutoPlayUseCase
    private lateinit var setAutoPlayUseCase: SetAutoPlayUseCase
    private lateinit var isKeepScreenOnUseCase: IsKeepScreenOnUseCase
    private lateinit var setKeepScreenOnUseCase: SetKeepScreenOnUseCase
    private lateinit var settingsViewModel: SettingsViewModel

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun prepareTest() {
        getPomodoroUseCase = Mockito.mock(GetPomodoroUseCase::class.java)
        setWorkTimeUseCase = Mockito.mock(SetWorkTimeUseCase::class.java)
        setRestTimeUseCase = Mockito.mock(SetRestTimeUseCase::class.java)
        getAutoPlayUseCase = Mockito.mock(GetAutoPlayUseCase::class.java)
        setAutoPlayUseCase = Mockito.mock(SetAutoPlayUseCase::class.java)
        isKeepScreenOnUseCase = Mockito.mock(IsKeepScreenOnUseCase::class.java)
        setKeepScreenOnUseCase = Mockito.mock(SetKeepScreenOnUseCase::class.java)

        settingsViewModel = SettingsViewModel(
            getPomodoroUseCase = getPomodoroUseCase,
            setWorkTimeUseCase = setWorkTimeUseCase,
            setRestTimeUseCase = setRestTimeUseCase,
            getAutoPlayUseCase = getAutoPlayUseCase,
            setAutoPlayUseCase = setAutoPlayUseCase,
            isKeepScreenOnUseCase = isKeepScreenOnUseCase,
            setKeepScreenOnUseCase = setKeepScreenOnUseCase,
            ioDispatcher = Dispatchers.Main,
            testMode = true
        )
    }

    @Test
    fun defaultValues() {
        with(settingsViewModel) {
            assertEquals(0L, pomodoroTime.value)
            assertEquals(0L, restTime.value)
            assertFalse(closeSettings.value ?: true)
            assertFalse(autoPlay.value)
            assertFalse(keepScreenOn.value)
        }
    }

    @Test
    fun `loadSettings test`() = runTest {
        // Given 25 and 5 minutes
        Mockito.`when`(getPomodoroUseCase.invoke()).thenReturn(
            Pomodoro(workTime = 1500000L, restTime = 300000L)
        )
        Mockito.`when`(getAutoPlayUseCase.invoke())
            .thenReturn(true)
        Mockito.`when`(isKeepScreenOnUseCase.invoke())
            .thenReturn(true)

        with(settingsViewModel) {
            // When
            settingsViewModel.loadSettings()

            // Then verify the values are loaded in readable minutes
            assertEquals(25, pomodoroTime.getOrAwaitValue())
            assertEquals(5, restTime.getOrAwaitValue())
            assertTrue(autoPlay.value)
            assertTrue(keepScreenOn.value)
        }
    }

    @Test
    fun `setPomodoroTime using string parameter`() {
        settingsViewModel.setPomodoroTime("10")
        assertEquals(10L, settingsViewModel.pomodoroTime.getOrAwaitValue())
    }

    @Test
    fun `setPomodoroTime using an invalid string parameter`() {
        settingsViewModel.setPomodoroTime("NaN")
        assertEquals(0L, settingsViewModel.pomodoroTime.getOrAwaitValue())
    }

    @Test
    fun `setRestTime using string parameter`() {
        settingsViewModel.setRestTime("10")
        assertEquals(10L, settingsViewModel.restTime.getOrAwaitValue())
    }

    @Test
    fun `setRestTime using an invalid string parameter`() {
        settingsViewModel.setRestTime("NaN")
        assertEquals(0L, settingsViewModel.restTime.getOrAwaitValue())
    }

    @Test
    fun `setAutoPlay test`() = runTest {
        with(settingsViewModel) {
            var localAutoPlay = false

            Mockito.`when`(setAutoPlayUseCase.invoke(true))
                .then {
                    localAutoPlay = true
                    it
                }
            Mockito.`when`(setAutoPlayUseCase.invoke(false))
                .then {
                    localAutoPlay = false
                    it
                }

            setAutoPlay(true)
            assertTrue(autoPlay.value)
            assertTrue(localAutoPlay)

            setAutoPlay(false)
            assertFalse(autoPlay.value)
            assertFalse(localAutoPlay)
        }
    }

    @Test
    fun `setKeepScreenOn test`() = runTest {
        with(settingsViewModel) {
            var localKeepScreenOn = false

            Mockito.`when`(setKeepScreenOnUseCase.invoke(true))
                .then {
                    localKeepScreenOn = true
                    it
                }
            Mockito.`when`(setKeepScreenOnUseCase.invoke(false))
                .then {
                    localKeepScreenOn = false
                    it
                }

            setKeepScreenOn(true)
            assertTrue(keepScreenOn.value)
            assertTrue(localKeepScreenOn)

            setKeepScreenOn(false)
            assertFalse(keepScreenOn.value)
            assertFalse(localKeepScreenOn)
        }
    }

    @Test
    fun `closeSettings test`() {
        settingsViewModel.closeSettings()

        val result = settingsViewModel.closeSettings.getOrAwaitValue()
        assertTrue(result)
    }
}
