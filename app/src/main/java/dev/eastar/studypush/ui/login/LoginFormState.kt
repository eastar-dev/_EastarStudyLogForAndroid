package dev.eastar.studypush.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val groupNameError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)