package com.example.agenda_musical_reto1.ui.viewmodels.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.agenda_musical_reto1.data.AuthLoginRequest
import com.example.agenda_musical_reto1.data.AuthUpdatePassword
import com.example.agenda_musical_reto1.data.LoginResponse
import com.example.agenda_musical_reto1.data.User
import com.example.agenda_musical_reto1.data.repository.IUserRepository
import com.example.agenda_musical_reto1.utils.JWTUtils
import com.example.agenda_musical_reto1.utils.MyApp
import com.example.agenda_musical_reto1.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userRepository: IUserRepository) : ViewModel(),
    UserViewModelInterface {

    private val _user = MutableLiveData<Resource<LoginResponse>?>()
    override val user: MutableLiveData<Resource<LoginResponse>?> get() = _user

    private val _created = MutableLiveData<Resource<Int>?>()
    override val created: LiveData<Resource<Int>?> get() = _created

    private val _updated = MutableLiveData<Resource<Int>?>()
    override val updated: LiveData<Resource<Int>?> get() = _updated

    private val _deleted = MutableLiveData<Resource<Int>?>()
    override val deleted: LiveData<Resource<Int>?> get() = _deleted

    override fun onUserLogin(email: String, password: String) {
        val authLoginRequest = AuthLoginRequest(email, password)
        viewModelScope.launch {
            val authLoginResponse = getUserLogin(authLoginRequest)
            //COLOCAMOS EL TOKEN EN EL SHAREDPREFERENCES
            authLoginResponse.data?.let { MyApp.userPreferences.saveAuthToken(it.accessToken) }
            //OBTENEMOS EL USUARIO DEL TOKEN
            val loggedUser: User? = authLoginResponse.data?.let { JWTUtils.decoded(it.accessToken) }
            //TODO COLOCAR EL USUARIO EN SHAREDPREFERENCES

            if (loggedUser != null) {
                MyApp.userPreferences.saveLoggedUser(loggedUser)
            }
            _user.value = getUserLogin(authLoginRequest)
        }
    }

    override suspend fun getUserLogin(authLoginRequest: AuthLoginRequest): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            userRepository.getUserLogin(authLoginRequest)
        }
    }

    override fun onUserRegister(name: String, surname: String, email: String, password: String) {
        val newUser = User(name, surname, email, password)
        viewModelScope.launch {
            _created.value = registerUser(newUser)
        }
    }

    override suspend fun registerUser(user: User): Resource<Int> {
        return withContext(Dispatchers.IO) {
            userRepository.registerUser(user)
        }
    }

    override fun onUserUpdate(oldPassword: String, newPassword: String) {
        val authUpdatePassword = AuthUpdatePassword(oldPassword, newPassword)
        viewModelScope.launch {
            _updated.value = updateUserPassword(authUpdatePassword)
        }
    }

    override suspend fun updateUserPassword(authUpdatePassword: AuthUpdatePassword): Resource<Int> {
        return withContext(Dispatchers.IO) {
            userRepository.updateUserPassword(authUpdatePassword)
        }
    }

    override fun onDeleteUser() {
        viewModelScope.launch {
            _deleted.value = deleteUser()
        }
    }

    override suspend fun deleteUser(): Resource<Int> {
        return withContext(Dispatchers.IO) {
            userRepository.deleteUser()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(private val userRepository: IUserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return UserViewModel(userRepository) as T
    }
}