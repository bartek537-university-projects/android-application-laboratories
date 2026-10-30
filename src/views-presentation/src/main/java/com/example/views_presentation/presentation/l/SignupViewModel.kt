package com.example.views_presentation.presentation.l

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

sealed interface UsernameAvailability {
    data object Idle : UsernameAvailability
    data class Loading(val username: String) : UsernameAvailability
    data class Success(val username: String, val isAvailable: Boolean) : UsernameAvailability
    data class Error(val username: String, val error: String) : UsernameAvailability
}

//data class UsernameAvailability(
//    val username: String = "",
//    val isAvailable: Boolean = false,
//    val isLoading: Boolean = false,
//    val isError: Boolean = false
//) {
//    val valid: Boolean
//        get() = isAvailable && !isLoading && !isError
//}

data class UsernameValidation(
    val hasMinimumLength: Boolean = false,
    val containsOnlyLetters: Boolean = false,
) {
    val valid: Boolean
        get() = hasMinimumLength && containsOnlyLetters

    companion object {
        private const val MINIMUM_USERNAME_LENGTH: Int = 4

        fun validate(value: String) = UsernameValidation(
            hasMinimumLength = value.length >= MINIMUM_USERNAME_LENGTH,
            containsOnlyLetters = value.all { it.isLetter() })
    }
}

data class PasswordValidation(
    val hasMinimumLength: Boolean = false,
    val hasDigit: Boolean = false,
    val hasLowerCaseLetter: Boolean = false,
    val hasUpperCaseLetter: Boolean = false,
) {
    val valid: Boolean
        get() = hasMinimumLength && hasDigit && hasLowerCaseLetter && hasUpperCaseLetter

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH: Int = 8

        fun validate(value: String) = PasswordValidation(
            hasMinimumLength = value.length >= MINIMUM_PASSWORD_LENGTH,
            hasDigit = value.any { it.isDigit() },
            hasLowerCaseLetter = value.any { it.isLowerCase() },
            hasUpperCaseLetter = value.any { it.isUpperCase() },
        )
    }
}

data class SignupState(
    val username: String = "",
    val usernameValidation: UsernameValidation = UsernameValidation(),
    val usernameAvailability: UsernameAvailability = UsernameAvailability.Idle,
    val password: String = "",
    val passwordValidation: PasswordValidation = PasswordValidation(),
    val repeatPassword: String = "",
    val isRepeatPasswordValid: Boolean = true,
) {
    val canSignUp: Boolean
        get() = usernameValidation.valid && isUsernameAvailable && passwordValidation.valid && isRepeatPasswordValid

    private val isUsernameAvailable: Boolean
        get() = when (usernameAvailability) {
            is UsernameAvailability.Success if usernameAvailability.isAvailable -> true
            else -> false
        }
}

interface UsernameRepository {
    suspend fun isAvailable(username: String): Boolean
}

class InMemoryUsernameRepository : UsernameRepository {

    override suspend fun isAvailable(username: String): Boolean {
        delay(500.milliseconds)

        return when {
            username.startsWith("er") -> throw IOException()
            username.startsWith("du") -> false
            else -> true
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SignupViewModel(
    private val repository: UsernameRepository = InMemoryUsernameRepository()
) : ViewModel() {

    private val username = MutableStateFlow("")

    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    init {
        observeUsernameAvailability()
    }

    fun onUsernameChanged(value: String) {
        _state.update { state ->
            state.copy(
                username = value,
                usernameValidation = UsernameValidation.validate(value),
                usernameAvailability = UsernameAvailability.Loading(value)
            )
        }
        username.value = value
    }

    fun onPasswordChanged(value: String) = _state.update { state ->
        state.copy(
            password = value,
            passwordValidation = PasswordValidation.validate(value),
            isRepeatPasswordValid = validateRepeatPassword(value, state.repeatPassword),
        )
    }

    fun onRepeatPasswordChanged(value: String) = _state.update { state ->
        state.copy(
            repeatPassword = value,
            isRepeatPasswordValid = validateRepeatPassword(state.password, value),
        )
    }

    private fun observeUsernameAvailability() {
        username.map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { username ->
                createUsernameAvailabilityFlow(username)
            }
            .onEach { availability ->
                _state.update { it.copy(usernameAvailability = availability) }
            }
            .launchIn(viewModelScope)
    }

    private fun createUsernameAvailabilityFlow(username: String) = flow {
        emit(UsernameAvailability.Loading(username))

        try {
            val available = repository.isAvailable(username)
            emit(UsernameAvailability.Success(username, available))
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            val message = ex.message ?: ""
            emit(UsernameAvailability.Error(username, message))
        }
    }.debounce(300.milliseconds)

    private fun validateRepeatPassword(original: String, repeat: String): Boolean {
        return original == repeat
    }
}