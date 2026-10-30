package com.example.views_presentation.presentation.l

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.views_presentation.R
import com.example.views_presentation.databinding.ActivityLSignupBinding
import kotlinx.coroutines.launch


class SignupActivity : AppCompatActivity() {

    private val viewModel by viewModels<SignupViewModel>()
    private lateinit var binding: ActivityLSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        configureEdgeToEdge()

        setupEventListeners()
        observeViewModelState()
    }

    private fun configureEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBarLayout.setPadding(0, bars.top, 0, 0)
//            binding.continueButton.setPadding(0, 0, 0, bars.bottom)

            insets
        }
    }

    private fun setupEventListeners() {
        binding.usernameInput.editText?.addTextChangedListener { editable ->
            val username = editable?.toString() ?: ""
            viewModel.onUsernameChanged(value = username)
        }
        binding.passwordOriginalInput.editText?.addTextChangedListener { editable ->
            val password = editable?.toString() ?: ""
            viewModel.onPasswordChanged(value = password)
        }
        binding.passwordRepeatInput.editText?.addTextChangedListener { editable ->
            val repeat = editable?.toString() ?: ""
            viewModel.onRepeatPasswordChanged(value = repeat)
        }
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: SignupState) {
        binding.continueButton.isEnabled = state.canSignUp

        renderUsername(state.username, state.usernameValidation, state.usernameAvailability)
        renderOriginalPassword(state.password, state.passwordValidation)
        renderRepeatPassword(state.repeatPassword, state.isRepeatPasswordValid)
    }

    private fun renderUsername(
        value: String, validation: UsernameValidation, availability: UsernameAvailability,
    ) {
        binding.usernameInput.isHelperTextEnabled = false
        binding.usernameInput.isErrorEnabled = false
        binding.usernameInput.helperText = null
        binding.usernameInput.error = null

        if (value.isEmpty()) {
            return
        }

        if (!validation.valid) {
            binding.usernameInput.error =
                getString(R.string.username_must_consist_of_at_least_4_letters)
            return
        }

        when (availability) {
            is UsernameAvailability.Loading -> {
                binding.usernameInput.helperText =
                    getString(R.string.checking_availability_for_username, availability.username)
            }

            is UsernameAvailability.Error -> {
                binding.usernameInput.error = getString(
                    R.string.unable_to_check_username_availability_reason, availability.error
                )
            }

            is UsernameAvailability.Success if !availability.isAvailable -> {
                binding.usernameInput.error =
                    getString(R.string.username_is_taken, availability.username)
            }

            is UsernameAvailability.Success -> {
                binding.usernameInput.helperText = getString(
                    R.string.username_is_available, availability.username
                )
            }

            else -> {}
        }
    }

    private fun renderOriginalPassword(value: String, validation: PasswordValidation) {
        binding.passwordOriginalInput.isErrorEnabled = false
        binding.passwordOriginalInput.error = null

        if (value.isNotEmpty() && !validation.valid) {
            binding.passwordOriginalInput.error = getString(R.string.password_is_not_strong_enough)
        }

        binding.passwordHasMinimumLengthCheckbox.isChecked = validation.hasMinimumLength
        binding.passwordHasDigitCheckbox.isChecked = validation.hasDigit
        binding.passwordHasLowerCaseLetterCheckbox.isChecked = validation.hasLowerCaseLetter
        binding.passwordHasUpperCaseLetterCheckbox.isChecked = validation.hasUpperCaseLetter
    }

    private fun renderRepeatPassword(value: String, valid: Boolean) {
        binding.passwordRepeatInput.isErrorEnabled = false
        binding.passwordRepeatInput.error = null

        if (value.isNotEmpty() && !valid) {
            binding.passwordRepeatInput.error = getString(R.string.passwords_do_not_match)
        }
    }
}