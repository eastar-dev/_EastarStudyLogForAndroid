package dev.eastar.studypush.data

import dev.eastar.studypush.data.model.LoggedInUser
import retrofit2.http.POST
import smart.net.Net
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    @POST("login")
    fun login(
        @retrofit2.http.Field("studyGroup") studyGroup: String,
        @retrofit2.http.Field("username") username: String,
        @retrofit2.http.Field("password") password: String
    ): Result<LoggedInUser> {
        try {
            Net.create<LoginDataSource>().login(studyGroup, username, password)

            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}